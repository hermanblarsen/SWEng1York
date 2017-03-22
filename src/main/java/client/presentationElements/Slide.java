package client.presentationElements;

import client.exceptions.SequenceNotFoundException;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by habl on 23/02/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Slide extends StackPane {
    Logger logger = LoggerFactory.getLogger(Slide.class);

    protected List<SlideElement> slideElementList;
    protected List<SlideElement> visibleSlideElementList;
    protected List<TextElement> textElementList = new ArrayList<>();
    protected List<GraphicElement> graphicElementList = new ArrayList<>();
    protected List<ImageElement> imageElementList = new ArrayList<>();
    protected List<VideoElement> videoElementList = new ArrayList<>();
    protected List<AudioElement> audioElementList = new ArrayList<>();

    //Parameters for searchForSequenceElement method. Is this good practice? - Amrik
    //  I would say so, far more readable with this than the numbers themselves.. - Herman
    private static final int START_SEARCH = 0;
    private static final int END_SEARCH = 1;

    //Passed back up to Presentation layer, to alert on whether sequence has changed the current slide or not
    public static final int SLIDE_NO_MOVE = 0;
    public static final int SLIDE_FORWARD = 1;
    public static final int SLIDE_BACKWARD = 2;

    protected int slideID;

    //Current Sequence number on slide
    int currentSequence = 0; //TODO is this something that should be in slide or in a presentation manager? -Herman
    int maxSequenceNumber;

    public Slide() {
        slideElementList = new ArrayList<>();
        visibleSlideElementList = new ArrayList<>();
    }

    public void addElement(int elementIndex, SlideElement newElement) {
        this.slideElementList.add(elementIndex, newElement);
    }


    public void addElement(SlideElement newElement) {
        logger.debug("Adding new element to SlideID: %d, slideID");
        this.slideElementList.add(this.slideElementList.size(), newElement);
    }


    public void deleteElementWithIndex(int elementIndex) {

    }

    public void deleteElementWithID(int elementID) {

    }

    public void moveElementToIndex(int index) {

    }

    public List<SlideElement> getSlideElementList() {
        return slideElementList;
    }

    public void setSlideElementList(List<SlideElement> slideElementList) {
        this.slideElementList = slideElementList;

        //Set Max Sequence number
        maxSequenceNumber = getMaxSequenceNumber(slideElementList);

        //Add slideElements to specific arraylists
        for (SlideElement slideElement : slideElementList) {
            if (slideElement instanceof TextElement) textElementList.add((TextElement) slideElement);
            if (slideElement instanceof GraphicElement) graphicElementList.add((GraphicElement) slideElement);
            if (slideElement instanceof ImageElement) imageElementList.add((ImageElement) slideElement);
            if (slideElement instanceof VideoElement) videoElementList.add((VideoElement) slideElement);
            if (slideElement instanceof AudioElement) audioElementList.add((AudioElement) slideElement);
        }
    }

    private int getMaxSequenceNumber(List<SlideElement> slideElementList) {
        int max = 0;

        for (SlideElement slideElement : slideElementList) {
            if (slideElement.getStartSequence() > max) {
                max = slideElement.getStartSequence();
            }

            if (slideElement.getEndSequence() > max) {
                max = slideElement.getEndSequence();
            }
        }
        return max;
    }

    public int getSlideID() {
        return slideID;
    }

    public void setSlideID(int slideID) {
        this.slideID = slideID;
    }

    public int advance(int direction) {
        SlideElement checkInVisibleSet;
        //If we're going forwards and not through all sequences in slide set
        if ((currentSequence < maxSequenceNumber) && (direction == SLIDE_FORWARD)) {
            currentSequence++;
            //Search for element with matching start sequence or end sequence in visible set. If they're not in there, add them.
            try {
                if (!(visibleSlideElementList.contains(checkInVisibleSet = searchForSequenceElement(slideElementList, currentSequence, START_SEARCH)))) {
                    visibleSlideElementList.add(checkInVisibleSet);
                }
                if (!(visibleSlideElementList.contains(checkInVisibleSet = searchForSequenceElement(slideElementList, currentSequence, END_SEARCH)))) {
                    visibleSlideElementList.add(checkInVisibleSet);
                }
            } catch (SequenceNotFoundException e) {
                logger.error("Failed to find Element with Sequence number of " + currentSequence + " in slideElementList. XML invalid?");
                return SLIDE_NO_MOVE;
                //return SLIDE_FORWARD; //TODO for testing, remove Slide forward when done testing
            }
        } else if ((currentSequence > 0) && (direction == SLIDE_BACKWARD)) {  //If we're going backwards and still elements left
            try {
                if (visibleSlideElementList.contains(checkInVisibleSet = searchForSequenceElement(slideElementList, currentSequence, START_SEARCH))) {
                    checkInVisibleSet.removeElement();
                }
            } catch (SequenceNotFoundException e) {
                logger.error("Failed to find Element with Sequence number of " + currentSequence + " in slideElementList. XML invalid?");
                return SLIDE_NO_MOVE;
                //return SLIDE_BACKWARD; //TODO for testing, remove Slide forward when done testing
            }
            currentSequence--;
        } else {
            //If we're at limit of sequence number, alert calling method that we need to move to next/previous slide dependent on direction
            switch (direction) {
                case SLIDE_FORWARD:
                    return SLIDE_FORWARD;
                case SLIDE_BACKWARD:
                    return SLIDE_BACKWARD;
            }
        }

        //Sort by Layer
        sortElementsByLayer(visibleSlideElementList);
        logger.info("Current Sequence is " + currentSequence);
        //Fire animations
        for (SlideElement elementToAnimate : visibleSlideElementList) {
            if (elementToAnimate.getStartSequence() == currentSequence) {
                elementToAnimate.renderElement(Animation.ENTRY_ANIMATION); //Entry Sequence
            } else if (elementToAnimate.getEndSequence() == currentSequence) {
                elementToAnimate.renderElement(Animation.EXIT_ANIMATION); //Exit Sequence
            }
        }

        return SLIDE_NO_MOVE;
    }

    private void sortElementsByLayer(List<SlideElement> slideElementListToSort) {
        //Sort by Layer
        slideElementListToSort.sort((element1, element2) -> {
            if (element1.getLayer() == element2.getLayer())
                return 0;
            return element1.getLayer() < element2.getLayer() ? -1 : 1;
        });
    }


    /**
     * Searches for currentSequence number in either start sequence or end sequence field. Used to determine whether to add an element to the visible
     * set. Throws not found exception if cant find a start or end sequence that matches.
     *
     * @param toSearch   List of Slide elements to search
     * @param sequence   Sequence number to search for in list (start or end)
     * @param startOrEnd Specifies whether to search for start sequence or end sequence
     * @return Returns slideElement corresponding to element with desired sequence number and type
     * @throws SequenceNotFoundException If a sequence number cant be found, there is most likely an error
     */
    private SlideElement searchForSequenceElement(List<SlideElement> toSearch, int sequence, int startOrEnd) throws SequenceNotFoundException {
        SlideElement toReturnStart = null;
        SlideElement toReturnEnd = null;

        //Search simulateously for start and end sequence elements
        for (SlideElement slideElement : toSearch) {
            if (slideElement.getStartSequence() == sequence) {
                toReturnStart = slideElement;
            }

            if (slideElement.getEndSequence() == sequence) {
                toReturnEnd = slideElement;
            }
        }

        //We cant find an element in SlideElementList with correct sequenceNumber. An error has occurred.
        if ((toReturnStart == null) && (toReturnEnd == null)) {
            throw new SequenceNotFoundException();
        }

        //If we can return element with startSequence, do it. Else return endSequence. Avoids checking for null return;
        switch (startOrEnd) {
            case START_SEARCH:
                if (toReturnStart == null) return toReturnEnd;
                else return toReturnStart;

            case END_SEARCH:
                if (toReturnEnd == null) return toReturnStart;
                else return toReturnEnd;
        }

        //We should never hit this. Satisfy JVM.
        return null;
    }

    public List<TextElement> getTextElementList() {
        return textElementList;
    }

    public void setTextElementList(List<TextElement> textElementList) {
        this.textElementList = textElementList;
    }

    public List<GraphicElement> getGraphicElementList() {
        return graphicElementList;
    }

    public void setGraphicElementList(List<GraphicElement> graphicElementList) {
        this.graphicElementList = graphicElementList;
    }

    public List<ImageElement> getImageElementList() {
        return imageElementList;
    }

    public void setImageElementList(List<ImageElement> imageElementList) {
        this.imageElementList = imageElementList;
    }

    public List<VideoElement> getVideoElementList() {
        return videoElementList;
    }

    public void setVideoElementList(List<VideoElement> videoElementList) {
        this.videoElementList = videoElementList;
    }

    public List<AudioElement> getAudioElementList() {
        return audioElementList;
    }

    public void setAudioElementList(List<AudioElement> audioElementList) {
        this.audioElementList = audioElementList;
    }
}
