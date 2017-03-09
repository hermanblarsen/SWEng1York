package presentationViewer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utilities.*;

import java.util.ArrayList;

/**
 * Created by amriksadhra on 23/02/2017.
 */
public class PresentationViewer extends Application {
    Scene scene;
    BorderPane border;
    Presentation myPresentationElement;

    public static void main(String[] args){launch(args);}

    @Override
    public void start(Stage primaryStage){
        primaryStage.setTitle("Edi");

        border = new BorderPane();
        scene = new Scene(border, 1000, 600);
        scene.getStylesheets().add("bootstrapfx.css");
        primaryStage.setScene(scene);
        border.setCenter(new StackPane());

        primaryStage.show();

        loadPresentation(border, "shit");
        border.setBottom(addPresentationControls(primaryStage));
    }

    private void loadPresentation(BorderPane mainUI, String path) {
        ParserXML readPresentationParser = new ParserXML(path);

        myPresentationElement = generateTestPresentation();     //TEST
//        myPresentationElement = readPresentationParser.parsePresentation();

        mainUI.setCenter(myPresentationElement.getCurrentSlide());
        //mainUI.setBottom(addStatBar(myPresentationElement.getCurrentSlide()));

//        for (Slide currentSlide : myPresentationElement.getSlideList()) {
//            for (SlideElement element : currentSlide.getSlideElementList()) {
//                element.setSlideCanvas(currentSlide);
//            }
//        }


        //Keyboard listener for moving through presentation
        scene.setOnKeyPressed(key -> {
            int presentationStatus = 0;
            if (key.getCode().equals(KeyCode.RIGHT)) {
                controlPresentation(Slide.SLIDE_FORWARD);
            } else if (key.getCode().equals(KeyCode.LEFT)) {
                controlPresentation(Slide.SLIDE_BACKWARD);
            }
        });
    }

    Boolean isFullscreen = false; //TODO Move this somewhere sensible
    public HBox addPresentationControls(Stage primaryStage){

        HBox presControls = new HBox();
        Button prevSlide = new Button("<");
        prevSlide.setOnAction(event -> {
            controlPresentation(Slide.SLIDE_BACKWARD);
        });
        Button nextSlide = new Button(">");
        nextSlide.setOnAction(event -> {
            controlPresentation(Slide.SLIDE_FORWARD);
        });

        Button fullScreen = new Button("Fullscreen");
        fullScreen.setOnAction(event -> {
            if(isFullscreen == false) {
                primaryStage.setFullScreen(true);
                fullScreen.setText("Exit Fullscreen");
                isFullscreen = true;
            }
            else{
                primaryStage.setFullScreen(false);
                fullScreen.setText("Fullscreen");
                isFullscreen = false;
            }
        });

        presControls.getChildren().addAll(prevSlide,nextSlide,fullScreen);
        return presControls;
    }

    private void controlPresentation(int direction) {
        int presentationStatus = myPresentationElement.advance(direction);

        //If Presentation handler told us that slide is changing, update the Slide present on Main screen
        //Can do specific things when presentation reached end, or start.
        if ((presentationStatus == Presentation.SLIDE_CHANGE) || (presentationStatus == Presentation.PRESENTATION_FINISH) || (presentationStatus == Presentation.PRESENTATION_START)) {
           // logger.info("Changing Slides");
            //Update MainUI panes when changing slides to account for new Slide root pane.
            border.setCenter(myPresentationElement.getCurrentSlide());
            //border.setBottom(addStatBar(myPresentationElement.getCurrentSlide()));
        }


    }

    public Presentation generateTestPresentation() {
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

        GraphicElement myGraphicElement = new GraphicElement();
        myGraphicElement.setLayer(1);
        myGraphicElement.setStartSequence(2);
        myGraphicElement.setEndSequence(4);
        myGraphicElement.setSlideCanvas(slide1);
        slideElementsSlide1.add(myGraphicElement);

        /*VideoElement myVideoElement = new VideoElement();
        myVideoElement.setMediaPath("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv");
        myVideoElement.setAutoPlay(true);
        myVideoElement.setMediaControl(true);
        myVideoElement.setLoop(false);
        myVideoElement.setVideoStartTime(Duration.seconds(5));
        myVideoElement.setVideoEndTime(Duration.seconds(7));
        myVideoElement.setAspectRatioLock(true);
        myVideoElement.setxPosition(200);
        myVideoElement.setyPosition(200);
        myVideoElement.setxSize(2000);
        myVideoElement.setySize(2000);
        myVideoElement.setLayer(2);
        myVideoElement.setStartSequence(5);
        myVideoElement.setEndSequence(6);
        myVideoElement.setSlideCanvas(slide1);
        slideElementsSlide1.add(myVideoElement);*/

        TextElement myTextElement1 = new TextElement();
        myTextElement1.setLayer(3);
        myTextElement1.setStartSequence(3);
        myTextElement1.setEndSequence(4);
        myTextElement1.setTextContent("<b>Poop</b>");
        myTextElement1.setSlideCanvas(slide1);
        slideElementsSlide1.add(myTextElement1);

        slide1.setSlideElementList(slideElementsSlide1);

        Slide slide2 = new Slide();
        slide2.setSlideID(2);
        slides.add(slide2);

        //Create some test Slide Elements
        ArrayList<SlideElement> slideElementsSlide2 = new ArrayList<>();

        //Create a test Text element, add some text and pop it onto our stack pane. This code will all be driven from XML parser
        TextElement myTextElementNewSlide = new TextElement();
        myTextElementNewSlide.setLayer(1);
        myTextElementNewSlide.setStartSequence(1);
        myTextElementNewSlide.setEndSequence(2);
        myTextElementNewSlide.setTextContent("<b>Slide2</b>");
        myTextElementNewSlide.setSlideCanvas(slide2);
        slideElementsSlide2.add(myTextElementNewSlide);

        slide2.setSlideElementList(slideElementsSlide2);

        Presentation myPresentation = new Presentation();
        myPresentation.setSlideList(slides);

        return myPresentation;
    }
}
