package client.presentationElements;


import client.utilities.OvalBuilder;
import client.utilities.PolygonBuilder;
import client.utilities.Theme;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
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
    private boolean isI2lpFormat;
    private ArrayList<String> xmlFaults = null;


    private int currentSlideNumber = 0;
    private int maxSlideNumber = 0;
    private Slide currentSlide;
    private boolean autoplayMedia;

    private int groupFormat;
    Logger logger = LoggerFactory.getLogger(Presentation.class);
    public static final int PRESENTATION_START = 0;
    public static final int PRESENTATION_FINISH = 1;
    public static final int SLIDE_CHANGE = 2;
    public static final int SAME_SLIDE = 3;

    private List<Slide> slideList;

    public int getCurrentSlideNumber() {
        return currentSlideNumber;
    }

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
        myTextElement.setTextContent("<h1 style='background : rgba(0,0,0,0);'><b><font color=\"red\">IILP </font><font color=\"blue\">HTML</font> <font color=\"green\">Support Test</font></b></h1>");
        myTextElement.setSlideCanvas(slide1);
        slideElementsSlide1.add(myTextElement);

        GraphicElement myGraphicElement2 = new GraphicElement();
        myGraphicElement2.setLayer(1);
        myGraphicElement2.setStartSequence(3);
        myGraphicElement2.setEndSequence(5);
        myGraphicElement2.setFillColour("00000000");
        myGraphicElement2.setLineColour("00FF00FF");
        myGraphicElement2.setShape(new PolygonBuilder(
                        new float[]{100, 100, 200},
                        new float[]{100, 200, 200},
                        false
                ).build()
        );
        myGraphicElement2.setSlideCanvas(slide1);
        slideElementsSlide1.add(myGraphicElement2);

        GraphicElement myGraphicElement = new GraphicElement();
        myGraphicElement.setLayer(2);
        myGraphicElement.setStartSequence(2);
        myGraphicElement.setEndSequence(5);
        myGraphicElement.setFillColour("00000000");
        myGraphicElement.setLineColour("0000FFFF");
        myGraphicElement.setShape(new OvalBuilder(
                        500.0f,
                        100.0f,
                        30.0f,
                        30.0f,
                        0
                ).build()
        );
        myGraphicElement.setSlideCanvas(slide1);
        slideElementsSlide1.add(myGraphicElement);


        GraphicElement myGraphicElement3 = new GraphicElement();
        myGraphicElement3.setLayer(3);
        myGraphicElement3.setStartSequence(4);
        myGraphicElement3.setEndSequence(6);
        myGraphicElement3.setFillColour("FF0000FF");
        myGraphicElement3.setLineColour("0000FFFF");
        myGraphicElement3.setShape( new PolygonBuilder(
                        new float[]{500, 100, 200, 200},
                        new float[]{100, 200, 200, 100},
                        true
                ).build()
        );
        myGraphicElement3.setSlideCanvas(slide1);
        slideElementsSlide1.add(myGraphicElement3);



        TextElement myTextElement1 = new TextElement();
        myTextElement1.setLayer(5);
        myTextElement1.setStartSequence(7);
        myTextElement1.setEndSequence(8);
        myTextElement1.setTextContent("<b>This is some sample text for Adar to be impressed by</b>");
        myTextElement1.setSlideCanvas(slide1);
        slideElementsSlide1.add(myTextElement1);

        slide1.setSlideElementList(slideElementsSlide1);
        if(slide1.getVideoElementList().size()>0){
            System.out.println(slide1.getVideoElementList().size());
        }

        Slide slide2 = new Slide();
        slide2.setSlideID(2);
        slides.add(slide2);

        //Create some test Slide Elements
        ArrayList<SlideElement> slideElementsSlide2 = new ArrayList<>();

        //Create a test Text element, add some text and pop it onto our stack pane. This code will all be driven from XML parser
        TextElement myTextElementNewSlide = new TextElement();
        myTextElementNewSlide.setLayer(1);
        myTextElementNewSlide.setStartSequence(1);
        myTextElementNewSlide.setEndSequence(5);
        myTextElementNewSlide.setTextContent("<b>Slide2</b>");
        myTextElementNewSlide.setSlideCanvas(slide2);
        slideElementsSlide2.add(myTextElementNewSlide);

        VideoElement myVideoElement = new VideoElement();
        myVideoElement.setMediaPath("externalResources/prometheus.mp4");
        myVideoElement.setAutoPlay(false);
        myVideoElement.setMediaControl(true);
        myVideoElement.setLoop(false);
        myVideoElement.setVideoStartTime(Duration.seconds(0));
        //myVideoElement.setVideoEndTime(Duration.seconds(7));
        myVideoElement.setAspectRatioLock(true);
        //myVideoElement.setxPosition(200);
        //myVideoElement.setyPosition(200);
        myVideoElement.setxSize(200);
        myVideoElement.setySize(200);
        myVideoElement.setLayer(3);
        myVideoElement.setStartSequence(2);
        myVideoElement.setEndSequence(3);
        myVideoElement.setSlideCanvas(slide2);
        slideElementsSlide2.add(myVideoElement);

        slide2.setSlideElementList(slideElementsSlide2);
        if(slide2.getVideoElementList().size()>0){
            System.out.println(slide2.getVideoElementList().size());
        }

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
        myTextElementNewSlide2.setTextContent("<b>Slide3</b>");
        myTextElementNewSlide2.setSlideCanvas(slide3);
        slideElementsSlide3.add(myTextElementNewSlide2);

        slide3.setSlideElementList(slideElementsSlide3);
        if(slide3.getVideoElementList().size()>0){
            System.out.println(slide3.getVideoElementList().size());
        }else{System.out.println("NO VIDEO");}

        Presentation myPresentation = new Presentation();
        myPresentation.setSlideList(slides);

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
}
