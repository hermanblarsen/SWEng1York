package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.PresentationSession;
import com.i2lp.edi.client.StudentSession;
import com.i2lp.edi.client.animation.Animation;
import com.i2lp.edi.client.exceptions.SequenceNotFoundException;
import com.i2lp.edi.client.presentationElements.InteractiveElement;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.presentationElements.Slide;
import com.i2lp.edi.client.presentationElements.SlideElement;
import com.i2lp.edi.client.presentationViewerElements.CommentPanel;
import com.i2lp.edi.client.presentationViewerElements.DrawPane;
import com.i2lp.edi.client.utilities.CursorState;
import com.i2lp.edi.client.utilities.SimpleChangeListener;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by kma517 on 16/03/2017.
 */
public abstract class PresentationManager {
    private static final float SLIDE_SIZE_ON_OPEN = 0.5f;
    private static final int PRES_CONTROLS_HEIGHT = 40;
    private static final int STAGE_MIN_WIDTH = 500;
    private static final int STAGE_MIN_HEIGHT = 300;
    private static final int HIDE_CURSOR_DELAY = 2000;
    public static final int MIN_ERASER_SIZE = 10;
    private static final double MAX_ERASER_SIZE = 30;
    private static final double DEFAULT_BUTTON_WIDTH = 30;
    private static final double DEFAULT_BUTTON_HEIGHT = 30;

    Logger logger = LoggerFactory.getLogger(PresentationManager.class);

    /* -------------- LIVE SESSION OBJECCTS ----------------*/
    protected final EdiManager ediManager;
    private PresentationSession presentationSession;
    private StudentSession studentSession;
    //Interactive Element list for linking to ServerSide data
    private ArrayList<InteractiveElement> interactiveElementList = new ArrayList<>();

    /* -------------- UI OBJECTS ----------------*/
    protected Scene scene;
    protected StackPane displayPane;
    protected VBox sceneBox;
    protected Presentation presentationElement;
    protected ProgressBar progressBar;
    private double slideProgress;
    protected Label slideNumber;
    protected Boolean isFullscreen = false;
    //protected Boolean buttonsRemoved = false;
    protected Boolean questionQueueActive = false;
    protected Stage presentationStage;
    protected CommentPanel commentPanel;
    protected Boolean isCommentPanelVisible = false;
    protected Boolean isEmbeddedBrowserOpen = false;
    private boolean isShowBlack = false;
    private boolean mouseActivityRegistered = true;
    private boolean mouseDown = false;
    private EventHandler<MouseEvent> disabledCursorFilter;
    protected BorderPane controlsPane;
    protected HBox presControls;
    protected VBox drawControls;
    private Region blackRegion;
    protected DrawPane drawPane;
    private ImageView visibilityButton;
    public ImageView linkButton;
    private Popup colourPopup;
    private CursorState currentCursorState = CursorState.DEFAULT;
    private Pane eraseCursorPane;

    protected double slideWidth;
    protected double slideHeight;


    protected int currentSlideNumber = 0; //Current slide number in presentation
    protected int currentSequenceNumber = 0; //Current sequence number in presentation (total)
    private boolean isMouseOverSlide = true;
    private double preFullscreenSlideWidth;
    private double preFullscreenSlideHeight;
    protected boolean isMouseOverControls = false;
    protected boolean isDrawModeOn = false;
    protected boolean isDrawPaneVisible = true;
    private boolean isThumbnailGen = false;
    private boolean isEndPresentation = false;
    private StackPane slidePane;

    private boolean wordCloudActive = false;

