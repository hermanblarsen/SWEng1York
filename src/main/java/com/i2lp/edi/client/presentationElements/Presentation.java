package com.i2lp.edi.client.presentationElements;


import com.i2lp.edi.client.utilities.Theme;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.i2lp.edi.client.Constants.PRESENTATIONS_PATH;
import static com.i2lp.edi.client.Constants.THUMBNAIL_WIDTH;

/**
 * Created by habl on 23/02/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Presentation extends Pane {

    protected static Logger logger = LoggerFactory.getLogger(Presentation.class);
    private String title;
    private String documentID;
    private String author;
    private Float version;
    private Float documentAspectRatio;
    private String description;
    private String tags;
    private Theme theme;
    private String subject;
    private boolean isI2lpFormat;
    private ArrayList<String> xmlFaults = null;

    private boolean isAutoplayPresetation = false;


    private int maxSlideNumber = 0;

    private Slide currentSlide;
    private boolean autoplayMedia;

    private int groupFormat;

    public static final int PRESENTATION_START = 0;
    public static final int PRESENTATION_FINISH = 1;
    public static final int SLIDE_CHANGE = 2;
    public static final int SAME_SLIDE = 3;
    public static final int SLIDE_LAST_ELEMENT = 4;

    private List<Slide> slideList;


    //---------- Getters and setters required for presentation sequencing ---------------
    public int getMaxSlideNumber() {
        return maxSlideNumber;
    }

    public void setMaxSlideNumber(int maxSlideNumber) {
        this.maxSlideNumber = maxSlideNumber;
    }

    public Presentation() {
        slideList = new ArrayList<>();
        this.theme = new Theme();
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

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }

    public void setSubject(String subject) { this.subject = subject; }

    public static Presentation generateTestPresentation() {
        ArrayList<Slide> slides = new ArrayList<>();

        Slide slide1 = new Slide();
        slide1.setSlideID(1);
        slides.add(slide1);

        //Create some test Slide Elements
        ArrayList<SlideElement> slideElementsSlide1 = new ArrayList<>();

        //Create a test Text element, add some text and pop it onto our stack pane. This code will all be driven from XML parser
        TextElement myTextElement = new TextElement();
        myTextElement.setLayer(0);
        myTextElement.setStartSequence(1);
        myTextElement.setEndSequence(3);
        myTextElement.setDuration(1);
        myTextElement.setElementID(0);
        myTextElement.setFont("Arial");
        myTextElement.setFontSize(12);
        myTextElement.setFontColour("#AF4567");
        myTextElement.setBgColour("#000000");
        //myTextElement.setBgColour("#000000");
        myTextElement.setBorderSize(2);
        myTextElement.setBorderColour("#000000");
        myTextElement.setTextContent("<b>IILP HTML Support Test</b>");
        myTextElement.setSlideCanvas(slide1);
        slideElementsSlide1.add(myTextElement);

        //Create a test Text element, add some text and pop it onto our stack pane. This code will all be driven from XML parser
        TextElement myTextElement1 = new TextElement();
        myTextElement1.setLayer(0);
        myTextElement1.setStartSequence(1);
        myTextElement1.setEndSequence(3);
        myTextElement1.setDuration(0.5f);
        myTextElement1.setElementID(9);
        myTextElement1.setFont("Arial");
        myTextElement1.setFontSize(12);
        myTextElement1.setFontColour("#AF4567");
        myTextElement1.setBgColour("#000000");
        myTextElement1.setBorderSize(2);
        myTextElement1.setBorderColour("#000000");
        myTextElement1.setTextContent("<b>Same starting sequence</b>");
        myTextElement1.setSlideCanvas(slide1);
        slideElementsSlide1.add(myTextElement1);

        GraphicElement myGraphicElement2 = new GraphicElement();
        myGraphicElement2.setLayer(1);
        myGraphicElement2.setStartSequence(3);
        myGraphicElement2.setEndSequence(5);
        myGraphicElement2.setDuration(0.5f);
        myGraphicElement2.setFillColour("#00000000");
        myGraphicElement2.setLineColour("#00FF00FF");
        myGraphicElement2.setOvalYPosition(0.5f);
        myGraphicElement2.setOvalXPosition(0.5f);
        myGraphicElement2.setrHorizontal(0.2f);
        myGraphicElement2.setrVertical(0.1f);
        myGraphicElement2.setRotation(0);
        myGraphicElement2.setPolygon(false);
        myGraphicElement2.setSlideCanvas(slide1);
        slideElementsSlide1.add(myGraphicElement2);

        GraphicElement myGraphicElement = new GraphicElement();
        myGraphicElement.setLayer(2);
        myGraphicElement.setStartSequence(2);
        myGraphicElement.setEndSequence(5);
        myGraphicElement.setDuration(1);
        myGraphicElement.setFillColour("#EEFFFFFF");
        myGraphicElement.setLineColour("#0000FFFF");
        myGraphicElement.polySetXPoints(new float[] {0.1f, 0.5f, 0.5f});
        myGraphicElement.polySetYPoints(new float[] {0.1f, 0.1f, 0.6f});
        myGraphicElement.setPolygon(true);
        myGraphicElement.setClosed(true);
        myGraphicElement.setSlideCanvas(slide1);
        slideElementsSlide1.add(myGraphicElement);


        GraphicElement myGraphicElement3 = new GraphicElement();
        myGraphicElement3.setLayer(3);
        myGraphicElement3.setStartSequence(6);
        myGraphicElement3.setEndSequence(8);
        myGraphicElement3.setDuration(0.5f);
        myGraphicElement3.setFillColour("#FF0000FF");
        myGraphicElement3.setLineColour("#0000FFFF");
        myGraphicElement3.polySetXPoints(new float[] {0.5f, 0.5f, 0.4f});
        myGraphicElement3.polySetYPoints(new float[] {0.2f, 0.8f, 0.3f});
        myGraphicElement3.setPolygon(true);
        myGraphicElement3.setClosed(true);
        myGraphicElement3.setSlideCanvas(slide1);
        slideElementsSlide1.add(myGraphicElement3);


        TextElement myTextElement11 = new TextElement();
        myTextElement11.setLayer(2);
        myTextElement11.setStartSequence(4);
        myTextElement11.setEndSequence(7);
        myTextElement11.setDuration(0.5f);
        myTextElement11.setElementID(5);
        myTextElement11.setFont("Times New Roman");
        myTextElement11.setFontSize(24);
        myTextElement11.setFontColour("#AA4567");
        myTextElement11.setBgColour("#000000");
        myTextElement11.setTextContent("<b>This is some sample text for Adar to be impressed by</b>");
        myTextElement11.setSlideCanvas(slide1);
        slideElementsSlide1.add(myTextElement11);


        PollElement poll = new PollElement();
        List<String> answers = new ArrayList<>();
        answers.add(0,"ANS 1");
        answers.add(1,"ANS 2");
        answers.add(2,"ANS 3");
        answers.add(3,"ANS 4");
        poll.setPossibleAnswers(answers);
        poll.setPollQuestion("HERE IS A SAMPLE QUESTION!");
        poll.setTimeLimit(15);

        poll.setLayer(6);
        poll.setStartSequence(9);
        poll.setEndSequence(10);
        poll.setDuration(1);
        poll.setElementID(111111);
        poll.setSlideCanvas(slide1);
        slideElementsSlide1.add(poll);

        WordCloudElement myWordCloudElement = new WordCloudElement();
        myWordCloudElement.setTask("Enter Words about Edi");
        myWordCloudElement.setTimeLimit(10);
        myWordCloudElement.setLayer(7);
        myWordCloudElement.setStartSequence(11);
        myWordCloudElement.setEndSequence(12);
        myWordCloudElement.setDuration(10);
        //myWordCloudElement.setCloudShapePath("file:/projectResources/logos/ediLogo400x400.png");
        myWordCloudElement.setElementID(123456);
        myWordCloudElement.setSlideCanvas(slide1);
        slideElementsSlide1.add(myWordCloudElement);

        slide1.setSlideElementList(slideElementsSlide1);


        Slide slide2 = new Slide();
        slide2.setSlideID(2);
        slides.add(slide2);

        //Create some test Slide Elements
        ArrayList<SlideElement> slideElementsSlide2 = new ArrayList<>();

        //Create a test Text element, add some text and pop it onto our stack pane. This code will all be driven from XML parser
        TextElement myTextElementNewSlide = new TextElement();
        myTextElementNewSlide.setLayer(2);
        myTextElementNewSlide.setStartSequence(2);
        myTextElementNewSlide.setEndSequence(4);
        myTextElementNewSlide.setDuration(1);
        myTextElementNewSlide.setElementID(69);
        myTextElementNewSlide.setTextContent("<b>Slide2</b>");
        myTextElementNewSlide.setSlideCanvas(slide2);
        slideElementsSlide2.add(myTextElementNewSlide);

        VideoElement myVideoElement = new VideoElement();
        myVideoElement.setPath("projectResources/sampleFiles/prometheus.mp4");
        myVideoElement.setAutoplay(true);
        myVideoElement.setMediaControl(true);
        myVideoElement.setLoop(false);
        myVideoElement.setStartTime(Duration.seconds(0));
        myVideoElement.setEndTime(Duration.seconds(30));
        myVideoElement.setElementAspectRatio(1.777777f);
        myVideoElement.setAspectRatioLock(false);
        myVideoElement.setxSize(0.300f);
        myVideoElement.setySize(0.3f);
        myVideoElement.setxPosition(0.2f);
        myVideoElement.setyPosition(0.2f);
        myVideoElement.setLayer(1);
        myVideoElement.setStartSequence(1);
        myVideoElement.setEndSequence(3);
        myVideoElement.setDuration(0.5f);
        myVideoElement.setSlideCanvas(slide2);
        slideElementsSlide2.add(myVideoElement);

        slide2.setSlideElementList(slideElementsSlide2);


        Slide slide3 = new Slide();
        slide3.setSlideID(3);
        slides.add(slide3);

        //Create some test Slide Elements
        ArrayList<SlideElement> slideElementsSlide3 = new ArrayList<>();

        //Create a test Text element, add some text and pop it onto our stack pane. This code will all be driven from XML parser
        TextElement myTextElementNewSlide2 = new TextElement();
        myTextElementNewSlide2.setLayer(1);
        myTextElementNewSlide2.setStartSequence(1);
        myTextElementNewSlide2.setEndSequence(2);
        myTextElementNewSlide2.setDuration(1);
        myTextElementNewSlide2.setElementID(124214);
        myTextElementNewSlide2.setTextContent("<b>Slide3</b>");
        myTextElementNewSlide2.setSlideCanvas(slide3);
        slideElementsSlide3.add(myTextElementNewSlide2);

        AudioElement audioElement = new AudioElement();
        audioElement.setLayer(0);
        audioElement.setElementID(1);
        audioElement.setStartSequence(2);
        audioElement.setEndSequence(3);
        audioElement.setDuration(5);
        audioElement.setPath("projectResources/sampleFiles/example.mp3");
        audioElement.isLoop(false);
        audioElement.isAutoPlay(false);
        audioElement.setStartTime(Duration.seconds(0));
        audioElement.setEndTime(Duration.seconds(5));
        slideElementsSlide3.add(audioElement);

        slide3.setSlideElementList(slideElementsSlide3);

        Presentation myPresentation = new Presentation();
        myPresentation.setSlideList(slides);
        myPresentation.setDocumentAspectRatio((float) 4/3);
        Theme theme = new Theme();
        theme.setBackgroundColour("#2ECC71FF");
        myPresentation.setTheme(theme);

        return myPresentation;
    }

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
        return isAutoplayPresetation;
    }

    public void setAutoplayPresetation(boolean autoplayPresetation) {
        isAutoplayPresetation = autoplayPresetation;
    }

    public ImageView getSlidePreview(int slideNumber, double thumbnailWidth) {
        ImageView preview;
        File thumbnailFile = new File(PRESENTATIONS_PATH + getDocumentID() + "/Thumbnails/" + "slide" + slideNumber + "_thumbnail.png");

        if (thumbnailFile.exists()) {
            try {
                preview = new ImageView("file:" + PRESENTATIONS_PATH + getDocumentID() + "/Thumbnails/" + "slide" + slideNumber + "_thumbnail.png");
                Rectangle2D viewport = new Rectangle2D(0, 0, THUMBNAIL_WIDTH, THUMBNAIL_WIDTH/getDocumentAspectRatio());
                preview.setViewport(viewport); //TODO: Move creating ImageView for thumbnails to a separate method and use it in PreviewPanel
            } catch (NullPointerException | IllegalArgumentException e) {
                logger.debug("Couldn't open thumbnail" + thumbnailFile.toString());
                preview = new ImageView("file:projectResources/icons/emptyThumbnail.png");
            }
        } else {
            preview = new ImageView("file:projectResources/icons/emptyThumbnail.png");
        }

        preview.setFitWidth(thumbnailWidth);
        preview.setPreserveRatio(true);
        preview.setSmooth(true);
        preview.setCache(true);

        return preview;
    }
}
