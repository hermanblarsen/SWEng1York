package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.exceptions.SequenceNotFoundException;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by habl on 23/02/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Slide extends Pane {
    Logger logger = LoggerFactory.getLogger(Slide.class);

    protected List<SlideElement> slideElementList;

    public List<SlideElement> getVisibleSlideElementList() {
        return visibleSlideElementList;
    }

    protected List<SlideElement> visibleSlideElementList;
    protected List<TextElement> textElementList = new ArrayList<>();
    protected List<GraphicElement> graphicElementList = new ArrayList<>();
    protected List<ImageElement> imageElementList = new ArrayList<>();
    protected List<VideoElement> videoElementList = new ArrayList<>();
    protected List<AudioElement> audioElementList = new ArrayList<>();

    //Passed back up to Presentation layer, to alert on whether sequence has changed the current slide or not
    public static final int SLIDE_NO_MOVE = 0;
    public static final int SLIDE_FORWARD = 1;
    public static final int SLIDE_BACKWARD = 2;
    public static final int SLIDE_PRE_CHANGE = 3;

    protected int slideID;


    public int getCurrentSequenceNumber() {
        return currentSequenceNumber;
    }

    public void setCurrentSequenceNumber(int currentSequenceNumber) {
        this.currentSequenceNumber = currentSequenceNumber;
    }

    int currentSequenceNumber = 0;
    int maxSequenceNumber = 0;



    public Slide() {
        slideElementList = new ArrayList<>();
        visibleSlideElementList = new ArrayList<>();
        //this.setPickOnBounds(false);
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

    public int getMaxSequenceNumber() {
        return maxSequenceNumber;
    }

    public void setMaxSequenceNumber(int maxSequenceNumber) {
        this.maxSequenceNumber = maxSequenceNumber;
    }

    public List<SlideElement> getSlideElementList() {
        return slideElementList;
    }

    public void setSlideElementList(List<SlideElement> slideElementList) {
        this.slideElementList = slideElementList;

        //Set Max Sequence number
        maxSequenceNumber = getListMaxSequenceNumber(slideElementList);

        //Add slideElements to specific arraylists
        for (SlideElement slideElement : slideElementList) {
            if (slideElement instanceof TextElement) textElementList.add((TextElement) slideElement);
            if (slideElement instanceof GraphicElement) graphicElementList.add((GraphicElement) slideElement);
            if (slideElement instanceof ImageElement) imageElementList.add((ImageElement) slideElement);
            if (slideElement instanceof VideoElement) videoElementList.add((VideoElement) slideElement);
            if (slideElement instanceof AudioElement) audioElementList.add((AudioElement) slideElement);
        }
    }

    private static int getListMaxSequenceNumber(List<SlideElement> slideElementList) {
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

    public static void sortElementsByLayer(List<SlideElement> slideElementListToSort) {
        //Sort by Layer
        slideElementListToSort.sort((element1, element2) -> {
            if (element1.getLayer() == element2.getLayer())
                return 0;
            return element1.getLayer() < element2.getLayer() ? -1 : 1;
        });
    }

    /**
     * Searches for currentSequence number in either start sequence or end sequence field. Used to determine whether to add elements to the visible
     * set. Throws not found exception if cant find elements with a start or end sequence that matches.
     *
     * @param toSearch   List of Slide elements to search
     * @param sequence   Sequence number to search for in list (start or end)
     * @return Returns ArrayList of slideElements corresponding to elements with desired sequence number
     * @throws SequenceNotFoundException If a sequence number cant be found, there is most likely an error
     */
    public static ArrayList<SlideElement> searchForSequenceElement(List<SlideElement> toSearch, int sequence) throws SequenceNotFoundException {
        ArrayList<SlideElement> toReturn = new ArrayList<>();

        //Search simulateously for start and end sequence elements
        for (SlideElement slideElement : toSearch) {
            if (slideElement.getStartSequence() ==sequence  || slideElement.getEndSequence() == sequence) {
                toReturn.add(slideElement);
            }
        }

        //We cant find an element in SlideElementList with correct sequenceNumber. An error has occurred.
        if (toReturn.isEmpty()) {
            throw new SequenceNotFoundException();
        }

        //Return elements that satisfy the sequence number
        return toReturn;
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
