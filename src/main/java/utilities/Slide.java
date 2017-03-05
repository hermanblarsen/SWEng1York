package utilities;

import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by habl on 23/02/2017.
 */
public class Slide extends StackPane {
    Logger logger = LoggerFactory.getLogger(Slide.class);

    protected List<SlideElement> slideElementList;
    protected List<SlideElement> visibleSlideElementsList;
    protected int slideID;

    //Current Sequence number on slide
    int currentSequence = 0;
    int maxSequenceNumber = 4;

    public Slide() {
        slideElementList = new ArrayList<>();
        visibleSlideElementsList = new ArrayList<>();

        this.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                this.advance();
            }
        });
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
    }

    public int getSlideID() {
        return slideID;
    }

    public void setSlideID(int slideID) {
        this.slideID = slideID;
    }

    public void advance() {
        if (currentSequence != maxSequenceNumber) {
            visibleSlideElementsList.add(slideElementList.get(currentSequence));

            //Sort by Layer
            sortElementsByLayer(visibleSlideElementsList);

            currentSequence++;
            logger.info("Current Sequence is " + currentSequence);

            //Fire animations
            for (SlideElement elementToAnimate : visibleSlideElementsList) {

                //TODO can we make this independent of the interface, or else we have to implement things in audio which is not needed.
                //TODO might be worth looking into reflectance, although this will make it more resource hungry..
                if (elementToAnimate.getStartSequence() == currentSequence) {
                    elementToAnimate.renderElement(Animation.ENTRY_ANIMATION); //Entry Sequence
                } else if (elementToAnimate.getEndSequence() == currentSequence) {
                    elementToAnimate.renderElement(Animation.EXIT_ANIMATION); //Exit Sequence
                } else {
                    elementToAnimate.renderElement(Animation.NO_ANIMATION); //No animation, just render
                }
            }
        } else {
            //Do something to tell the class above us that we're done
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

}
