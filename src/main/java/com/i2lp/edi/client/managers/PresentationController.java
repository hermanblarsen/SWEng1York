package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.Animation.Animation;
import com.i2lp.edi.client.exceptions.SequenceNotFoundException;
import com.i2lp.edi.client.presentationElements.*;
import com.i2lp.edi.client.presentationViewer.StudentPresentationController;
import com.i2lp.edi.client.presentationViewer.TeacherPresentationController;
import com.i2lp.edi.client.utilities.ParserXML;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
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
public abstract class PresentationController {
    private static final float SLIDE_SIZE = 0.5f;
    private static final double PRES_CONTROLS_HEIGHT = 40;
    private static final double STAGE_MIN_WIDTH = 450;
    private static final double STAGE_MIN_HEIGHT = 300;
    Logger logger = LoggerFactory.getLogger(PresentationController.class);

    protected Scene scene;
    protected StackPane displayPane;
    private VBox sceneBox;

    protected Presentation presentationElement;
    protected ProgressBar progressBar;
    protected Label slideNumber;
    protected Boolean isFullscreen = false;
    //protected Boolean buttonsRemoved = false;
    protected Boolean questionQueueActive = false;
    protected Stage presentationStage;
    protected Boolean isCommentPanelVisible = false;
    protected Panel commentPanel;
    private boolean isShowBlack = false;
    private boolean mouseMoved = true;
    private EventHandler<MouseEvent> disabledCursorFilter;
    private HBox presControls;
    private Region blackRegion;
    private DrawPane drawPane;

    private boolean isCursorHidden = false;
    private CursorState cursorState = CursorState.DEFAULT;

