package com.i2lp.edi.client.presentationElements;


import com.i2lp.edi.client.dashboard.DashModule;
import com.i2lp.edi.client.dashboard.PresentationPanel;
import com.i2lp.edi.client.dashboard.Subject;
import com.i2lp.edi.client.utilities.Theme;
import com.i2lp.edi.server.packets.PresentationMetadata;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.i2lp.edi.client.Constants.PRESENTATIONS_PATH;

/**
 * Created by habl on 23/02/2017.
 */

/**
 * Presentation element, storing slides which again stores slide elements
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Presentation extends Pane {
    protected static Logger logger = LoggerFactory.getLogger(Presentation.class);

    //From i2lp:
    private PresentationMetadata presentationMetadata;
    private String documentTitle;
    private int currentSlideNumber;
    private String documentFilePath;
    private LocalDateTime goLiveDateTime;
    private boolean hasThumbnails = true;

    //From Schema
    private String documentID;
    private String author;
    private Float version;
    private Float documentAspectRatio;
    private String description;
    private String tags;
    private Theme theme;
    private boolean isI2lpFormat;
    private ArrayList<String> xmlFaults = null;

    private boolean autoplayPresentation = false;

    private int maxSlideNumber = 0;

    private Slide currentSlide;
    private boolean autoplayMedia;

    private DashModule module; //DashModule to which this presentation belongs
    private PresentationPanel presPanel;

    private int groupFormat;

    public static final int PRESENTATION_START = 0;
    public static final int PRESENTATION_FINISH = 1;
    public static final int SLIDE_CHANGE = 2;
    public static final int SAME_SLIDE = 3;
    public static final int SLIDE_LAST_ELEMENT = 4;
    public static final int SLIDE_LAST_ELEMENT_ALONE = 5;

    private List<Slide> slideList;

    public Presentation() {
        slideList = new ArrayList<>();
        this.theme = new Theme();
    }

    //---------- Getters and setters required for presentation sequencing ---------------
    public int getMaxSlideNumber() {
        return maxSlideNumber;
    }

    public void setMaxSlideNumber(int maxSlideNumber) {
        this.maxSlideNumber = maxSlideNumber;
    }

    public void setCurrentSlide(Slide currentSlide) {
        this.currentSlide = currentSlide;
    }

    public Slide getSlide(int currentSlideNumber) {
        return slideList.get(currentSlideNumber);
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

    public String getDocumentTitle() { return documentTitle; }

    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }

    public Subject getSubject() { return module.getSubject(); }

    public void setI2lpFormat(boolean i2lpFormat) {
        isI2lpFormat = i2lpFormat;
    }

    public boolean isI2lpFormat() {
        return isI2lpFormat;
    }

    public ArrayList<String> getXmlFaults() {
        return xmlFaults;
    }

    public void setXmlFaults(ArrayList<String> xmlFaults) {
        this.xmlFaults = xmlFaults;
    }

    public boolean isAutoplayPresentation() {
        return autoplayPresentation;
    }

    public void setAutoplayPresentation(boolean autoplayPresentation) {
        this.autoplayPresentation = autoplayPresentation;
    }

    public ImageView getSlidePreview(int slideNumber, double thumbnailWidth) {
        ImageView preview;
        File thumbnailFile = new File(PRESENTATIONS_PATH + File.separator + getModule().getModuleName() + File.separator + getDocumentID() + "/Thumbnails/" + "slide" + slideNumber + "_thumbnail.png");

        if (thumbnailFile.exists()) {
            try {
                preview = new ImageView("file:" + PRESENTATIONS_PATH + File.separator + getModule().getModuleName() + File.separator + getDocumentID() + "/Thumbnails/" + "slide" + slideNumber + "_thumbnail.png");
            } catch (NullPointerException | IllegalArgumentException e) {
                logger.debug("Couldn't open thumbnail" + thumbnailFile.toString());
                hasThumbnails = false;
                preview = new ImageView("file:projectResources/icons/emptyThumbnail.png");
            }
        } else {
            hasThumbnails = false;
            preview = new ImageView("file:projectResources/icons/emptyThumbnail.png");
        }

        preview.setFitWidth(thumbnailWidth);
        preview.setPreserveRatio(true);
        preview.setSmooth(true);
        preview.setCache(true);

        return preview;
    }

    public Slide getCurrentSlide() {
        return currentSlide;
    }

    public String getPath() { return documentFilePath; }

    public void setPath(String path) { this.documentFilePath = path; }

    public DashModule getModule() { return module; }

    public void setModule(DashModule module) { this.module = module; }

    public void setPresentationMetadata(PresentationMetadata presentationMetadata) {
        this.presentationMetadata = presentationMetadata;
        try {
            goLiveDateTime = presentationMetadata.getGoLiveTimestamp().toLocalDateTime();
        } catch (NullPointerException e) {
            //Exception will be thrown if goLiveDateTime is not specified. Do nothing.
        }
    }

    public PresentationMetadata getPresentationMetadata() {
        return presentationMetadata;
    }

    public void setGoLiveDate(LocalDateTime goLiveDateTime) {
        this.goLiveDateTime = goLiveDateTime;
        presentationMetadata.setGoLiveTimestamp(goLiveDateTime);
    }

    public LocalDateTime getGoLiveDateTime() { return goLiveDateTime; }

    public boolean hasThumbnails() { return hasThumbnails; }

    public void setPresPanel(PresentationPanel presPanel) { this.presPanel = presPanel; }

    public void setLive(boolean isLive) {
        this.presentationMetadata.setLive(isLive);
        if (presPanel != null) {
            if (presPanel.isLive() != isLive) {
                presPanel.setLive(isLive);
            }
        }
    }

    public boolean isLive() { return this.presentationMetadata.getLive(); }
}
