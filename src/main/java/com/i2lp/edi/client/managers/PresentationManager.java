package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.Animation.Animation;
import com.i2lp.edi.client.exceptions.SequenceNotFoundException;
import com.i2lp.edi.client.presentationElements.CommentPanel;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.presentationElements.Slide;
import com.i2lp.edi.client.presentationElements.SlideElement;
import com.i2lp.edi.client.presentationViewer.TeacherPresentationManager;
import com.i2lp.edi.client.utilities.ParserXML;
import javafx.animation.FadeTransition;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by kma517 on 16/03/2017.
 */
public abstract class PresentationManager {
    private static final float SLIDE_SIZE = 0.5f;
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
    protected Boolean elementClicked = false;
    protected VBox bottomPane = new VBox();
    protected Panel commentPanel;
    private boolean isShowBlack = false;
    private boolean mouseMoved = true;
    private EventHandler<MouseEvent> disabledCursorFilter;

    private boolean isCursorHidden = false;
    protected double slideWidth = 0;
    protected double slideHeight = 0;

    protected StackPane stackPane;
    public int currentSlideNumber = 0; //Current slide number in presentation


    /**
     * Assign Slide to each SlideElement within a presentation, to enable them to be setup and rendered. Plumbs XML parsing into
     * Presentation rendering.
     *
     * @param myPresentationElement Presentation within which to assign canvas's
     * @author Amrik Sadhra
     */
    protected void assignAttributes(Presentation myPresentationElement) {
        for (Slide toAssign : myPresentationElement.getSlideList()) {
            for (SlideElement toBeAssigned : toAssign.getSlideElementList()) {
                toBeAssigned.setSlideID(toAssign.getSlideID());
                toBeAssigned.setPresentationID(myPresentationElement.getDocumentID());
                toBeAssigned.setSlideCanvas(toAssign);
                if(this instanceof TeacherPresentationManager){
                    toBeAssigned.setTeacher(true);
                }else{
                    toBeAssigned.setTeacher(false);
                }
                toBeAssigned.setSlideWidth(slideWidth);
                toBeAssigned.setSlideHeight(slideHeight);
            }
        }
    }

    public void openPresentation(String path) {
        presentationStage = new Stage();
        presentationStage.setTitle("Edi");

        border = new BorderPane();
        loadPresentation(border, path);
        stackPane = new StackPane();
        Region blackRegion = new Region();
        blackRegion.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        stackPane.getChildren().add(border);
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        slideWidth = primaryScreenBounds.getWidth() * SLIDE_SIZE;
        slideHeight = slideWidth / myPresentationElement.getTheme().getPresentationAspectRatio();
        scene = new Scene(stackPane, slideWidth, slideHeight); //1000x600
        scene.getStylesheets().add("bootstrapfx.css");

        //Listeners for moving through presentation
        scene.setOnKeyPressed(key -> {
            if (key.getCode().equals(KeyCode.ENTER) ||
                    key.getCode().equals(KeyCode.SPACE) ||
                    key.getCode().equals(KeyCode.PAGE_UP) ||
                    key.getCode().equals(KeyCode.RIGHT) ||
                    key.getCode().equals(KeyCode.UP)) {
                controlPresentation(Slide.SLIDE_FORWARD);
                slideProgress(myPresentationElement);
            } else if (key.getCode().equals(KeyCode.LEFT) ||
                    key.getCode().equals(KeyCode.BACK_SPACE) ||
                    key.getCode().equals(KeyCode.PAGE_DOWN) ||
                    key.getCode().equals(KeyCode.DOWN)) {
                controlPresentation(Slide.SLIDE_BACKWARD);
                slideProgress(myPresentationElement);
            } else if(key.getCode().equals(KeyCode.F5)) {
                presentationStage.setFullScreen(true);
            } else if(key.getCode().equals(KeyCode.B)) {
                if(isShowBlack) {
                    stackPane.getChildren().remove(blackRegion);
                    isShowBlack = false;
                } else {
                    stackPane.getChildren().add(blackRegion);
                    isShowBlack = true;
                }
            } else if(key.getCode().equals(KeyCode.HOME)) {
                while (slideAdvance(myPresentationElement, Slide.SLIDE_BACKWARD) != Presentation.PRESENTATION_START);
            } else if(key.getCode().equals(KeyCode.END)) {
                while (slideAdvance(myPresentationElement, Slide.SLIDE_FORWARD) != Presentation.PRESENTATION_FINISH);
            }
        });

        scene.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                ContextMenu cMenu = new ContextMenu();

                MenuItem nextSequence = new MenuItem("Next sequence");
                nextSequence.setOnAction(nextEvent -> {
                    controlPresentation(Slide.SLIDE_FORWARD);
                    slideProgress(myPresentationElement);
                });
                cMenu.getItems().add(nextSequence);

                MenuItem prevSequence = new MenuItem("Previous sequence");
                prevSequence.setOnAction(prevEvent -> {
                    controlPresentation(Slide.SLIDE_BACKWARD);
                    slideProgress(myPresentationElement);
                });
                cMenu.getItems().add(prevSequence);

                MenuItem firstSequence = new MenuItem("First sequence");
                firstSequence.setOnAction(firstEvent -> {
                    while (slideAdvance(myPresentationElement, Slide.SLIDE_BACKWARD) != Presentation.PRESENTATION_START);
                });
                cMenu.getItems().add(firstSequence);

                MenuItem lastSequence = new MenuItem("Last sequence");
                lastSequence.setOnAction(lastEvent -> {
                    while (slideAdvance(myPresentationElement, Slide.SLIDE_FORWARD) != Presentation.PRESENTATION_FINISH);
                });
                cMenu.getItems().add(lastSequence);

                cMenu.show(presentationStage, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
        });