    protected double slideWidth;
    protected double slideHeight;
    protected int currentSlideNumber = 0; //Current slide number in presentation
    private boolean isMouseOverSlide = true;
    private double preFullscreenSlideWidth;
    private double preFullscreenSlideHeight;
    private boolean isMouseOverControls = false;
    private boolean isDrawModeOn = false;
    private boolean showDrawing = true;


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
                toBeAssigned.setPresentationController(this); //Needed for onClickAction
                toBeAssigned.setSlideID(toAssign.getSlideID());
                toBeAssigned.setPresentationID(myPresentationElement.getDocumentID());
                if (this instanceof TeacherPresentationController) {
                    toBeAssigned.setTeacher(true);
                } else {
                    toBeAssigned.setTeacher(false);
                }
                toBeAssigned.setSlideCanvas(toAssign); //Has to be called after setTeacher()
                toBeAssigned.setSlideWidth(slideWidth);
                toBeAssigned.setSlideHeight(slideHeight);
            }
        }
    }

    protected void assignSizeProperties(Slide slide) {
        for (SlideElement slideElement : slide.getSlideElementList()) {
            slideElement.setSlideWidth(slideWidth);
            slideElement.setSlideHeight(slideHeight);
        }
    }

    public void openPresentation(String path) {
        presentationStage = new Stage();
        Image ediLogoSmall = new Image("file:projectResources/logos/ediLogo32x32.png");
        presentationStage.getIcons().add(ediLogoSmall);

        presentationStage.setMinWidth(STAGE_MIN_WIDTH);
        presentationStage.setMinHeight(STAGE_MIN_HEIGHT);
        presentationStage.setOnCloseRequest(event -> destroyAllVisibleElements());



        sceneBox = new VBox();
        displayPane = new StackPane();
        sceneBox.getChildren().add(displayPane);
        VBox.setVgrow(displayPane, Priority.ALWAYS);
        displayPane.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        displayPane.setAlignment(Pos.CENTER);
        blackRegion = new Region();
        blackRegion.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        drawPane = new DrawPane();
        progressBar = new ProgressBar(0);
        slideNumber = new Label();
        presControls = addPresentationControls();

        loadPresentation(path);

        presentationStage.setTitle(presentationElement.getTitle());
        slideNumber.setText("Slide 1 of " + presentationElement.getSlideList().size());

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        slideWidth = primaryScreenBounds.getWidth() * SLIDE_SIZE;
        slideHeight = slideWidth / presentationElement.getDocumentAspectRatio();

        scene = new Scene(sceneBox, slideWidth, slideHeight); //1000x600
        scene.getStylesheets().add("bootstrapfx.css");

        //Listeners for moving through presentation
        addKeyboardListeners();
        addMouseListeners();
        addResizeListeners();

        presentationStage.setScene(scene);
        presentationStage.show();

        createCommentPanel();
        resize();

        if (presentationElement.isAutoplayPresetation()){
            autoPlay();
        }
    }

    private void addKeyboardListeners() {
        scene.setOnKeyPressed(key -> {
            if (key.getCode().equals(KeyCode.ENTER) ||
                    key.getCode().equals(KeyCode.SPACE) ||
                    key.getCode().equals(KeyCode.PAGE_UP) ||
                    key.getCode().equals(KeyCode.RIGHT) ||
                    key.getCode().equals(KeyCode.UP)) {
                controlPresentation(Slide.SLIDE_FORWARD);
            } else if (key.getCode().equals(KeyCode.LEFT) ||
                    key.getCode().equals(KeyCode.BACK_SPACE) ||
                    key.getCode().equals(KeyCode.PAGE_DOWN) ||
                    key.getCode().equals(KeyCode.DOWN)) {
                controlPresentation(Slide.SLIDE_BACKWARD);
            } else if (key.getCode().equals(KeyCode.F5)) {
                toggleFullscreen();
            } else if (key.getCode().equals(KeyCode.ESCAPE) && isFullscreen) {
                setFullscreen(false);
            } else if (key.getCode().equals(KeyCode.B)) {
                if (isShowBlack) {
                    isShowBlack = false;
                } else {
                    isShowBlack = true;
                }
                displayCurrentSlide();
            } else if (key.getCode().equals(KeyCode.HOME)) {
                while (slideAdvance(presentationElement, Slide.SLIDE_BACKWARD) != Presentation.PRESENTATION_START) ;
            } else if (key.getCode().equals(KeyCode.END)) {
                while (slideAdvance(presentationElement, Slide.SLIDE_FORWARD) != Presentation.PRESENTATION_FINISH) ;
            }
        });
    }

    private void addMouseListeners() {
        scene.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                ContextMenu cMenu = new ContextMenu();

                MenuItem nextSequence = new MenuItem("Next sequence");
                nextSequence.setOnAction(nextEvent -> controlPresentation(Slide.SLIDE_FORWARD));
                cMenu.getItems().add(nextSequence);

                MenuItem prevSequence = new MenuItem("Previous sequence");
                prevSequence.setOnAction(prevEvent -> controlPresentation(Slide.SLIDE_BACKWARD));
                cMenu.getItems().add(prevSequence);

                MenuItem firstSequence = new MenuItem("First sequence");
                firstSequence.setOnAction(firstEvent -> {
                    while (slideAdvance(presentationElement, Slide.SLIDE_BACKWARD) != Presentation.PRESENTATION_START);
                    slideProgress(presentationElement);
                });
                cMenu.getItems().add(firstSequence);

                MenuItem lastSequence = new MenuItem("Last sequence");
                lastSequence.setOnAction(lastEvent -> {
                    while (slideAdvance(presentationElement, Slide.SLIDE_FORWARD) != Presentation.PRESENTATION_FINISH) ;
                    slideProgress(presentationElement);
                });
                cMenu.getItems().add(lastSequence);

                cMenu.show(presentationStage, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
        });

        scene.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (isMouseOverSlide) {
                if (event.getDeltaY() > 0) {
                    controlPresentation(Slide.SLIDE_BACKWARD);
                } else {
                    controlPresentation(Slide.SLIDE_FORWARD);
                }
            }
            event.consume();
        });

        disabledCursorFilter = event -> {
            if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED) && event.getButton().equals(MouseButton.PRIMARY)) {
                controlPresentation(Slide.SLIDE_FORWARD);
                event.consume();
            } else if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED) && event.getButton().equals(MouseButton.SECONDARY)) {
                controlPresentation(Slide.SLIDE_BACKWARD);
                event.consume();
            }

        };

        Timer cursorHideTimer = new Timer(true);
        cursorHideTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!mouseMoved && cursorState.equals(CursorState.DEFAULT) && isMouseOverSlide && !isMouseOverControls)
                    setCursorState(CursorState.HIDDEN);

                mouseMoved = false;
            }
        }, 0, 2000);

        displayPane.addEventFilter(MouseEvent.ANY, event -> {
            if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {
                isMouseOverSlide = true;
            } else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {
                isMouseOverSlide = false;
            } else if (event.getEventType().equals(MouseEvent.MOUSE_MOVED)) {
                mouseMoved = true;
                if (cursorState.equals(CursorState.HIDDEN))
                    setCursorState(CursorState.DEFAULT);
            }
        });
    }

    private void addResizeListeners() {
        //Automatic resize of SlideElements
        displayPane.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> resize());
        displayPane.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> resize());
    }

    public void loadPresentation(String path) {
        logger.info("Attempting to load presentation located at: " + path);
        ParserXML xmlParser = new ParserXML(path);
        presentationElement = xmlParser.parsePresentation();

        //TEST PRESENTATION, STOP USING
        //logger.info("Bypassing file located at: " + path + ", programmatically making tet presentation instead.");
        //presentationElement = Presentation.generateTestPresentation();  //TODO REMOVE AND STOP USING

        assignAttributes(presentationElement);
        displayCurrentSlide();
    }

    private void resize() {
        double width = displayPane.getWidth();
        double height = displayPane.getHeight();
        double aspectRatio = presentationElement.getDocumentAspectRatio();

        if (width / height > aspectRatio) {
            slideWidth = height * aspectRatio;
            slideHeight = height;
        } else {
            slideWidth = width;
            slideHeight = width / aspectRatio;
        }

        presentationElement.getSlideList().get(currentSlideNumber).setMaxSize(slideWidth, slideHeight);
        assignSizeProperties(presentationElement.getSlide(currentSlideNumber));

        for (SlideElement toResize : presentationElement.getSlide(currentSlideNumber).getVisibleSlideElementList()) {
            toResize.doClassSpecificRender();
        }

        if(isDrawModeOn)
            drawPane.setMaxSize(slideWidth, slideHeight);
    }

    @SuppressWarnings("ConstantConditions")
    private void controlPresentation(int direction) {
        int presentationStatus = slideAdvance(presentationElement, direction);

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

        slideProgress(presentationElement);
    }

    //protected abstract void questionQueueFunction();

    protected abstract void loadSpecificFeatures();

    protected void toggleComments() {
        if (!isCommentPanelVisible) {
            sceneBox.getChildren().add(commentPanel);
            isCommentPanelVisible = true;
        } else {
            sceneBox.getChildren().remove(commentPanel);
            isCommentPanelVisible = false;
        }

        resize();
    }

    protected void createCommentPanel() {
        commentPanel = new CommentPanel(true);
    }

    private void toggleDrawingMode() {
        if(!isDrawModeOn) {
            isDrawModeOn = true;
            setCursorState(CursorState.DRAW);
            drawPane.setActive(true);
        } else {
            isDrawModeOn = false;
            setCursorState(CursorState.DEFAULT);
            presentationElement.getSlide(currentSlideNumber).setSlideDrawing(drawPane.getSlideDrawing());
            drawPane.setActive(false);
        }

        displayCurrentSlide();
    }

    public HBox addPresentationControls() {
        HBox presControls = new HBox();
        presControls.setStyle("-fx-background-color:transparent");//#34495e
        presControls.setPadding(new Insets(5, 12, 5, 12));
        presControls.setSpacing(5);
        DropShadow shadow = new DropShadow();
        Image next = new Image("file:projectResources/icons/Right_NEW.png", 30, 30, true, true);
        ImageView nextButton = new ImageView(next);
        nextButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> controlPresentation(Slide.SLIDE_FORWARD));
        nextButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> nextButton.setEffect(shadow));
        nextButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> nextButton.setEffect(null));

        Image back = new Image("file:projectResources/icons/Left_NEW.png", 30, 30, true, true);
        ImageView backButton = new ImageView(back);
        backButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> controlPresentation(Slide.SLIDE_BACKWARD));
        backButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> backButton.setEffect(shadow));
        backButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> backButton.setEffect(null));


        Image fullScreen = new Image("file:projectResources/icons/Fullscreen_NEW.png", 30, 30, true, true);

        ImageView fullScreenButton = new ImageView(fullScreen);

        fullScreenButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> toggleFullscreen());
        fullScreenButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> fullScreenButton.setEffect(shadow));
        fullScreenButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> fullScreenButton.setEffect(null));
        ImageView specificFeats;
        if (this instanceof StudentPresentationController) {
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
            questionQ.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> questionQ.setEffect(shadow));
            questionQ.addEventHandler(MouseEvent.MOUSE_EXITED, event -> questionQ.setEffect(null));
            specificFeats = questionQ;
        } else {
            Image checkList = new Image("file:projectResources/icons/TeacherToolKit.png", 30, 30, true, true);
            ImageView teacherToolKit = new ImageView(checkList);
            teacherToolKit.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (!questionQueueActive) {
                    loadSpecificFeatures();
                    questionQueueActive = true;

                } else {
                    loadSpecificFeatures();
                    questionQueueActive = false;
                }
            });
            teacherToolKit.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> teacherToolKit.setEffect(shadow));
            teacherToolKit.addEventHandler(MouseEvent.MOUSE_EXITED, event -> teacherToolKit.setEffect(null));

            specificFeats = teacherToolKit;
        }

        Image commentIcon = new Image("file:projectResources/icons/SB_filled.png", 30, 30, true, true);
        ImageView commentButton = new ImageView(commentIcon);
        commentButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> toggleComments());
        commentButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> commentButton.setEffect(shadow));
        commentButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> commentButton.setEffect(null));

        Image drawIcon = new Image("file:projectResources/icons/draw.png", 30, 30, true, true);
        ImageView drawButton = new ImageView(drawIcon);
        drawButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> toggleDrawingMode());
        drawButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> drawButton.setEffect(shadow));
        drawButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> drawButton.setEffect(null));

        Image visibleIcon = new Image("file:projectResources/icons/eyeVisible.png", 30, 30, true, true);
        Image hiddenIcon = new Image("file:projectResources/icons/eyeHidden.png", 30, 30, true, true);
        ImageView visibilityButton;
        if(showDrawing)
            visibilityButton = new ImageView(hiddenIcon);
        else
            visibilityButton = new ImageView(visibleIcon);

        visibilityButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(showDrawing) {
                showDrawing = false;
                presentationElement.getSlide(currentSlideNumber).setSlideDrawing(drawPane.getSlideDrawing());
                displayPane.getChildren().remove(drawPane);
                visibilityButton.setImage(visibleIcon);
            } else {
                showDrawing = true;
                visibilityButton.setImage(hiddenIcon);
                displayCurrentSlide();
            }
        });
        visibilityButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> visibilityButton.setEffect(shadow));
        visibilityButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> visibilityButton.setEffect(null));

        Image deleteIcon = new Image("file:projectResources/icons/trash.png", 30, 30, true, true);
        ImageView deleteButton= new ImageView(deleteIcon);
        deleteButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            drawPane.clear();
            presentationElement.getSlide(currentSlideNumber).setSlideDrawing(drawPane.getSlideDrawing());
        });
        deleteButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> deleteButton.setEffect(shadow));
        deleteButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> deleteButton.setEffect(null));

        StackPane progressBar = new StackPane();
        this.progressBar.setMinSize(200, 10);
        progressBar.getChildren().addAll(this.progressBar, slideNumber);

        presControls.getChildren().addAll(backButton, nextButton, fullScreenButton, specificFeats, commentButton, drawButton, visibilityButton, deleteButton, progressBar);
        if (this instanceof StudentPresentationController) {

        }

        presControls.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            presControls.setVisible(true);
            FadeTransition ft0 = new FadeTransition(Duration.millis(500), presControls);
            ft0.setFromValue(0.0);
            ft0.setToValue(1.0);
            ft0.play();
            isMouseOverControls = true;
        });
        presControls.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            FadeTransition ft0 = new FadeTransition(Duration.millis(500), presControls);
            ft0.setFromValue(1.0);
            ft0.setToValue(0.0);
            ft0.play();
            isMouseOverControls = false;
        });

        Timer hidePresControlsTimer = new Timer(true);
        hidePresControlsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                FadeTransition ft0 = new FadeTransition(Duration.millis(500), presControls);
                ft0.setFromValue(1.0);
                ft0.setToValue(0.0);
                if(!isMouseOverControls)
                    ft0.play();
            }
        }, (long) 1000); //TODO make this a constant, and maybe add id or show to context menu.

        presControls.setMaxHeight(PRES_CONTROLS_HEIGHT);
        presControls.setAlignment(Pos.BOTTOM_LEFT);
        return presControls;
    }

    protected void slideProgress(Presentation presentation) {
        double slideNo = currentSlideNumber + 1;
        double slideMax = presentation.getSlideList().size();
        double progress = slideNo / slideMax;
        progressBar.setProgress(progress);
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
                        presentationElement.getSlide(currentSlideNumber).setSlideDrawing(drawPane.getSlideDrawing());
                        currentSlideNumber++;
                        presentationStatus = Presentation.SLIDE_CHANGE;
                        //Update MainUI panes when changing slides to account for new Slide root pane.
                        displayCurrentSlide();
                    }
                } else if (changeStatus == Slide.SLIDE_PRE_CHANGE) {
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
                    presentationElement.getSlide(currentSlideNumber).setSlideDrawing(drawPane.getSlideDrawing());
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
                    displayCurrentSlide();
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
                logger.warn("Failed to find Element with Sequence number of " + slideToAdvance.getCurrentSequenceNumber() + " in slideElementList.");
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
                logger.warn("Failed to find Element with Sequence number of " + slideToAdvance.getCurrentSequenceNumber() + " in slideElementList.");
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
//            if(direction == Slide.SLIDE_FORWARD) {
                if (elementToAnimate.getStartSequence() == slideToAdvance.getCurrentSequenceNumber()) {
                elementToAnimate.renderElement(Animation.ENTRY_ANIMATION); //Entry Sequence
                } else if (elementToAnimate.getEndSequence() == slideToAdvance.getCurrentSequenceNumber()) {
                    elementToAnimate.renderElement(Animation.EXIT_ANIMATION); //Exit Sequence
                } //TODO: @Amrik, does the code below make sense?
//            } else if(direction == Slide.SLIDE_BACKWARD) { //When going backwards elements should exit where they entered and vice versa
//                if (elementToAnimate.getEndSequence() == slideToAdvance.getCurrentSequenceNumber()) {
//                    elementToAnimate.renderElement(Animation.ENTRY_ANIMATION); //Entry Sequence
//                } else if (elementToAnimate.getStartSequence() == slideToAdvance.getCurrentSequenceNumber()) {
//                    elementToAnimate.renderElement(Animation.EXIT_ANIMATION); //Exit Sequence
//                }
//            }
        }

        if (slideToAdvance.getCurrentSequenceNumber() == slideToAdvance.getMaxSequenceNumber())
            return Slide.SLIDE_PRE_CHANGE;
        return Slide.SLIDE_NO_MOVE;
    }

    private void autoPlay(){
        Slide currentSlide = presentationElement.getSlide(currentSlideNumber);

        try{
            float longestDuration = 0;
            boolean isAnyNewElements = false;

            ArrayList<SlideElement> currentElements = Slide.searchForSequenceElement(currentSlide.getSlideElementList(), currentSlide.getCurrentSequenceNumber());
            for (SlideElement element : currentElements) {
                // Go Through every element which has start/end sequence here
                if (element.getStartSequence() == currentSlide.getCurrentSequenceNumber()) {
                    //If this element starts at this sequence number (Ignore any which end on this number)
                    isAnyNewElements = true;
                    if (element.getDuration() > longestDuration) {
                        // We will stay on this sequence number for the duration of the longest Element
                        longestDuration = element.getDuration();
                    }
                }
            }

            boolean isLastSlide =  currentSlideNumber == presentationElement.getMaxSlideNumber()-1;
            boolean isLastElement = currentSlide.getCurrentSequenceNumber() == currentSlide.getMaxSequenceNumber();
            if (!(isLastElement && isLastSlide)) {
                // If this isn't the last element in the presentation.
                if (isAnyNewElements && (longestDuration != 0)) {
                    //If a new element was added then set the timer which will advance the slide
                    Timeline autoplayTimeline = new Timeline(
                            new KeyFrame(Duration.seconds(longestDuration), task -> {
                                controlPresentation(Slide.SLIDE_FORWARD);
                                autoPlay();
                            })
                    );
                    autoplayTimeline.setCycleCount(1);
                    autoplayTimeline.play();
                } else {
                    // No new elements start at this sequence number so just skip it.
                    controlPresentation(Slide.SLIDE_FORWARD);
                    autoPlay();
                }
            }
        } catch (SequenceNotFoundException snfe){
            // Most likely we've gone past max sequence number
            controlPresentation(Slide.SLIDE_FORWARD);
            autoPlay();
        }
    }

    /**
     * Shutdown the presentation manager cleanly.
     */
    @SuppressWarnings("FinalizeCalledExplicitly")
    public void close() {
        presentationStage.close();
        try {
            finalize();
        } catch (Throwable throwable) {
            logger.error("Couldnt finalize PresMan");
        }
    }

    private void setCursorState(CursorState state) {
        cursorState = state;
        switch(state) {
            case DEFAULT:
                scene.getRoot().setCursor(Cursor.DEFAULT);
                displayPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, disabledCursorFilter);
                break;
            case HIDDEN:
                scene.getRoot().setCursor(Cursor.NONE);
                displayPane.addEventFilter(MouseEvent.MOUSE_CLICKED, disabledCursorFilter);
                break;
            case DRAW:
                scene.getRoot().setCursor(new ImageCursor(new Image("file:projectResources/cursors/drawCursor.png"),0, Double.MAX_VALUE));
                break;
            default:
                //This should never be reached
        }
    }

    protected void displayCurrentSlide() {
        displayPane.getChildren().clear();
        Slide slide = presentationElement.getSlide(currentSlideNumber);
        slide.setBackground(new Background(new BackgroundFill(Color.valueOf(presentationElement.getTheme().getBackgroundColour()), null, null)));
        displayPane.getChildren().add(slide);

        if(showDrawing) {
            drawPane.setSlideDrawing(presentationElement.getSlide(currentSlideNumber).getSlideDrawing());
            displayPane.getChildren().add(drawPane);
        }

        if (isShowBlack)
            displayPane.getChildren().add(blackRegion);


        if (!(this instanceof ThumbnailGenerationController)) {
            displayPane.getChildren().add(presControls); //TODO: Fix glitch when transitioning between slides
            StackPane.setAlignment(presControls, Pos.BOTTOM_CENTER);
        }

        resize();
    }

    /**
     * Go to a specific slide number
     *
     * @param targetSlideNumber Slide to traverse to.
     * @author Amrik Sadhra
     */
    public void goToSlide(int targetSlideNumber) {
        //If target slide invalid, do nothing and log warning
        if ((targetSlideNumber < 0) || (targetSlideNumber > presentationElement.getMaxSlideNumber())) {
            logger.warn("Target slide number lies outside that which is available in this presentation. Modify XML to account for this.");
            return;
        }

        //If we need to go backwards, go backwards
        if (targetSlideNumber < currentSlideNumber) {
            while (currentSlideNumber != targetSlideNumber - 1) {
                slideAdvance(presentationElement, Slide.SLIDE_BACKWARD);
            }
        } else if (targetSlideNumber > currentSlideNumber) { //If we need to fo forwards, go forwards
            while (currentSlideNumber != targetSlideNumber - 1) {
                slideAdvance(presentationElement, Slide.SLIDE_FORWARD);
            }
        }

        //Update progress bar
        slideProgress(presentationElement);
    }

    private void setFullscreen(boolean fullscreen) {
        presentationStage.setFullScreen(fullscreen);
        isFullscreen = fullscreen;
        if(fullscreen) {
            preFullscreenSlideWidth = slideWidth;
            preFullscreenSlideHeight = slideHeight;

            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            slideWidth = primaryScreenBounds.getWidth();
            slideHeight = primaryScreenBounds.getHeight();
        } else {
            slideWidth = preFullscreenSlideWidth;
            slideHeight = preFullscreenSlideHeight;
        }
        resize();
    }

    private void toggleFullscreen() {
        if (!isFullscreen) {
            setFullscreen(true);
        } else {
            setFullscreen(false);
        }
    }

    private void destroyAllVisibleElements() {
        for(SlideElement slideElement : presentationElement.getSlide(currentSlideNumber).getVisibleSlideElementList())
            slideElement.destroyElement();
    }
}

enum CursorState {
    DEFAULT,
    HIDDEN,
    DRAW
}
