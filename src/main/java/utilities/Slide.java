package utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by habl on 23/02/2017.
 */
public class Slide {

    protected List<SlideElement> slideElementList;
    protected int slideID;


    public Slide () {
        slideElementList = new ArrayList<SlideElement>();
    }

    public void addElement(int elementIndex, SlideElement newElement) {
        this.slideElementList.add(elementIndex, newElement);
    }
    public void addElement(SlideElement newElement) {
        this.slideElementList.add(this.slideElementList.size(), newElement);
    }


    public void deleteElementWithIndex(int elementIndex) {

    }
    public void deleteElementWithID(int elementID) {

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

}
