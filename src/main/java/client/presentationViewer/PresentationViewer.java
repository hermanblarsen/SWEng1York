package client.presentationViewer;

import client.presentationElements.*;
import javafx.animation.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import client.utilities.*;

import java.util.ArrayList;

/**
 * Created by amriksadhra on 23/02/2017.
 */
public class PresentationViewer extends Application {
    private Scene scene;
    private BorderPane border;
    private Presentation myPresentationElement;
    private ProgressBar pb;
    private Label slideNumber;
    private Boolean isFullscreen = false;

    public static void main(String[] args){launch(args);}

    @Override
    public void start(Stage primaryStage){
        primaryStage.setTitle("Edi");

        border = new BorderPane();
        scene = new Scene(border, 1000, 600);
        scene.getStylesheets().add("bootstrapfx.css");
        primaryStage.setScene(scene);
        primaryStage.show();
        loadPresentation(border, "shit");
        pb = new ProgressBar(0);
        slideNumber = new Label("Slide 1 of "+myPresentationElement.getSlideList().size());
        border.setBottom(addPresentationControls(primaryStage));
    }

    private void loadPresentation(BorderPane mainUI, String path) {
        ParserXML readPresentationParser = new ParserXML(path);

        myPresentationElement = generateTestPresentation();     //TEST
 //       myPresentationElement = readPresentationParser.parsePresentation();

        mainUI.setCenter(myPresentationElement.getCurrentSlide());
        //mainUI.setBottom(addStatBar(myPresentationElement.getCurrentSlide()));

//        for (Slide currentSlide : myPresentationElement.getSlideList()) {
//            for (SlideElement element : currentSlide.getSlideElementList()) {
//                element.setSlideCanvas(currentSlide);
//            }
//        }


        //Keyboard listener for moving through presentation
        scene.setOnKeyPressed(key -> {

            if (key.getCode().equals(KeyCode.RIGHT)) {
                controlPresentation(Slide.SLIDE_FORWARD);
                slideProgress(myPresentationElement);
            } else if (key.getCode().equals(KeyCode.LEFT)) {
                controlPresentation(Slide.SLIDE_BACKWARD);
                slideProgress(myPresentationElement);
            }
        });


    }

    public HBox addPresentationControls(Stage primaryStage){
        HBox presControls = new HBox();
        presControls.setStyle("-fx-background-color: #34495e;");
        presControls.setMinHeight(10);
        Image next = new Image("file:externalResources/Right_NEW.png",30,30,true,true);
        ImageView nextButton = new ImageView(next);
        nextButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                controlPresentation(Slide.SLIDE_FORWARD);
                slideProgress(myPresentationElement);

            }
        });

        Image back = new Image("file:externalResources/Left_NEW.png",30,30,true,true);
        ImageView backButton = new ImageView(back);
        backButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                controlPresentation(Slide.SLIDE_BACKWARD);
                slideProgress(myPresentationElement);

            }
        });

        Image fullScreen = new Image("file:externalResources/Fullscreen_NEW.png", 30,30,true,true);

        ImageView fullScreenButton = new ImageView(fullScreen);

        fullScreenButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                if(isFullscreen == false) {
                    primaryStage.setFullScreen(true);
                    isFullscreen = true;
                }
                else{
                    primaryStage.setFullScreen(false);
                    isFullscreen = false;
                }

            }

        });

        StackPane sp = new StackPane();
        sp.getChildren().addAll(pb,slideNumber);

        presControls.addEventHandler(MouseEvent.MOUSE_ENTERED, evt ->{

            presControls.getChildren().addAll(backButton,nextButton,fullScreenButton,sp);

            FadeTransition ft = new FadeTransition(Duration.millis(300),backButton);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
            FadeTransition ft2 = new FadeTransition(Duration.millis(300),nextButton);
            ft2.setFromValue(0.0);
            ft2.setToValue(1.0);
            ft2.play();
            FadeTransition ft3 = new FadeTransition(Duration.millis(300),fullScreenButton);
            ft3.setFromValue(0.0);
            ft3.setToValue(1.0);
            ft3.play();
            FadeTransition ft4 = new FadeTransition(Duration.millis(300),sp);
            ft4.setFromValue(0.0);
            ft4.setToValue(1.0);
            ft4.play();

        });
        presControls.addEventHandler(MouseEvent.MOUSE_EXITED, evt->{
            FadeTransition ft = new FadeTransition(Duration.millis(300),backButton);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.play();
            FadeTransition ft2 = new FadeTransition(Duration.millis(300),nextButton);
            ft2.setFromValue(1.0);
            ft2.setToValue(0.0);
            ft2.play();
            FadeTransition ft3 = new FadeTransition(Duration.millis(300),fullScreenButton);
            ft3.setFromValue(1.0);
            ft3.setToValue(0.0);
            ft3.play();
            FadeTransition ft4 = new FadeTransition(Duration.millis(300),sp);
            ft4.setFromValue(1.0);
            ft4.setToValue(0.0);
            ft4.play();

            ft4.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    presControls.getChildren().removeAll(backButton, nextButton, fullScreenButton, sp);
                }
            });

        });

        return presControls;
    }

    private void slideProgress(Presentation pe){
        double slideNo = pe.getCurrentSlideNumber()+1;
        double slideMax = pe.getSlideList().size();
        double progress  = slideNo/slideMax;
        pb.setProgress(progress);
        slideNumber.setText("Slide "+(int) slideNo+" of "+(int) slideMax);
    }

    private void controlPresentation(int direction) {
        int presentationStatus = myPresentationElement.advance(direction);

        // If Presentation handler told us that slide is changing, update the Slide present on Main screen
        // Can do specific things when presentation reached end, or start.
        if ((presentationStatus == Presentation.SLIDE_CHANGE) || (presentationStatus == Presentation.PRESENTATION_FINISH) || (presentationStatus == Presentation.PRESENTATION_START)) {
            // logger.info("Changing Slides");
            // Update MainUI panes when changing slides to account for new Slide root pane.
            border.setCenter(myPresentationElement.getCurrentSlide());
            // border.setBottom(addStatBar(myPresentationElement.getCurrentSlide()));
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

//        VideoElement myVideoElement = new VideoElement();
//        myVideoElement.setMediaPath("externalResources/prometheus.mp4");
//        myVideoElement.setAutoPlay(true);
//        myVideoElement.setMediaControl(true);
//        myVideoElement.setLoop(false);
//        myVideoElement.setVideoStartTime(Duration.seconds(0));
//        //myVideoElement.setVideoEndTime(Duration.seconds(7));
//        myVideoElement.setAspectRatioLock(true);
//        //myVideoElement.setxPosition(200);
//       // myVideoElement.setyPosition(200);
//        myVideoElement.setxSize(2000);
//        myVideoElement.setySize(2000);
//        myVideoElement.setLayer(3);
//        myVideoElement.setStartSequence(6);
//        myVideoElement.setEndSequence(7);
//        myVideoElement.setSlideCanvas(slide1);
//        slideElementsSlide1.add(myVideoElement);

        TextElement myTextElement1 = new TextElement();
        myTextElement1.setLayer(5);
        myTextElement1.setStartSequence(7);
        myTextElement1.setEndSequence(8);
        myTextElement1.setTextContent("<b>This is some sample text for Adar to be impressed by</b>");
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

        Presentation myPresentation = new Presentation();
        myPresentation.setSlideList(slides);

        return myPresentation;
    }
}
