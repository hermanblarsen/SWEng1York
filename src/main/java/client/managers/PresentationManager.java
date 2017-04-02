package client.managers;

import client.presentationElements.Presentation;
import client.presentationElements.Slide;
import client.utilities.ParserXML;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
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


/**
 * Created by kma517 on 16/03/2017.
 */
public abstract class PresentationManager extends Application {
    protected Scene scene;
    protected BorderPane border;
    protected Presentation myPresentationElement;
    protected ProgressBar pb;
    protected Label slideNumber;
    protected Boolean isFullscreen = false;
    //protected Boolean buttonsRemoved = false;
    protected Boolean questionQueueActive = false;
    protected Boolean commentActive = false;



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


    protected void loadPresentation(BorderPane mainUI, String path) {
        ParserXML readPresentationParser = new ParserXML(path);

        myPresentationElement = Presentation.generateTestPresentation();     //TEST
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

    protected abstract void questionQueueFunction();
    protected abstract void commentFunction();

    public HBox addPresentationControls(Stage primaryStage){
        HBox presControls = new HBox();
        presControls.setStyle("-fx-background-color: #34495e;");
        presControls.setPadding(new Insets(5, 12, 5, 12));
        presControls.setSpacing(5);
        Image next = new Image("file:externalResources/Right_NEW.png",30,30,true,true);
        ImageView nextButton = new ImageView(next);
        nextButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {

            controlPresentation(Slide.SLIDE_FORWARD);
            slideProgress(myPresentationElement);

        });

        Image back = new Image("file:externalResources/Left_NEW.png",30,30,true,true);
        ImageView backButton = new ImageView(back);
        backButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {

            controlPresentation(Slide.SLIDE_BACKWARD);
            slideProgress(myPresentationElement);

        });

        Image fullScreen = new Image("file:externalResources/Fullscreen_NEW.png", 30,30,true,true);

        ImageView fullScreenButton = new ImageView(fullScreen);

        fullScreenButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {

            if(!isFullscreen) {
                primaryStage.setFullScreen(true);
                isFullscreen = true;
            }
            else{
                primaryStage.setFullScreen(false);
                isFullscreen = false;
            }

        });

        Image questionBubble = new Image("file:externalResources/QM_Filled.png",30,30,true,true);
        ImageView questionQ = new ImageView(questionBubble);
        questionQ.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(!questionQueueActive) {
                questionQueueFunction();
                questionQueueActive = true;

            }else{
                questionQueueFunction();
                questionQueueActive = false;

            }
        });

        Image commentIcon = new Image("file:externalResources/SB_filled.png",30,30,true,true);
        ImageView commentButton = new ImageView(commentIcon);
        commentButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(!commentActive) {
                commentFunction();
                commentActive = true;

            }else{
                commentFunction();
                commentActive = false;

            }

        });


        StackPane progressBar = new StackPane();
        pb.setMinSize(200,10);
        progressBar.getChildren().addAll(pb,slideNumber);

        presControls.getChildren().addAll(backButton, nextButton, fullScreenButton,questionQ,commentButton,progressBar);

        presControls.addEventHandler(MouseEvent.MOUSE_ENTERED, evt ->{
            presControls.setVisible(true);
            FadeTransition ft0 = new FadeTransition(Duration.millis(500),presControls);
            ft0.setFromValue(0.0);
            ft0.setToValue(1.0);
            ft0.play();

        });
        presControls.addEventHandler(MouseEvent.MOUSE_EXITED, evt->{
            FadeTransition ft0 = new FadeTransition(Duration.millis(500),presControls);
            ft0.setFromValue(1.0);
            ft0.setToValue(0.0);
            ft0.play();
        });
        return presControls;
    }


    protected void slideProgress(Presentation pe){
        double slideNo = pe.getCurrentSlideNumber()+1;
        double slideMax = pe.getSlideList().size();
        double progress  = slideNo/slideMax;
        pb.setProgress(progress);
        slideNumber.setText("Slide "+(int) slideNo+" of "+(int) slideMax);
    }

    protected void controlPresentation(int direction) {
        int presentationStatus = myPresentationElement.advance(direction);
        // If Presentation handler told us that slide is changing, update the Slide present on Main screen
        // Can do specific things when presentation reached end, or start.
        if ((presentationStatus == Presentation.SLIDE_CHANGE) || (presentationStatus == Presentation.PRESENTATION_FINISH) || (presentationStatus == Presentation.PRESENTATION_START)) {
            //logger.info("Changing Slides");
            System.out.println("Changing Slides");

            // Update MainUI panes when changing slides to account for new Slide root pane.
            border.setCenter(myPresentationElement.getCurrentSlide());
            // border.setBottom(addStatBar(myPresentationElement.getCurrentSlide()));
        }


    }


}
