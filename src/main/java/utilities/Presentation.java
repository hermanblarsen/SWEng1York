package utilities;


import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by habl on 23/02/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Presentation extends Pane {
    private String documentID;
    private String author;
    private Float version;
    private Float documentAspectRatio;
    private String description;
    private String tags;
    private Theme theme;

    private int currentSlideNumber = 0;
    private int maxSlideNumber;
    private Slide currentSlide;
    private boolean autoplayMedia;

    private int groupFormat;
    Logger logger = LoggerFactory.getLogger(Presentation.class);
    public static final int PRESENTATION_START = 0;
    public static final int PRESENTATION_FINISH = 1;
    public static final int SLIDE_CHANGE = 2;
    public static final int SAME_SLIDE = 3;

    private List<Slide> slideList;


    public Presentation() {
        slideList = new ArrayList<>();
        this.theme = new Theme();
    }

    public void addSlide(int slideIndex, Slide newSlide) {
        this.slideList.add(slideIndex, newSlide);
    }

    public void addSlide(Slide newSlide) {
        this.slideList.add(this.slideList.size(), newSlide);
    }

    public void deleteSlideIndex(int slideIndex) {

    }

    public void deleteSlideID(int slideID) {

    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Float getVersion() {
        return version;
    }

    public void setVersion(Float version) {
        this.version = version;
    }

    public Float getDocumentAspectRatio() {
        return documentAspectRatio;
    }

    public void setDocumentAspectRatio(Float documentAspectRatio) {
        this.documentAspectRatio = documentAspectRatio;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public boolean isAutoplayMedia() {
        return autoplayMedia;
    }

    public void setAutoplayMedia(boolean autoplayMedia) {
        this.autoplayMedia = autoplayMedia;
    }

    public int getGroupFormat() {
        return groupFormat;
    }

    public void setGroupFormat(int groupFormat) {
        this.groupFormat = groupFormat;
    }

    public List<Slide> getSlideList() {
        return slideList;
    }

    public void setSlideList(List<Slide> slideList) {
        this.slideList = slideList;
        maxSlideNumber = slideList.size();
        if (slideList.size() > 0) currentSlide = slideList.get(0); //Set First Slide
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Slide getCurrentSlide() {
        return slideList.get(currentSlideNumber);
    }

    public int advance(int direction) {
        //Initialise this with something more appropriate
        int presentationStatus = SAME_SLIDE;

        if (direction == Slide.SLIDE_FORWARD) {
            //If we're not at end of presentation
            if (currentSlideNumber < maxSlideNumber) {
                //If slide tells you to move forward to next slide, do it by changing to next slide in slide list.
                if (currentSlide.advance(direction) == direction) {
                    currentSlideNumber++;

                    if (currentSlideNumber >= maxSlideNumber - 1) {
                        logger.info("Reached final slide: " + maxSlideNumber);
                        currentSlideNumber = maxSlideNumber - 1; //Wrap to this slide as maximum
                        presentationStatus = PRESENTATION_FINISH;
                    } else {
                        presentationStatus = SLIDE_CHANGE;
                    }

                    currentSlide = slideList.get(currentSlideNumber);
                }
            }
        } else if (direction == Slide.SLIDE_BACKWARD) {
            //If we're not at start of presentation
            if (currentSlideNumber >= 0) {
                //If slide tells you to move backward to prev slide, do it by changing to prev slide in slide list.
                //Allow slideElements to play on slide though.
                if (currentSlide.advance(direction) == direction) {
                    currentSlideNumber--;

                    if (currentSlideNumber < 0) {
                        logger.info("Reached Min slide number. Presentation back at start.");
                        currentSlideNumber = 0;//Wrap to this slide as minimum
                        presentationStatus = PRESENTATION_START;
                    } else {
                        presentationStatus = SLIDE_CHANGE;
                    }

                    currentSlide = slideList.get(currentSlideNumber);
                }
            }
        }
        return presentationStatus;
    }
}
