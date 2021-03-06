package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.exceptions.SequenceNotFoundException;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by habl on 23/02/2017.
 */

/**
 * Slide element storing slide elements
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Slide extends Pane {
    Logger logger = LoggerFactory.getLogger(Slide.class);

    protected List<SlideElement> slideElementList;
    protected List<SlideElement> visibleSlideElementList;
    protected List<TextElement> textElementList = new ArrayList<>();
    protected List<InteractiveElement> interactiveElementList = new ArrayList<>();

    private ArrayList<WritableImage> slideDrawings;
    private int drawingIndex;
    private String userComments;

    //Passed back up to Presentation layer, to alert on whether sequence has changed the current slide or not
    public static final int SLIDE_NO_MOVE = 0;
    public static final int SLIDE_FORWARD = 1;
    public static final int SLIDE_BACKWARD = 2;
    public static final int SLIDE_PRE_CHANGE = 3;
    protected int slideID;

    int currentSequenceNumber = 0;
    int maxSequenceNumber = 0;


    public Slide() {
        slideElementList = new ArrayList<>();
        visibleSlideElementList = new ArrayList<>();
        slideDrawings = new ArrayList<>();
        slideDrawings.add(new WritableImage(1, 1));
        drawingIndex = 0;
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

    public SlideElement getElementWithID(int id){
        return slideElementList.stream().filter(item->item.getElementID() == id).findFirst().get();
    }

    public void moveElementToIndex(int index) {

    }

    public int getCurrentSequenceNumber() {
        return currentSequenceNumber;
    }

    public void setCurrentSequenceNumber(int currentSequenceNumber) {
        this.currentSequenceNumber = currentSequenceNumber;
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
            if(slideElement instanceof InteractiveElement) interactiveElementList.add((InteractiveElement) slideElement);
        }
    }

    /**
     * Get the maximum sequence number of start and end sequence on a slide
     * @param slideElementList slideElement list to check
     * @return maximum sequence
     */
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

    /**
     * Sort elements on the slide by layer
     * @param slideElementListToSort list to sort by layer
     */
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
     * @param toSearch List of Slide elements to search
     * @param sequence Sequence number to search for in list (start or end)
     * @return Returns ArrayList of slideElements corresponding to elements with desired sequence number
     * @throws SequenceNotFoundException If a sequence number cant be found, there is most likely an error
     */
    public static ArrayList<SlideElement> searchForSequenceElement(List<SlideElement> toSearch, int sequence) throws SequenceNotFoundException {
        ArrayList<SlideElement> toReturn = new ArrayList<>();

        //Search simulateously for start and end sequence elements
        for (SlideElement slideElement : toSearch) {
            if (slideElement.getStartSequence() == sequence || slideElement.getEndSequence() == sequence) {
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

    public List<SlideElement> getVisibleSlideElementList() {
        return visibleSlideElementList;
    }

    public WritableImage getCurrentSlideDrawing() { return slideDrawings.get(drawingIndex); }

    public WritableImage getPreviousSlideDrawing() {
        if(drawingIndex > 0)
            return slideDrawings.get(--drawingIndex);
        else
            return  slideDrawings.get(drawingIndex);
    }

    /**
     * Get the next slide drawing
     * @return drawing
     */
    public WritableImage getNextSlideDrawing() {
        if(drawingIndex < slideDrawings.size() - 1)
            return slideDrawings.get(++drawingIndex);
        else
            return slideDrawings.get(drawingIndex);
    }

    /**
     * Add drawings to slide
     * @param slideDrawing the drawing
     */
    public void addSlideDrawing(WritableImage slideDrawing) {
        ListIterator<WritableImage> iterator = slideDrawings.listIterator(drawingIndex + 1);
        while(iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        slideDrawings.add(slideDrawing);
        drawingIndex++;
    }

    /**
     * "Destroys" all of the visible elements on this slide.  Should be called whenever a slide is no longer visible so that
     *  all elements are given the opportunity to clean up anything non-visual.
     */
    public void destroyAllVisible(){
        for(SlideElement element : visibleSlideElementList){
            element.destroyElement();
        }
    }

    public String getUserComments() {
        return userComments;
    }

    public void setUserComments(String userComments) {
        this.userComments = userComments;
    }

    public List<InteractiveElement> getInteractiveElementList() {
        return interactiveElementList;
    }
}