        scene.setOnScroll(event -> {
            if(event.getDeltaY()>0) {
                controlPresentation(Slide.SLIDE_BACKWARD);
                slideProgress(myPresentationElement);
            } else {
                controlPresentation(Slide.SLIDE_FORWARD);
                slideProgress(myPresentationElement);
            }
        });

        disabledCursorFilter = event -> {
            controlPresentation(Slide.SLIDE_FORWARD);
            slideProgress(myPresentationElement);
            event.consume();
        };

        Timer cursorHideTimer = new Timer(true);
        cursorHideTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(!mouseMoved && !isCursorHidden())
                    setCursorHidden(true);

                mouseMoved = false;
            }
        }, 0, 2000);

        scene.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            mouseMoved = true;
            if(isCursorHidden())
                setCursorHidden(false);
        });

        presentationStage.setScene(scene);
        presentationStage.show();


        pb = new ProgressBar(0);
        slideNumber = new Label("Slide 1 of " + myPresentationElement.getSlideList().size());

        createCommentPanel();
        bottomPane.getChildren().add(0, addPresentationControls(presentationStage));
        border.setBottom(bottomPane);
    }

    public void loadPresentation(BorderPane mainUI, String path) {
        logger.info("Attempting to load presentation located at: " + path);

        ParserXML readPresentationParser = new ParserXML(path);
        myPresentationElement = readPresentationParser.parsePresentation();
        //myPresentationElement = Presentation.generateTestPresentation();     //TEST

        assignAttributes(myPresentationElement);
        mainUI.setCenter(myPresentationElement.getSlide(currentSlideNumber));


        //Automatic resize of SlideElements
        scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {redraw();});
        scene.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> redraw());
    }

    private void redraw(){
        logger.trace("Resizing slide Elements");
        for (SlideElement toResize : myPresentationElement.getSlide(currentSlideNumber).getVisibleSlideElementList()) {
                toResize.doClassSpecificRender();
        }
    }

    @SuppressWarnings("ConstantConditions")
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

    //protected abstract void questionQueueFunction();

    protected abstract void loadSpecificFeatures();

    protected void commentFunction() {
        if(!elementClicked) {
            bottomPane.getChildren().add(0, commentPanel);
            elementClicked = true;
        }else{
            bottomPane.getChildren().remove(commentPanel);
            elementClicked = false;
        }
    }

    protected void createCommentPanel() {
        commentPanel = new CommentPanel(true);
    }

    public HBox addPresentationControls(Stage primaryStage) {
        HBox presControls = new HBox();
        presControls.setStyle("-fx-background-color: #34495e;");
        presControls.setPadding(new Insets(5, 12, 5, 12));
        presControls.setSpacing(5);
        Image next = new Image("file:projectResources/icons/Right_NEW.png", 30, 30, true, true);
        ImageView nextButton = new ImageView(next);
        nextButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            controlPresentation(Slide.SLIDE_FORWARD);
            slideProgress(myPresentationElement);

        });

        Image back = new Image("file:projectResources/icons/Left_NEW.png", 30, 30, true, true);
        ImageView backButton = new ImageView(back);
        backButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            controlPresentation(Slide.SLIDE_BACKWARD);
            slideProgress(myPresentationElement);
        });

        Image fullScreen = new Image("file:projectResources/icons/Fullscreen_NEW.png", 30, 30, true, true);

        ImageView fullScreenButton = new ImageView(fullScreen);

        fullScreenButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            if (!isFullscreen) {
                primaryStage.setFullScreen(true);
                isFullscreen = true;
                slideWidth = primaryScreenBounds.getWidth();
                slideHeight = primaryScreenBounds.getHeight();

            } else {
                primaryStage.setFullScreen(false);
                isFullscreen = false;
                slideWidth = primaryScreenBounds.getWidth() * 0.75;
                slideHeight = primaryScreenBounds.getHeight() * 0.75;
            }
        });

        Image questionBubble = new Image("file:projectResources/icons/QM_Filled.png", 30, 30, true, true);
        ImageView questionQ = new ImageView(questionBubble);
        questionQ.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (!questionQueueActive) {
                loadSpecificFeatures();
                questionQueueActive = true;

            } else {
                loadSpecificFeatures();
                questionQueueActive = false;
            }
        });

        Image commentIcon = new Image("file:projectResources/icons/SB_filled.png", 30, 30, true, true);
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

    public boolean isCursorHidden() { return isCursorHidden;}

    public void setCursorHidden(boolean cursorHidden) {
        isCursorHidden = cursorHidden;
        if(cursorHidden) {
            scene.getRoot().setCursor(Cursor.NONE); //TODO: Doesn't seem to work on webviews?
            border.getCenter().addEventFilter(MouseEvent.MOUSE_CLICKED, disabledCursorFilter);
        } else {
            scene.getRoot().setCursor(Cursor.DEFAULT);
            border.getCenter().removeEventFilter(MouseEvent.MOUSE_CLICKED, disabledCursorFilter);
        }
    }
}
