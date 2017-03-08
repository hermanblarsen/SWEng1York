package utilities;

import exceptions.SequenceNotFoundException;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.Text;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by habl on 23/02/2017.
 */
public class Slide extends StackPane {
    Logger logger = LoggerFactory.getLogger(Slide.class);

    protected List<SlideElement> slideElementList;
    protected List<SlideElement> visibleSlideElementList;
    protected List<TextElement> textElementList = new ArrayList<>();
    protected List<GraphicElement> graphicElementList = new ArrayList<>();
    protected List<VideoElement> videoElementList = new ArrayList<>();

    private static final int START_SEARCH = 0;
    private static final int END_SEARCH = 1;

    protected int slideID;

    //Current Sequence number on slide
    int currentSequence = 0;
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
        //Sort by Sequence
        sortElementsByStartSequence(slideElementList);
        //Set Max Sequence number
        maxSequenceNumber = getMaxSequenceNumber(slideElementList);

        //Create specific arraylists
        for (SlideElement slideElement : slideElementList) {
            if (slideElement instanceof TextElement) textElementList.add((TextElement) slideElement);
            if (slideElement instanceof GraphicElement) graphicElementList.add((GraphicElement) slideElement);
            if (slideElement instanceof VideoElement) videoElementList.add((VideoElement) slideElement);
        }
    }

    private int getMaxSequenceNumber(List<SlideElement> slideElementList) {
        int max = 0;

        for (int i = 1; i < slideElementList.size(); i++) {
            if (slideElementList.get(i).getStartSequence() > max) {
                max = slideElementList.get(i).getStartSequence();
            }

            if (slideElementList.get(i).getEndSequence() > max) {
                max = slideElementList.get(i).getEndSequence();
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

    public void retard(){
        System.out.println("Lol");
    }

    public void advance() {
        SlideElement checkInVisibleSet;
        //If we're going forwards and still elements left. If we're going backwards but not at element 0
        if (currentSequence < maxSequenceNumber) {
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
                logger.error("Failed to find Element with Sequence number of " + currentSequence + " in slideElementList. XML invalid.");
                return;
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
        } else {
            //Do something to tell the class above us that we're done and to go next slide/Previous
        }
    }

    private void sortElementsByStartSequence(List<SlideElement> slideElementListToSort) {
        //Sort by Layer
        Collections.sort(slideElementListToSort, (element1, element2) -> {
            if (element1.getStartSequence() == element2.getStartSequence())
                return 0;
            return element1.getStartSequence() < element2.getStartSequence() ? -1 : 1;
        });
    }

    private void sortElementsByLayer(List<SlideElement> slideElementListToSort) {
        //Sort by Layer
        Collections.sort(slideElementListToSort, (element1, element2) -> {
            if (element1.getLayer() == element2.getLayer())
                return 0;
            return element1.getLayer() < element2.getLayer() ? -1 : 1;
        });
    }


    /**
     * Searches for currentSequence number in either start sequence or end sequence field. Used to determine whether to add an element to the visible
     * set. Throws not found exception if cant find a start or end sequence that matches.
     * @param toSearch List of Slide elements to search
     * @param sequence Sequence number to search for in list (start or end)
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
                if(toReturnStart == null) return toReturnEnd;
                else return toReturnStart;

            case END_SEARCH:
                if(toReturnEnd == null) return toReturnStart;
                else return toReturnEnd;
        }

        //We should never hit this. Satisfy JVM.
        return null;
    }
}
