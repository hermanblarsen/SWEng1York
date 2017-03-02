package utilities;

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
    protected int slideID;
    boolean isDone = false;


    public Slide () {
        slideElementList = new ArrayList<SlideElement>();
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

    public boolean isDone() {
        return isDone;
    }

    public void moveElementToIndex (int index) {

    }

    public List<SlideElement> getSlideElementList() {
        return slideElementList;
    }

    public void setSlideElementList(List<SlideElement> slideElementList) {
        this.slideElementList = slideElementList;
    }

    public int getSlideID() {
        return slideID;
    }

    public void setSlideID(int slideID) {
        this.slideID = slideID;
    }

    public void start(){
        logger.debug("Beginning animation sequence for SlideID: %d", slideID);
        //Sort by Sequence
        sortElementsBySequence();
        //Sort by Layer
        //sortElementsByLayer();
        //Render Sequence Loop
        for(SlideElement slideElement : slideElementList){
            slideElement.renderElement(0);
        }
        setSlideDone();
    }

    private void sortElementsBySequence() {
        //Sort by Layer
        Collections.sort(slideElementList, (o1, o2) -> {
            if(o1.getStartSequence() == o2.getStartSequence())
                return 0;
            return o1.getStartSequence() < o2.getStartSequence() ? -1 : 1;
        });
    }

    private void setSlideDone() {
        isDone = true;
    }

    private void sortElementsByLayer() {
        //Sort by Layer
        Collections.sort(slideElementList, (o1, o2) -> {
            if(o1.getLayer() == o2.getLayer())
                return 0;
            return o1.getLayer() < o2.getLayer() ? -1 : 1;
        });
    }

}
