package client.managers;

import client.exceptions.SequenceNotFoundException;
import client.presentationElements.Animation;
import client.presentationElements.Presentation;
import client.presentationElements.Slide;
import client.presentationElements.SlideElement;
import client.utilities.ParserXML;
import javafx.animation.FadeTransition;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


/**
 * Created by kma517 on 16/03/2017.
 */
public abstract class PresentationManager {
    Logger logger = LoggerFactory.getLogger(PresentationManager.class);

    protected Scene scene;
    public BorderPane border;
    public Presentation myPresentationElement;
    protected ProgressBar pb;
    protected Label slideNumber;
    protected Boolean isFullscreen = false;
    //protected Boolean buttonsRemoved = false;
    protected Boolean questionQueueActive = false;
    protected Boolean commentActive = false;
    protected Stage presentationStage;

    public int currentSlideNumber = 0; //Current slide number in presentation


    /**
     * Assign Slide to each SlideElement within a presentation, to enable them to be setup and rendered. Plumbs XML parsing into
     * Presentation rendering.
     *
     * @param myPresentationElement Presentation within which to assign canvas's
     * @author Amrik Sadhra
     */
    protected void assignCanvas(Presentation myPresentationElement) {
        for (Slide toAssign : myPresentationElement.getSlideList()) {
            for (SlideElement toBeAssigned : toAssign.getSlideElementList()) {
                toBeAssigned.setSlideCanvas(toAssign);
            }
        }
    }

    public void openPresentation(String path) {
        presentationStage = new Stage();
        presentationStage.setTitle("Edi");

        border = new BorderPane();
        scene = new Scene(border, 1000, 600);
        scene.getStylesheets().add("bootstrapfx.css");
        presentationStage.setScene(scene);
        presentationStage.show();
        loadPresentation(border, path);

        pb = new ProgressBar(0);
        slideNumber = new Label("Slide 1 of " + myPresentationElement.getSlideList().size());
        border.setBottom(addPresentationControls(presentationStage));
    }

    public void loadPresentation(BorderPane mainUI, String path) {
        logger.info("Attempting to load presentation located at: " + path);

        ParserXML readPresentationParser = new ParserXML(path);
        myPresentationElement = readPresentationParser.parsePresentation();
        //myPresentationElement = Presentation.generateTestPresentation();     //TEST

        assignCanvas(myPresentationElement);
        mainUI.setCenter(myPresentationElement.getSlide(currentSlideNumber));

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

    private void controlPresentation(int direction) {
        int presentationStatus = slideAdvance(myPresentationElement, direction);

        //If Presentation handler told us that slide is changing, update the Slide present on Main screen
        //Can do specific things when presentation reached end, or start.
        if (presentationStatus == Presentation.SLIDE_CHANGE || presentationStatus == Presentation.PRESENTATION_FINISH || presentationStatus == Presentation.PRESENTATION_START || presentationStatus == Presentation.SLIDE_LAST_ELEMENT) {
            if (presentationStatus == Presentation.SLIDE_CHANGE) {
                logger.info("Changing Slides");
            } else if (presentationStatus == Presentation.PRESENTATION_START) {
                logger.info("At Presentation start");
            } else if (presentationStatus == Presentation.PRESENTATION_FINISH) {
                logger.info("At Presentation finish");
            } else if (presentationStatus == Presentation.SLIDE_LAST_ELEMENT) {
                logger.info("On last element in slide");
            }
        }
    }

    protected abstract void questionQueueFunction();

    protected abstract void commentFunction();

    public HBox addPresentationControls(Stage primaryStage) {
        HBox presControls = new HBox();
        presControls.setStyle("-fx-background-color: #34495e;");
        presControls.setPadding(new Insets(5, 12, 5, 12));
        presControls.setSpacing(5);
        Image next = new Image("file:externalResources/Right_NEW.png", 30, 30, true, true);
        ImageView nextButton = new ImageView(next);
        nextButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            controlPresentation(Slide.SLIDE_FORWARD);
            slideProgress(myPresentationElement);

        });

