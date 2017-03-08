package utilities;

import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by habl on 23/02/2017.
 */
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

    private List<Slide> slideList;


    public Presentation() {
        slideList = new ArrayList<Slide>();
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
        if(slideList.size() > 0) currentSlide = slideList.get(0); //Set First Slide
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

    /**
     * Start the animation sequence for Presentation
     *
     * @author Amrik Sadhra
     */
    public void start() {

    }

    public Slide advance() {
        if (currentSlideNumber != maxSlideNumber) {
            currentSlideNumber++;
        } else {
            logger.info("Reached end of Presentation. No more Slides in Presentation ArrayList");
        }

        return currentSlide = slideList.get(currentSlideNumber);
    }
}