    public PresentationManager(EdiManager ediManager) {
        this.ediManager = ediManager;

        presentationStage = new Stage();
        Image ediLogoSmall = new Image("file:projectResources/logos/ediLogo32x32.png");
        presentationStage.getIcons().add(ediLogoSmall);
        presentationStage.setMinWidth(STAGE_MIN_WIDTH);
        presentationStage.setMinHeight(STAGE_MIN_HEIGHT);
        presentationStage.setOnCloseRequest(event -> {
            destroyAllElements();
            close();
        });

        sceneBox = new VBox();
        sceneBox.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (colourPopup != null) {
                if (colourPopup.isShowing() && !event.getTarget().equals(colourPopup)) {
                    colourPopup.hide();
                }
            }
        });
        displayPane = new StackPane();
        displayPane.addEventFilter(MouseEvent.ANY, event -> {
            if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
                logger.trace("Mouse Pressed");
                mouseDown = true;
            } else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
                logger.trace("Mouse Released");
                mouseDown = false;
            }
        });
        sceneBox.getChildren().add(displayPane);
        VBox.setVgrow(displayPane, Priority.ALWAYS);
        displayPane.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        displayPane.setAlignment(Pos.CENTER);
        blackRegion = new Region();
        blackRegion.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        drawPane = new DrawPane(displayPane);
        progressBar = new ProgressBar(0);
        slideNumber = new Label();
        controlsPane = new BorderPane();
        controlsPane.setPickOnBounds(false);
        displayPane.setPickOnBounds(false);
        presControls = addPresentationControls();
        drawControls = addDrawControls();
    }

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
                toBeAssigned.setEdiManager(ediManager); //Allows access to PresMan for OnClick, and SocketClient for Interactive Elements
                toBeAssigned.setSlideID(toAssign.getSlideID());
                toBeAssigned.setPresentationID(myPresentationElement.getDocumentID());
                if (this instanceof PresentationManagerTeacher) {
                    toBeAssigned.setTeacher(true);
                } else {
                    toBeAssigned.setTeacher(false);
                }
                //setupElement is called from within setSlideCanvas.  setupElement needs to be done for all elements.
                toBeAssigned.setSlideCanvas(toAssign); //Has to be called after setTeacher()
                toBeAssigned.setSlideWidth(slideWidth);
                toBeAssigned.setSlideHeight(slideHeight);
            }
            //Retrieve Interactive Element lists
            interactiveElementList.addAll(toAssign.getInteractiveElementList());
        }
    }

    public ArrayList<InteractiveElement> getInteractiveElementList(){
        if(interactiveElementList.isEmpty()) logger.error("Interactive element list not set yet");
        return interactiveElementList;
    }

    protected void assignSizeProperties(Slide slide) {
        for (SlideElement slideElement : slide.getSlideElementList()) {
            slideElement.setSlideWidth(slideWidth);
            slideElement.setSlideHeight(slideHeight);
        }
    }

    public void openPresentation(Presentation presentation, Boolean thumbnailGen) {
        this.presentationElement = presentation;
        assignAttributes(presentationElement);
        presentationStage.setTitle(presentationElement.getDocumentTitle());
        slideNumber.setText("Slide 1 of " + presentationElement.getSlideList().size());
        isThumbnailGen = thumbnailGen;
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        slideWidth = primaryScreenBounds.getWidth() * SLIDE_SIZE_ON_OPEN;
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
        commentPanel.setSlide(this.presentationElement.getCurrentSlide()); //Required for comments to work.

        displayCurrentSlide();

        beginLiveSession();//Start the live session before the presentation starts!

        if (presentationElement.isAutoplayPresentation()) {
            autoPlay();
        } else {
            //Move to StartSequence 1
            slideAdvance(presentationElement, Slide.SLIDE_FORWARD);
            notifySlideChangeListener(-1, currentSlideNumber);//Send out initial Slide change event.
        }

    }

    private void beginLiveSession() {
        if (presentationElement.getPresentationMetadata() != null) {//If not local presentation
            if (presentationElement.getPresentationMetadata().getLive()) {//If we are live, start up a PresentationSession in which to track connectivity data
                if (this instanceof PresentationManagerTeacher) {
                    presentationSession = new PresentationSession(ediManager);
                } else if (this instanceof PresentationManagerStudent) {
                    studentSession = new StudentSession(ediManager);
                }
            }
        }
    }

    public StudentSession getStudentSession() {
        return studentSession;
    }

    public PresentationSession getPresentationSession() {
        return presentationSession;
    }

    private void addKeyboardListeners() {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (!isCommentPanelVisible && !isEmbeddedBrowserOpen && !wordCloudActive) {
                if (keyEvent.getCode().equals(KeyCode.ENTER) ||
                        keyEvent.getCode().equals(KeyCode.SPACE) ||
                        keyEvent.getCode().equals(KeyCode.PAGE_UP) ||
                        keyEvent.getCode().equals(KeyCode.RIGHT) ||
                        keyEvent.getCode().equals(KeyCode.UP)) {
                    controlPresentation(Slide.SLIDE_FORWARD);
                } else if (keyEvent.getCode().equals(KeyCode.LEFT) ||
                        keyEvent.getCode().equals(KeyCode.BACK_SPACE) ||
                        keyEvent.getCode().equals(KeyCode.PAGE_DOWN) ||
                        keyEvent.getCode().equals(KeyCode.DOWN)) {
                    controlPresentation(Slide.SLIDE_BACKWARD);
                } else if (keyEvent.getCode().equals(KeyCode.F5)) {
                    toggleFullscreen();
                } else if (keyEvent.getCode().equals(KeyCode.ESCAPE) && isFullscreen) {
                    setFullscreen(false);
                } else if (keyEvent.getCode().equals(KeyCode.B)) {
                    if (isShowBlack) {
                        isShowBlack = false;
                    } else {
                        isShowBlack = true;
                    }
                    displayCurrentSlide();
                } else if (keyEvent.getCode().equals(KeyCode.HOME)) {
                    while (slideAdvance(presentationElement, Slide.SLIDE_BACKWARD) != Presentation.PRESENTATION_START) ;
                } else if (keyEvent.getCode().equals(KeyCode.END)) {
                    while (slideAdvance(presentationElement, Slide.SLIDE_FORWARD) != Presentation.PRESENTATION_FINISH) ;
                }

                keyEvent.consume();
            }
        });
    }

    private void addMouseListeners() {  //TODO maybe add hide or show to context menu.
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
                    while (slideAdvance(presentationElement, Slide.SLIDE_BACKWARD) != Presentation.PRESENTATION_START) ;
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
            if (isMouseOverSlide && !isEmbeddedBrowserOpen) {
                if (event.getDeltaY() > 0) {
                    controlPresentation(Slide.SLIDE_BACKWARD);
                } else {
                    controlPresentation(Slide.SLIDE_FORWARD);
                }
            }
        });

        disabledCursorFilter = event -> {
            if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED) && event.getButton().equals(MouseButton.PRIMARY)) {
                controlPresentation(Slide.SLIDE_FORWARD);
                event.consume();
            } else if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED) && event.getButton().equals(MouseButton.SECONDARY)) {
                controlPresentation(Slide.SLIDE_BACKWARD);
                event.consume();
            }
        };

        Timer cursorHideTimer = new Timer(true);
        cursorHideTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!mouseDown && !mouseActivityRegistered && currentCursorState.equals(CursorState.DEFAULT) && isMouseOverSlide && !isMouseOverControls)
                    setCursorState(CursorState.HIDDEN);

                mouseActivityRegistered = false;
            }
        }, 0, 2000);

        displayPane.addEventFilter(MouseEvent.ANY, event -> {
            if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {
                isMouseOverSlide = true;
            } else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {
                isMouseOverSlide = false;
            }

            mouseActivityRegistered = true;
            if (currentCursorState.equals(CursorState.HIDDEN)) {
                setCursorState(CursorState.DEFAULT);
            }
        });

        drawPane.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            if (isDrawModeOn) {
                if (drawPane.isEraserMode()) {
                    setCursorState(CursorState.ERASE);
                } else {
                    setCursorState(CursorState.DRAW);
                }
            }
        });
    }

    private void addResizeListeners() {
        //Automatic resize of SlideElements
        displayPane.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> resize());
        displayPane.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> resize());
    }

    protected void resize() {
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

        Slide currentSlide = presentationElement.getSlideList().get(currentSlideNumber);
        currentSlide.setMaxSize(slideWidth, slideHeight);
        assignSizeProperties(currentSlide);

        for (SlideElement toResize : presentationElement.getSlide(currentSlideNumber).getVisibleSlideElementList()) {
            toResize.doClassSpecificRender();
        }

        drawPane.setMaxSize(slideWidth, slideHeight);
    }

    @SuppressWarnings("ConstantConditions")
    private void controlPresentation(int direction) {
        if ((direction == Slide.SLIDE_BACKWARD) && isEndPresentation) {
            isEndPresentation = false;
            displayCurrentSlide();
            return;
        }

        int presentationStatus = slideAdvance(presentationElement, direction);

        //If Presentation handler told us that slide is changing, update the Slide present on Main screen
        //Can do specific things when presentation reached end, or start.
        if (presentationStatus == Presentation.SLIDE_CHANGE || presentationStatus == Presentation.PRESENTATION_FINISH || presentationStatus == Presentation.PRESENTATION_START || presentationStatus == Presentation.SLIDE_LAST_ELEMENT || presentationStatus == Presentation.SAME_SLIDE) {
            if ((presentationStatus == Presentation.SLIDE_CHANGE) || (presentationStatus == Presentation.SAME_SLIDE) || (presentationStatus == Presentation.SLIDE_LAST_ELEMENT)) {
                if (presentationStatus == Presentation.SLIDE_CHANGE) logger.info("Changing Slides");
                if (getPresentationSession() != null) {//If in live hosting session
                    getPresentationSession().synchroniseSlides();
                }
            } else if (presentationStatus == Presentation.PRESENTATION_START) {
                logger.info("At Presentation start");
            } else if (presentationStatus == Presentation.PRESENTATION_FINISH) {
                logger.info("At Presentation finish");
                if (!isEndPresentation) {
                    isEndPresentation = true;
                    displayCurrentSlide();
                }
            } else if (presentationStatus == Presentation.SLIDE_LAST_ELEMENT) {
                logger.info("On last element in slide");
            }
        }

        slideProgress(presentationElement);
    }

    //protected abstract void questionQueueFunction();

    protected abstract void loadSpecificFeatures();

    protected void toggleCommentsWindow() {
        if (!isCommentPanelVisible) {
            commentPanel.setSlide(presentationElement.getCurrentSlide());
            sceneBox.getChildren().add(commentPanel);
            if (!isFullscreen && !presentationStage.isMaximized()) {
                presentationStage.setHeight(presentationStage.getHeight() + commentPanel.getPrefHeight());
            }
            isCommentPanelVisible = true;
        } else {
            if (!isFullscreen && !presentationStage.isMaximized()) {
                presentationStage.setHeight(presentationStage.getHeight() - commentPanel.getHeight());
            }
            sceneBox.getChildren().remove(commentPanel);
            isCommentPanelVisible = false;
        }

        resize();
    }

    protected abstract void createCommentPanel();

    private void toggleDrawingMode() {
        if (!isDrawModeOn && !isEndPresentation) {
            isDrawModeOn = true;
            setDrawPaneVisible(true);
            drawPane.setActive(true);
            controlsFadeIn(drawControls);
        } else {
            isDrawModeOn = false;
            drawPane.setActive(false);
        }

        displayCurrentSlide();
    }

    private void setDrawPaneVisible(boolean setVisible) {
        if (setVisible) {
            isDrawPaneVisible = true;
            Image hiddenIcon = new Image("file:projectResources/icons/eyeHidden.png", 30, 30, true, true);
            visibilityButton.setImage(hiddenIcon);
            displayCurrentSlide();
        } else {
            isDrawPaneVisible = false;
            displayPane.getChildren().remove(drawPane);
            Image visibleIcon = new Image("file:projectResources/icons/eyeVisible.png", 30, 30, true, true);
            visibilityButton.setImage(visibleIcon);
        }
    }


    public HBox addPresentationControls() {
        HBox presControls = new HBox();
        presControls.setStyle("-fx-background-color:transparent");//#34495e
        presControls.setPadding(new Insets(5, 12, 5, 12));
        presControls.setSpacing(5);

        ImageView nextButton = makeCustomButton("file:projectResources/icons/Right_NEW.png", event -> controlPresentation(Slide.SLIDE_FORWARD));

        ImageView backButton = makeCustomButton("file:projectResources/icons/Left_NEW.png", event -> controlPresentation(Slide.SLIDE_BACKWARD));

        ImageView fullScreenButton = makeCustomButton("file:projectResources/icons/Fullscreen_NEW.png", event -> toggleFullscreen());

        String specificFeatsIconURL;
        if (this instanceof PresentationManagerStudent) {
            specificFeatsIconURL = "file:projectResources/icons/QM_Filled.png";
        } else {
            specificFeatsIconURL = "file:projectResources/icons/TeacherToolKit.png";
        }
        ImageView specificFeats = makeCustomButton(specificFeatsIconURL, event -> {
            if (!questionQueueActive) {
                loadSpecificFeatures();
                questionQueueActive = true;

            } else {
                loadSpecificFeatures();
                questionQueueActive = false;
            }
        });


        String linkIconURL = "file:projectResources/icons/lock.png";

        linkButton = makeCustomButton(linkIconURL, evt -> {
            if (studentSession != null) {
                if (studentSession.isLinked()) {
                    studentSession.setPresentationLink(false);
                } else {
                    studentSession.setPresentationLink(true);
                }
            }
        });


        ImageView commentButton = makeCustomButton("file:projectResources/icons/SB_filled.png", event -> toggleCommentsWindow());

        ImageView drawButton = makeCustomButton("file:projectResources/icons/draw.png", event -> toggleDrawingMode());

        String visibilityIconURL;
        if (isDrawPaneVisible)
            visibilityIconURL = "file:projectResources/icons/eyeHidden.png";
        else
            visibilityIconURL = "file:projectResources/icons/eyeVisible.png";



        visibilityButton = makeCustomButton(visibilityIconURL, event -> {
            if (isDrawPaneVisible) {
                setDrawPaneVisible(false);
            } else {
                setDrawPaneVisible(true);
            }
        });

        StackPane progressBar = new StackPane();
        this.progressBar.setMinSize(200, 10);
        progressBar.getChildren().addAll(this.progressBar, slideNumber);
        if (this instanceof PresentationManagerStudent) {
            presControls.getChildren().addAll(backButton, nextButton, fullScreenButton, linkButton, specificFeats, commentButton, drawButton, visibilityButton, progressBar);
        } else {
            presControls.getChildren().addAll(backButton, nextButton, fullScreenButton, specificFeats, commentButton, drawButton, visibilityButton, progressBar);

        }
        addMouseHandlersToControls(presControls);

        presControls.setMaxHeight(PRES_CONTROLS_HEIGHT);
        presControls.setAlignment(Pos.BOTTOM_LEFT);

        return presControls;
    }

    private ImageView makeCustomButton(String iconURL, EventHandler<MouseEvent> mouseClickedEventHandler) {
        return makeCustomButton(iconURL, mouseClickedEventHandler, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT);
    }

    private ImageView makeCustomButton(String iconURL, EventHandler<MouseEvent> mouseClickedEventHandler, double width, double height) {
        ImageView button = new ImageView(new Image(iconURL, width, height, true, true));
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedEventHandler);
        button.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> button.setEffect(new DropShadow()));
        button.addEventHandler(MouseEvent.MOUSE_EXITED, event -> button.setEffect(null));

        return button;
    }

    private VBox addDrawControls() {
        VBox drawControls = new VBox(5);
        drawControls.setStyle("-fx-background-color:transparent");//#34495e
        drawControls.setPadding(new Insets(5, 12, 5, 12));

        ImageView undoButton = makeCustomButton("file:projectResources/icons/undo.png", event -> drawPane.setSlideDrawing(presentationElement.getSlide(currentSlideNumber).getPreviousSlideDrawing()));

        ImageView redoButton = makeCustomButton("file:projectResources/icons/redo.png", event -> drawPane.setSlideDrawing(presentationElement.getSlide(currentSlideNumber).getNextSlideDrawing()));

        ImageView eraserButton = makeCustomButton("file:projectResources/icons/erase.png", event -> {
            if (drawPane.isEraserMode()) {
                drawPane.setEraserMode(false);
            } else {
                drawPane.setEraserMode(true);
            }
        });

        ColorPicker colorPicker = new ColorPicker(drawPane.getBrushColor());
        ImageView colourButton = makeCustomButton("file:projectResources/icons/selectBrushColour.png", event -> {
            colourPopup = new Popup();
            colorPicker.setOnAction(event1 -> {
                drawPane.setBrushColor(colorPicker.getValue());
                colourPopup.hide();
            });
            colourPopup.getContent().add(colorPicker);
            colourPopup.show(presentationStage, event.getScreenX(), event.getScreenY());
        });

        ImageView widthButton = makeCustomButton("file:projectResources/icons/selectBrushWidth.png", event -> {
            Popup widthPopup = new Popup();
            Slider widthSlider;
            widthPopup.setAutoHide(true);
            if (!drawPane.isEraserMode()) {
                widthSlider = new Slider(0.1, 10, drawPane.getBrushWidth());
            } else {
                widthSlider = new Slider(MIN_ERASER_SIZE, MAX_ERASER_SIZE, drawPane.getEraserSize());
            }
            widthPopup.setOnAutoHide(event1 -> {
                if (!drawPane.isEraserMode()) {
                    drawPane.setBrushWidth(widthSlider.getValue());
                } else {
                    drawPane.setEraserSize(widthSlider.getValue());
                }
            });
            widthSlider.addEventFilter(MouseEvent.MOUSE_ENTERED, event1 -> setCursorState(CursorState.DEFAULT));
            widthSlider.setOnMouseReleased(event1 -> {
                if (!drawPane.isEraserMode()) {
                    drawPane.setBrushWidth(widthSlider.getValue());
                } else {
                    drawPane.setEraserSize(widthSlider.getValue());
                }
                widthPopup.hide();
            });
            widthPopup.getContent().add(widthSlider);
            widthPopup.show(presentationStage, event.getScreenX(), event.getScreenY());
        });

        ImageView deleteButton = makeCustomButton("file:projectResources/icons/trash.png", event -> {
            drawPane.clear();
            presentationElement.getSlide(currentSlideNumber).addSlideDrawing(drawPane.getSlideDrawing());
        });

        drawControls.getChildren().addAll(undoButton, redoButton, eraserButton, colourButton, widthButton, deleteButton);
        drawControls.setAlignment(Pos.CENTER_LEFT);

        addMouseHandlersToControls(drawControls);

        return drawControls;
    }

    private void addMouseHandlersToControls(Node controls) {
        controls.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            if (!mouseDown) {
                controlsFadeIn(controls);
                isMouseOverControls = true;
                setCursorState(CursorState.DEFAULT);
            }
        });
        controls.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            controlsFadeOut(controls);
            isMouseOverControls = false;
        });

        controlsFadeIn(controls);
    }

    private void controlsFadeIn(Node controls) {
        FadeTransition ft0 = new FadeTransition(Duration.millis(500), controls);
        ft0.setFromValue(controls.getOpacity());
        ft0.setToValue(1.0);
        ft0.play();


        Timer hideControlsTimer = new Timer(true);
        hideControlsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                FadeTransition ft0 = new FadeTransition(Duration.millis(500), controls);
                ft0.setFromValue(controls.getOpacity());
                ft0.setToValue(0.0);
                if (!isMouseOverControls)
                    ft0.play();
            }
        }, (long) HIDE_CURSOR_DELAY);
    }

    private void controlsFadeOut(Node controls) {
        FadeTransition ft0 = new FadeTransition(Duration.millis(500), controls);
        ft0.setFromValue(controls.getOpacity());
        ft0.setToValue(0.0);
        ft0.play();
    }

    protected void slideProgress(Presentation presentation) {
        //Calculate the total number of sequences in the presentation
        int sequenceNumberMax = 0;
        for (Slide slide : presentation.getSlideList()) {
            sequenceNumberMax += slide.getMaxSequenceNumber();
            sequenceNumberMax++;
        }

        //Make sure the current sequence doesn't go out of bounds
        if (currentSequenceNumber >= sequenceNumberMax) currentSequenceNumber = sequenceNumberMax;
        if (currentSequenceNumber <= 0) currentSequenceNumber = 0;

        //Calculate progress
        slideProgress = (float) (currentSequenceNumber) / sequenceNumberMax;
        progressBar.setProgress(slideProgress);

        //Make sure currentSlideNumber doesn't overflow and reset text in progressbar
        int slideNumber = currentSlideNumber + 1;
        int slideNumberMax = presentation.getSlideList().size();
        this.slideNumber.setText("Slide " + slideNumber + " of " + slideNumberMax);
    }

    public int slideAdvance(Presentation presentationToAdvance, int direction) {
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
                        presentationElement.getSlide(currentSlideNumber).destroyAllVisible(); // Destroy any remaining elements
                        currentSlideNumber++;
                        presentationStatus = Presentation.SLIDE_CHANGE;
                        //Update MainUI panes when changing slides to account for new Slide root pane.
                        displayCurrentSlide();
                    }
                    notifySlideChangeListener(currentSlideNumber-1, currentSlideNumber);
                } else if (changeStatus == Slide.SLIDE_PRE_CHANGE) {
                    //Useful state for Thumbnail generation
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
                    presentationElement.getSlide(currentSlideNumber).destroyAllVisible(); //Destroy any remaining elements on the slide
                    currentSlideNumber--;
                    if (currentSlideNumber < 0) {
                        logger.info("Reached Min slide number. Presentation back at start.");
                        currentSlideNumber = 0;//Wrap to this slide as minimum
                        presentationStatus = Presentation.PRESENTATION_START;
                    } else {
                        presentationStatus = Presentation.SLIDE_CHANGE;
                    }

                    //Update MainUI panes when changing slides to account for new Slide root pane.
                    displayCurrentSlide();
                    notifySlideChangeListener(currentSlideNumber+1, currentSlideNumber);
                }

            }
        }
        presentationToAdvance.setCurrentSlide(presentationToAdvance.getSlideList().get(currentSlideNumber));

        if ((presentationToAdvance.getCurrentSlide().getCurrentSequenceNumber() == 0) && (currentSlideNumber != 0))
            slideAdvance(presentationToAdvance, direction);

        if (commentPanel != null) commentPanel.setSlide(this.presentationElement.getCurrentSlide());
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
            currentSequenceNumber++;
            notifySequenceChangeListeners(currentSequenceNumber-1, currentSequenceNumber);//Notify any sequence number listeners that there has been a change
        } else if ((slideToAdvance.getCurrentSequenceNumber() > 1) && (direction == Slide.SLIDE_BACKWARD)) {  //If we're going backwards and still elements left
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
            currentSequenceNumber--;
            notifySequenceChangeListeners(currentSequenceNumber+1, currentSequenceNumber);//Notify any sequence number listeners that there has been a change
        } else {
            //If we're at limit of sequence number, alert calling method that we need to move to next/previous slide dependent on direction and reset sequence number
            switch (direction) {
                case Slide.SLIDE_FORWARD:
                    currentSequenceNumber++;
                    return Slide.SLIDE_FORWARD;
                case Slide.SLIDE_BACKWARD:
                    currentSequenceNumber--;
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

        if (slideToAdvance.getCurrentSequenceNumber() == slideToAdvance.getMaxSequenceNumber())
            return Slide.SLIDE_PRE_CHANGE;

        return Slide.SLIDE_NO_MOVE;
    }

    private void autoPlay() {
        Slide currentSlide = presentationElement.getSlide(currentSlideNumber);

        try {
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

            boolean isLastSlide = currentSlideNumber == presentationElement.getMaxSlideNumber() - 1;
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
        } catch (SequenceNotFoundException snfe) {
            // Most likely we've gone past max sequence number
            controlPresentation(Slide.SLIDE_FORWARD);
            autoPlay();
        }
    }

    /**
     * Shutdown the presentation manager cleanly. End Teacher/Student Session
     */
    @SuppressWarnings("FinalizeCalledExplicitly")
    public void close() {
        if (this instanceof PresentationManagerTeacher) {
            if (presentationSession != null) {
                presentationSession.endSession();
            }
        } else if (this instanceof PresentationManagerStudent) {
            if (studentSession != null) {
                studentSession.endSession();
            }
        }

        //Reset EdiManager presentation manager reference to null
        if(ediManager != null) ediManager.setPresentationManager(null);
        presentationStage.close();
    }

    protected void displayCurrentSlide() {
        displayPane.getChildren().clear();
        if (!isEndPresentation) {
            Slide slide = presentationElement.getSlide(currentSlideNumber);
            slide.setBackground(new Background(new BackgroundFill(Color.valueOf(presentationElement.getTheme().getBackgroundColour()), null, null)));
            displayPane.getChildren().add(slide);
        } else {
            Label lab = new Label("End of Presentation");
            Region bgRegion = new Region();
            bgRegion.setBackground(new Background(new BackgroundFill(Color.web("#34495e"), null, null)));
            slidePane = new StackPane();
            lab.setFont(new Font("Helvetica", 20));
            lab.setTextFill(Color.WHITE);
            lab.setWrapText(true);
            slidePane.setPrefSize(slideWidth, slideHeight);
            slidePane.getChildren().addAll(bgRegion, lab);
            displayPane.getChildren().add(slidePane);
        }

        if (isDrawPaneVisible) {
            drawPane.setSlideDrawing(presentationElement.getSlide(currentSlideNumber).getCurrentSlideDrawing());
            displayPane.getChildren().add(drawPane);
        }

        if (isShowBlack)
            displayPane.getChildren().add(blackRegion);

        controlsPane.setBottom(presControls);

        if (isDrawModeOn)
            controlsPane.setLeft(drawControls);
        else
            controlsPane.setLeft(null);
        displayPane.getChildren().add(controlsPane);

        if (eraseCursorPane != null)
            displayPane.getChildren().add(eraseCursorPane);

        resize();
    }

    //targetPresentationState[0] = current slide number, [1] = current sequence number
    public boolean goToSlideElement(Integer[] targetPresentationState) {
        boolean successState = false;

        logger.info("Requested move to Slide: " + targetPresentationState[0] + " Element: " + targetPresentationState[1]);
        //If moving to target slide succeeds, move to target Element
        if (goToSlide(targetPresentationState[0])) {
            successState = goToElement(targetPresentationState[1]);
        } else {
            successState = false;
        }

        return successState;
    }

    public boolean goToElement(int targetElementNumber) {
        int direction = Slide.SLIDE_NO_MOVE;
        int currentSequenceNumber = presentationElement.getSlide(currentSlideNumber).getCurrentSequenceNumber();

        //Check TargetElement number exists
        if (currentSequenceNumber == targetElementNumber) return true;
        else if (currentSequenceNumber > targetElementNumber) direction = Slide.SLIDE_BACKWARD;
        else if (currentSequenceNumber < targetElementNumber) direction = Slide.SLIDE_FORWARD;

        //Attempt to stop infinite loops (defense against faulty controlpresentation logic)
        int numElements = presentationElement.getSlide(currentSlideNumber).getMaxSequenceNumber();
        int i = 0;

        do {
            elementAdvance(presentationElement.getSlide(currentSlideNumber), direction);
            if (i++ == numElements) return false;
        }
        while (presentationElement.getSlide(currentSlideNumber).getCurrentSequenceNumber() != targetElementNumber);

        return true;
    }

    /**
     * Go to a specific slide number
     *
     * @param targetSlideNumber Slide to traverse to.
     * @author Amrik Sadhra
     */
    public boolean goToSlide(int targetSlideNumber) {
        if(targetSlideNumber == currentSlideNumber) return true;
        //If target slide invalid, do nothing and log warning
        if ((targetSlideNumber < 0) || (targetSlideNumber > presentationElement.getMaxSlideNumber())) {
            logger.warn("Target slide number lies outside that which is available in this presentation. Modify XML to account for this.");
            return false;
        }


        if (targetSlideNumber < currentSlideNumber) {
            while (currentSlideNumber != targetSlideNumber) {
                slideAdvance(presentationElement, Slide.SLIDE_BACKWARD);
            }
        } else if (targetSlideNumber > currentSlideNumber) { //If we need to go forwards, go forwards
            while (currentSlideNumber != targetSlideNumber) {
                slideAdvance(presentationElement, Slide.SLIDE_FORWARD);
            }
        }

        //Update progress bar
        slideProgress(presentationElement);

        return true;
    }

    private void setFullscreen(boolean fullscreen) {
        presentationStage.setFullScreen(fullscreen);
        isFullscreen = fullscreen;
        if (fullscreen) {
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

    private void destroyAllElements() {
        for (SlideElement slideElement : presentationElement.getSlide(currentSlideNumber).getSlideElementList())
            slideElement.destroyElement();
    }

    public SlideElement getElement(int elementID) {
        SlideElement slideElement = presentationElement.getSlide(currentSlideNumber).getSlideElementList().get(elementID);
        return slideElement;
    }

    private void setCursorState(CursorState cursorState) {
        logger.trace("Cursor state: " + cursorState.name());
        currentCursorState = cursorState;
        if (displayPane.getChildren().contains(eraseCursorPane)) {
            displayPane.getChildren().remove(eraseCursorPane);
        }
        eraseCursorPane = null;

        switch (cursorState) {
            case DEFAULT:
                scene.setCursor(Cursor.DEFAULT);
                displayPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, disabledCursorFilter);
                break;
            case HIDDEN:
                scene.setCursor(Cursor.NONE);
                displayPane.addEventFilter(MouseEvent.MOUSE_CLICKED, disabledCursorFilter);
                break;
            case DRAW:
                Dimension2D drawCursorDimension = ImageCursor.getBestSize(32, 32); //TODO use constants for size
                ImageCursor drawCursor = new ImageCursor(new Image("file:projectResources/cursors/drawCursor.png", drawCursorDimension.getWidth(), drawCursorDimension.getHeight(), true, true), 0, Double.MAX_VALUE);
                scene.setCursor(drawCursor);
                break;
            case ERASE:
                scene.setCursor(Cursor.NONE);
                ImageView eraseCursor = new ImageView(new Image("file:projectResources/cursors/eraseCursor.png", drawPane.getEraserSize(), drawPane.getEraserSize(), true, true));
                eraseCursor.setMouseTransparent(true);

                eraseCursorPane = new Pane(eraseCursor);
                eraseCursorPane.setMouseTransparent(true);
                displayPane.addEventFilter(MouseEvent.ANY, event -> {
                    eraseCursor.setTranslateX(event.getX());
                    eraseCursor.setTranslateY(event.getY());
                });

                displayPane.getChildren().add(eraseCursorPane);
                break;
            default:
                //This should never be reached
        }
    }

    public void setIsEmbeddedBrowserOpen(boolean isOpen) {
        isEmbeddedBrowserOpen = isOpen;
    }

    public int getCurrentSlideNumber() {
        return currentSlideNumber;
    }

    //Sequence Number Listener implementations:
    CopyOnWriteArrayList<SimpleChangeListener> sequenceChangeListeners = new CopyOnWriteArrayList<SimpleChangeListener>();

    /**
     * Adds a listener which will be notified whenever the sequence number changes.
     * listeners are notified by calling notifySequenceChangeListeners()
     *
     * @param listener
     */
    public void addSequenceChangeListener(SimpleChangeListener listener) {
        sequenceChangeListeners.add(listener);
    }

    /**
     * Notifies all of the current sequence change listeners of a change.
     */
    public void notifySequenceChangeListeners(int oldVal, int newVal) {
        for (SimpleChangeListener listener : sequenceChangeListeners) {
            listener.changed(oldVal, newVal);
        }
    }

    public void removeSequenceChangeListener(SimpleChangeListener listener) {
        sequenceChangeListeners.remove(listener);
    }

    public Presentation getPresentationElement() {
        return presentationElement;
    }


    //Slide Changed Event:
    CopyOnWriteArrayList<SimpleChangeListener> slideChangeListeners = new CopyOnWriteArrayList<>();

    public void addSlideChangeListener(SimpleChangeListener listener){
        slideChangeListeners.add(listener);
    }

    public void removeSlideChangeListener(SimpleChangeListener listener){
        slideChangeListeners.remove(listener);
    }

    public void notifySlideChangeListener(int oldVal, int newVal){
        for(SimpleChangeListener listener: slideChangeListeners){
            listener.changed(oldVal, newVal);
        }
    }

    public void setWordCloudActive(boolean wordCloudActive) {
        this.wordCloudActive = wordCloudActive;
    }
}