        Image back = new Image("file:externalResources/Left_NEW.png", 30, 30, true, true);
        ImageView backButton = new ImageView(back);
        backButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            controlPresentation(Slide.SLIDE_BACKWARD);
            slideProgress(myPresentationElement);
        });

        Image fullScreen = new Image("file:externalResources/Fullscreen_NEW.png", 30, 30, true, true);

        ImageView fullScreenButton = new ImageView(fullScreen);

        fullScreenButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {

            if (!isFullscreen) {
                primaryStage.setFullScreen(true);
                isFullscreen = true;
            } else {
                primaryStage.setFullScreen(false);
                isFullscreen = false;
            }

        });

        Image questionBubble = new Image("file:externalResources/QM_Filled.png", 30, 30, true, true);
        ImageView questionQ = new ImageView(questionBubble);
        questionQ.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (!questionQueueActive) {
                questionQueueFunction();
                questionQueueActive = true;

            } else {
                questionQueueFunction();
                questionQueueActive = false;

            }
        });

        Image commentIcon = new Image("file:externalResources/SB_filled.png", 30, 30, true, true);
        ImageView commentButton = new ImageView(commentIcon);
        commentButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (!commentActive) {
                commentFunction();
                commentActive = true;

            } else {
                commentFunction();
                commentActive = false;
            }

        });


        StackPane progressBar = new StackPane();
        pb.setMinSize(200, 10);
        progressBar.getChildren().addAll(pb, slideNumber);

        presControls.getChildren().addAll(backButton, nextButton, fullScreenButton, questionQ, commentButton, progressBar);

        presControls.addEventHandler(MouseEvent.MOUSE_ENTERED, evt -> {
            presControls.setVisible(true);
            FadeTransition ft0 = new FadeTransition(Duration.millis(500), presControls);
            ft0.setFromValue(0.0);
            ft0.setToValue(1.0);
            ft0.play();

        });
        presControls.addEventHandler(MouseEvent.MOUSE_EXITED, evt -> {
            FadeTransition ft0 = new FadeTransition(Duration.millis(500), presControls);
            ft0.setFromValue(1.0);
            ft0.setToValue(0.0);
            ft0.play();
        });
        return presControls;
    }

    protected void slideProgress(Presentation pe) {
        double slideNo = currentSlideNumber + 1;
        double slideMax = pe.getSlideList().size();
        double progress = slideNo / slideMax;
        pb.setProgress(progress);
        slideNumber.setText("Slide " + (int) slideNo + " of " + (int) slideMax);
    }

    public int slideAdvance(Presentation presentationToAdvance, int direction) {
        //Initialise this with something more appropriate
        int presentationStatus = Presentation.SAME_SLIDE;
        int changeStatus;
        if (direction == Slide.SLIDE_FORWARD) {
            //If we're not at end of presentation
            if (currentSlideNumber < presentationToAdvance.getMaxSlideNumber()) {
                //If slide tells you to move forward to next slide, do it by changing to next slide in slide list.
                if ((changeStatus = elementAdvance(presentationToAdvance.getSlide(currentSlideNumber), direction)) == direction) {
                    if (currentSlideNumber + 1 >= presentationToAdvance.getMaxSlideNumber()) {
                        logger.info("Reached final slide: " + presentationToAdvance.getMaxSlideNumber());
                        presentationStatus = Presentation.PRESENTATION_FINISH;
                    } else {
                        currentSlideNumber++;
                        presentationStatus = Presentation.SLIDE_CHANGE;
                        //Update MainUI panes when changing slides to account for new Slide root pane.
                        border.setCenter(myPresentationElement.getSlide(currentSlideNumber));
                    }
                } else if (changeStatus == Slide.SLIDE_PRE_CHANGE){
                    //Userful state for Thumbnail generation
                    presentationStatus = Presentation.SLIDE_LAST_ELEMENT;
                }
            } else {//If on last slide, as == maxSlideNumber
                logger.info("Reached final slide: " + presentationToAdvance.getMaxSlideNumber());
                presentationStatus = Presentation.PRESENTATION_FINISH;
            }
        } else if (direction == Slide.SLIDE_BACKWARD) {
            //If we're not at start of presentation
            if (currentSlideNumber >= 0) {
                //If slide tells you to move backward to prev slide, do it by changing to prev slide in slide list.
                //Allow slideElements to play on slide though.
                if (elementAdvance(presentationToAdvance.getSlide(currentSlideNumber), direction) == direction) {
                    currentSlideNumber--;
                    if (currentSlideNumber < 0) {
                        logger.info("Reached Min slide number. Presentation back at start.");
                        currentSlideNumber = 0;//Wrap to this slide as minimum
                        presentationStatus = Presentation.PRESENTATION_START;
                    } else {
                        presentationStatus = Presentation.SLIDE_CHANGE;
                    }
                    presentationToAdvance.setCurrentSlide(presentationToAdvance.getSlideList().get(currentSlideNumber));
                    //Update MainUI panes when changing slides to account for new Slide root pane.
                    border.setCenter(myPresentationElement.getSlide(currentSlideNumber));
                }
            }
        }
        return presentationStatus;
    }

    public int elementAdvance(Slide slideToAdvance, int direction) {
        ArrayList<SlideElement> checkInVisibleSet;
        //If we're going forwards and not through all sequences in slide set
        if ((slideToAdvance.getCurrentSequenceNumber() < slideToAdvance.getMaxSequenceNumber()) && (direction == Slide.SLIDE_FORWARD)) {
            slideToAdvance.setCurrentSequenceNumber(slideToAdvance.getCurrentSequenceNumber() + 1);
            //Search for elements with matching start sequence or end sequence in visible set. If they're not in there, add them.
            try {
                checkInVisibleSet = Slide.searchForSequenceElement(slideToAdvance.getSlideElementList(), slideToAdvance.getCurrentSequenceNumber());

                for (SlideElement toCheckVisible : checkInVisibleSet) {
                    if (!(slideToAdvance.getVisibleSlideElementList().contains(toCheckVisible))) {
                        slideToAdvance.getVisibleSlideElementList().add(toCheckVisible);
                    }
                }
            } catch (SequenceNotFoundException e) {
                logger.error("Failed to find Element with Sequence number of " + slideToAdvance.getCurrentSequenceNumber() + " in slideElementList.");
            }
        } else if ((slideToAdvance.getCurrentSequenceNumber() > 0) && (direction == Slide.SLIDE_BACKWARD)) {  //If we're going backwards and still elements left
            try {
                checkInVisibleSet = Slide.searchForSequenceElement(slideToAdvance.getSlideElementList(), slideToAdvance.getCurrentSequenceNumber());

                for (SlideElement toCheckVisible : checkInVisibleSet) {
                    if (slideToAdvance.getVisibleSlideElementList().contains(toCheckVisible)) {
                        toCheckVisible.removeElement();
                    }
                }
            } catch (SequenceNotFoundException e) {
                logger.error("Failed to find Element with Sequence number of " + slideToAdvance.getCurrentSequenceNumber() + " in slideElementList.");
            }
            slideToAdvance.setCurrentSequenceNumber(slideToAdvance.getCurrentSequenceNumber() - 1);
        } else {
            //If we're at limit of sequence number, alert calling method that we need to move to next/previous slide dependent on direction and reset sequence number
            switch (direction) {
                case Slide.SLIDE_FORWARD:
                    return Slide.SLIDE_FORWARD;
                case Slide.SLIDE_BACKWARD:
                    return Slide.SLIDE_BACKWARD;
            }
        }

        //Sort by Layer
        Slide.sortElementsByLayer(slideToAdvance.getVisibleSlideElementList());
        logger.info("Current Sequence is " + slideToAdvance.getCurrentSequenceNumber());
        //Fire animations
        for (SlideElement elementToAnimate : slideToAdvance.getVisibleSlideElementList()) {
            if (elementToAnimate.getStartSequence() == slideToAdvance.getCurrentSequenceNumber()) {
                elementToAnimate.renderElement(Animation.ENTRY_ANIMATION); //Entry Sequence
            } else if (elementToAnimate.getEndSequence() == slideToAdvance.getCurrentSequenceNumber()) {
                elementToAnimate.renderElement(Animation.EXIT_ANIMATION); //Exit Sequence
            }
        }

        if(slideToAdvance.getCurrentSequenceNumber() == slideToAdvance.getMaxSequenceNumber()) return Slide.SLIDE_PRE_CHANGE;
        return Slide.SLIDE_NO_MOVE;
    }

    /**
     * Shutdown the presentation manager cleanly.
     *
     */
    @SuppressWarnings("FinalizeCalledExplicitly")
    public void close(){
        presentationStage.close();
        try {
            finalize();
        } catch (Throwable throwable) {
            logger.error("Couldnt finalize PresMan");
        }
    }
}
