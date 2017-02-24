package utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by habl on 23/02/2017.
 */
public class Slide {

    private List<SlideElement> slideElementList;

    public Slide () {
        slideElementList = new ArrayList<SlideElement>();
    }

    public void addElement(int elementIndex, SlideElement newElement) {
        this.slideElementList.add(elementIndex, newElement);
    }
    public void addElement(SlideElement newElement) {
        this.slideElementList.add(this.slideElementList.size(), newElement);
    }


    public void deleteElementIndex(int elementIndex) {

    }
    public void deleteElementID(int elementID) {

    }

}
