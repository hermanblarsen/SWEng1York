package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.StudentSession;
import com.i2lp.edi.client.TeacherSession;
import com.i2lp.edi.client.animation.Animation;
import com.i2lp.edi.client.exceptions.SequenceNotFoundException;
import com.i2lp.edi.client.presentationElements.*;
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
    public static final int HIDE_CONTROLS_DELAY = 5000;
    public static final int MIN_ERASER_SIZE = 10;
    private static final double MAX_ERASER_SIZE = 30;
    public static final double DEFAULT_BUTTON_WIDTH = 35;
    public static final double DEFAULT_BUTTON_HEIGHT = 35;

    private boolean alone = false;

    Logger logger = LoggerFactory.getLogger(PresentationManager.class);

    /* -------------- LIVE SESSION OBJECTS ----------------*/
    protected final EdiManager ediManager;
    protected TeacherSession teacherSession;
    protected StudentSession studentSession;
    //Interactive Element list for linking to ServerSide data
    private ArrayList<InteractiveElement> interactiveElementList = new ArrayList<>();

    /* -------------- UI OBJECTS ----------------*/
    protected Scene scene;
    protected StackPane displayPane;
    protected VBox sceneBox;
    protected Presentation presentationElement;
    protected ProgressBar progressBar;
    protected Label slideNumber;
    protected Boolean isFullscreen = false;
    //protected Boolean buttonsRemoved = false;
    protected Boolean questionQueueActive = false;
    protected Stage presentationStage;
    protected CommentPanel commentPanel;
    protected Boolean isCommentPanelVisible = false;
    protected Boolean isEmbeddedBrowserOpen = false;
    protected boolean isShowBlack = false;
    private boolean mouseActivityRegistered = true;
    protected boolean mouseDown = false;
    private EventHandler<MouseEvent> disabledCursorFilter;
    protected BorderPane controlsPane;
    protected HBox presControls;
    protected VBox drawControls, questionQueueControls;
    private Region blackRegion;
    protected DrawPane drawPane;
    private ImageView visibilityButton;
    public ImageView linkButton;
    private Popup colourPopup;
    protected CursorState currentCursorState = CursorState.DEFAULT;
    private Pane eraseCursorPane;
    private ImageView drawButton;
    private ImageView eraserButton;
    protected ContextMenu cMenu;

    protected double slideWidth;
    protected double slideHeight;


    protected int currentSlideNumber = 0; //Current slide number in presentation
    protected int currentSequenceNumber = 0; //Current sequence number in presentation (total)
    protected boolean isMouseOverSlide = true;
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
        presentationStage.setOnCloseRequest(event -> doCloseSequence());

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
        blackRegion = new Region();
        blackRegion.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        drawPane = new DrawPane(displayPane);
        progressBar = new ProgressBar(0);
        slideNumber = new Label();
        controlsPane = new BorderPane();
        controlsPane.setPickOnBounds(false);
        displayPane.setPickOnBounds(false);
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

    public ArrayList<InteractiveElement> getInteractiveElementList() {
        if (interactiveElementList.isEmpty()) logger.error("Interactive element list not set yet");
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

        setupLinkButton(); //Link button has to be initialised before live session begins

        beginLiveSession();//Start the live session before the presentation starts!

        presControls = addPresentationControls(); //Setup all controls after live session begins as they depend on online/offline state
        drawControls = addDrawControls();
        questionQueueControls = addQuestionQueueControls();

        //Listeners for moving through presentation
        addKeyboardListeners();
        addMouseListeners();
        addResizeListeners();

        presentationStage.setScene(scene);
        presentationStage.show();

        createCommentPanel();
        commentPanel.setSlide(this.presentationElement.getCurrentSlide()); //Required for comments to work.

        displayCurrentSlide();

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
            if (presentationElement.getPresentationMetadata().getLive()) {//If we are live, start up a TeacherSession in which to track connectivity data
                if (this instanceof PresentationManagerTeacher) {
                    teacherSession = new TeacherSession(ediManager);
                } else if (this instanceof PresentationManagerStudent) {
                    studentSession = new StudentSession(ediManager);
                }
            }
        }
    }

    public StudentSession getStudentSession() {
        return studentSession;
    }

    public TeacherSession getTeacherSession() {
        return teacherSession;
    }

    private void addKeyboardListeners() {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (!isCommentPanelVisible && !isEmbeddedBrowserOpen && !wordCloudActive) {
                if (keyEvent.getCode().equals(KeyCode.ENTER) ||
                        keyEvent.getCode().equals(KeyCode.SPACE) ||
                        keyEvent.getCode().equals(KeyCode.PAGE_DOWN) ||
                        keyEvent.getCode().equals(KeyCode.RIGHT) ||
                        keyEvent.getCode().equals(KeyCode.UP)) {
                    controlPresentation(Slide.SLIDE_FORWARD);
                } else if (keyEvent.getCode().equals(KeyCode.LEFT) ||
                        keyEvent.getCode().equals(KeyCode.BACK_SPACE) ||
                        keyEvent.getCode().equals(KeyCode.PAGE_UP) ||
                        keyEvent.getCode().equals(KeyCode.DOWN)) {
                    controlPresentation(Slide.SLIDE_BACKWARD);
                } else if (keyEvent.getCode().equals(KeyCode.F5)) {
                    toggleFullscreen();
                } else if (keyEvent.getCode().equals(KeyCode.ESCAPE) && isFullscreen) {
                    setFullscreen(false);
                } else if (keyEvent.getCode().equals(KeyCode.B) ||
                        keyEvent.getCode().equals(KeyCode.PERIOD)) {
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
                cMenu = new ContextMenu();

                MenuItem nextSequence = new MenuItem("Next sequence");
                nextSequence.setOnAction(nextEvent -> controlPresentation(Slide.SLIDE_FORWARD));
                cMenu.getItems().add(nextSequence);

                MenuItem prevSequence = new MenuItem("Previous sequence");
                prevSequence.setOnAction(prevEvent -> controlPresentation(Slide.SLIDE_BACKWARD));
                cMenu.getItems().add(prevSequence);

                MenuItem firstSequence = new MenuItem("First sequence");
                firstSequence.setOnAction(firstEvent -> {
                    while (slideAdvance(presentationElement, Slide.SLIDE_BACKWARD) != Presentation.PRESENTATION_START) ;
                    slideProgress();
                });
                cMenu.getItems().add(firstSequence);

                MenuItem lastSequence = new MenuItem("Last sequence");
                lastSequence.setOnAction(lastEvent -> {
                    while (slideAdvance(presentationElement, Slide.SLIDE_FORWARD) != Presentation.PRESENTATION_FINISH) ;
                    slideProgress();
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
        }, 0, HIDE_CURSOR_DELAY);

        displayPane.addEventFilter(MouseEvent.ANY, event -> {
            if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {
                isMouseOverSlide = true;
            } else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {
                isMouseOverSlide = false;
            }

            if (event.getEventType().equals(MouseEvent.MOUSE_MOVED)) {
                if (drawControls.getOpacity() == 0) {
                    controlsFadeInTimed(drawControls);
                }
                if (drawControls.getOpacity() == 0) {
                    controlsFadeInTimed(presControls);
                }

                if (currentCursorState.equals(CursorState.HIDDEN)) {
                    setCursorState(CursorState.DEFAULT);
                }
            }

            mouseActivityRegistered = true;
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
            if (toResize instanceof AudioElement) {
                ((AudioElement) toResize).setAutoPlayOverridden(true);
            } else if (toResize instanceof VideoElement) {
                ((VideoElement) toResize).setAutoPlayOverridden(true);
            }
            toResize.doClassSpecificRender();
            if (toResize instanceof AudioElement) {
                ((AudioElement) toResize).setAutoPlayOverridden(false);
            } else if (toResize instanceof VideoElement) {
                ((VideoElement) toResize).setAutoPlayOverridden(false);
            }
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
                if (presentationStatus == Presentation.SLIDE_LAST_ELEMENT) logger.info("On last element in slide");

                if (getTeacherSession() != null) {//If in live hosting session
                    getTeacherSession().synchroniseSlides();
                }
            } else if (presentationStatus == Presentation.PRESENTATION_START) {
                logger.info("At Presentation start");
            } else if (presentationStatus == Presentation.PRESENTATION_FINISH) {
                logger.info("At Presentation finish");
                if (!isEndPresentation) {
                    isEndPresentation = true;
                    displayCurrentSlide();
                }
            }
        }

        slideProgress();
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
            controlsFadeInTimed(drawControls);
            drawButton.setImage(new Image("file:projectResources/icons/cursor_icon.png", DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, true, true));
        } else {
            isDrawModeOn = false;
            drawPane.setActive(false);
            drawButton.setImage(new Image("file:projectResources/icons/draw.png", DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, true, true));
        }

        displayCurrentSlide();
    }

    private void setDrawPaneVisible(boolean setVisible) {
        if (setVisible) {
            isDrawPaneVisible = true;
            Image hiddenIcon = new Image("file:projectResources/icons/eyeHidden.png", DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, true, true);
            visibilityButton.setImage(hiddenIcon);
            displayCurrentSlide();
        } else {
            isDrawPaneVisible = false;
            displayPane.getChildren().remove(drawPane);
            Image visibleIcon = new Image("file:projectResources/icons/eyeVisible.png", DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, true, true);
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

        ImageView fullScreenButton = makeCustomButton("file:projectResources/icons/FullScreen_NEW.png", event -> toggleFullscreen());

        String specificFeatsIconURL;
        if (this instanceof PresentationManagerStudent) {
            specificFeatsIconURL = "file:projectResources/icons/Question_Filled.png";
        } else {
            specificFeatsIconURL = "file:projectResources/icons/TeacherToolKit.png";
        }
        ImageView specificFeats = makeCustomButton(specificFeatsIconURL, event -> {
            loadSpecificFeatures();
            if (!questionQueueActive) {
                questionQueueActive = true;
            } else {
                questionQueueActive = false;
            }
        });

        ImageView commentButton = makeCustomButton("file:projectResources/icons/SB_Filled.png", event -> {
            try {
                toggleCommentsWindow();
            } catch (NullPointerException e) {
                //Exception occasionally thrown by JavaFX's HTML editor for unknown reasons. No handling needed - do nothing.
            }
        });

        String drawIconURL;
        if (isDrawModeOn) {
            drawIconURL = "file:projectResources/icons/cursor_icon.png";
        } else {
            drawIconURL = "file:projectResources/icons/draw.png";
        }

        drawButton = makeCustomButton(drawIconURL, event -> toggleDrawingMode());

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
            presControls.getChildren().addAll(backButton, nextButton, fullScreenButton);

            if (studentSession != null) {
                presControls.getChildren().addAll(linkButton, specificFeats);
            }

            presControls.getChildren().addAll(commentButton, drawButton, visibilityButton, progressBar);
        } else {
            presControls.getChildren().addAll(backButton, nextButton, fullScreenButton);

            if (teacherSession != null) {
                presControls.getChildren().add(specificFeats);
            }

            presControls.getChildren().addAll(commentButton, drawButton, visibilityButton, progressBar);
        }
        addMouseHandlersToControls(presControls);

        presControls.setMaxHeight(PRES_CONTROLS_HEIGHT);
        presControls.setAlignment(Pos.BOTTOM_LEFT);

        return presControls;
    }

    private void setupLinkButton() {
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
    }

    protected ImageView makeCustomButton(String iconURL, EventHandler<MouseEvent> mouseClickedEventHandler) {
        ImageView button = new ImageView(new Image(iconURL, DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, true, true));
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedEventHandler);
        button.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> button.setEffect(new DropShadow()));
        button.addEventHandler(MouseEvent.MOUSE_EXITED, event -> button.setEffect(null));

        return button;
    }

    private VBox addDrawControls() {
        VBox drawControls = new VBox(5);
        drawControls.setStyle("-fx-background-color:transparent");
        drawControls.setPadding(new Insets(5, 12, 5, 12));

        ImageView undoButton = makeCustomButton("file:projectResources/icons/undo.png", event -> drawPane.setSlideDrawing(presentationElement.getSlide(currentSlideNumber).getPreviousSlideDrawing()));

        ImageView redoButton = makeCustomButton("file:projectResources/icons/redo.png", event -> drawPane.setSlideDrawing(presentationElement.getSlide(currentSlideNumber).getNextSlideDrawing()));

        String eraserIconURL;
        if (drawPane.isEraserMode())
            eraserIconURL = "file:projectResources/icons/draw.png";
        else
            eraserIconURL = "file:projectResources/icons/erase.png";

        eraserButton = makeCustomButton(eraserIconURL, event -> toggleEraserMode());

        ColorPicker colorPicker = new ColorPicker(drawPane.getBrushColor());
        ImageView colourButton = makeCustomButton("file:projectResources/icons/selectBrushColor.png", event -> {
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

    private void toggleEraserMode() {
        if (drawPane.isEraserMode()) {
            drawPane.setEraserMode(false);
            eraserButton.setImage(new Image("file:projectResources/icons/erase.png", DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, true, true));
        } else {
            drawPane.setEraserMode(true);
            eraserButton.setImage(new Image("file:projectResources/icons/draw.png", DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, true, true));
        }
    }

    protected abstract VBox addQuestionQueueControls();

    private void addMouseHandlersToControls(Node controls) {
        controls.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            if (!mouseDown) {
                controlsFadeInTimed(controls);
                isMouseOverControls = true;
                setCursorState(CursorState.DEFAULT);
            }
        });
        controls.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            controlsFadeOut(controls);
            isMouseOverControls = false;
        });

        controlsFadeInTimed(controls);
    }

    protected void controlsFadeInTimed(Node controls) {
        FadeTransition ft0 = new FadeTransition(Duration.millis(500), controls);
        ft0.setFromValue(controls.getOpacity());
        ft0.setToValue(1.0);
        ft0.play();

        hideControlsTimed(controls);
    }

    protected void hideControlsTimed(Node controls) {
        Timer hideControlsTimer = new Timer(true);
        hideControlsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                FadeTransition ft0 = new FadeTransition(Duration.millis(500), controls);
                ft0.setFromValue(controls.getOpacity());
                ft0.setToValue(0.0);
                if (!isMouseOverControls) {
                    ft0.play();
                }
            }
        }, (long) HIDE_CONTROLS_DELAY);
    }

    protected void controlsFadeOut(Node controls) {
        FadeTransition ft0 = new FadeTransition(Duration.millis(500), controls);
        ft0.setFromValue(controls.getOpacity());
        ft0.setToValue(0.0);
        ft0.play();
    }

    protected void slideProgress() {
        int totalSlideNumber = 0;
        boolean finalSlideReached = false;
        for (Slide slide : presentationElement.getSlideList()) {
            totalSlideNumber++;
        }
        double slideProgress;
        if(!isEndPresentation) {
            slideProgress = (float) (currentSlideNumber) / totalSlideNumber;
        }else{
            slideProgress = (float) (currentSlideNumber+1) / totalSlideNumber;
        }
        progressBar.setProgress(slideProgress);

        //Make sure currentSlideNumber doesn't overflow and reset text in progressbar
        int slideNumber = currentSlideNumber + 1;
        int slideNumberMax = presentationElement.getSlideList().size();
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
                    notifySlideChangeListener(currentSlideNumber - 1, currentSlideNumber);
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
                    notifySlideChangeListener(currentSlideNumber + 1, currentSlideNumber);
                }

            }
        }
        presentationToAdvance.setCurrentSlide(presentationToAdvance.getSlideList().get(currentSlideNumber));

        if ((presentationToAdvance.getCurrentSlide().getCurrentSequenceNumber() == 0) && (currentSlideNumber != 0) && (!(presentationToAdvance.getSlide(currentSlideNumber).getSlideElementList().size() == 0)))
            slideAdvance(presentationToAdvance, direction);

        if (commentPanel != null) commentPanel.setSlide(this.presentationElement.getCurrentSlide());
        return presentationStatus;
    }

    public int elementAdvance(Slide slideToAdvance, int direction) {
        ArrayList<SlideElement> checkInVisibleSet;

        //If we're going forwards and not through all sequences in slide set
        if (alone || ((slideToAdvance.getCurrentSequenceNumber() < slideToAdvance.getMaxSequenceNumber()) && (direction == Slide.SLIDE_FORWARD))) {
            alone = false;
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
            notifySequenceChangeListeners(currentSequenceNumber - 1, currentSequenceNumber);//Notify any sequence number listeners that there has been a change
        } else if ((slideToAdvance.getCurrentSequenceNumber() > 1) && (direction == Slide.SLIDE_BACKWARD)) {  //If we're going backwards and still elements left

            try {
                checkInVisibleSet = Slide.searchForSequenceElement(slideToAdvance.getSlideElementList(), slideToAdvance.getCurrentSequenceNumber());

                for (SlideElement toCheckVisible : checkInVisibleSet) {
                    if (slideToAdvance.getVisibleSlideElementList().contains(toCheckVisible)) {
                        if (!(toCheckVisible instanceof InteractiveElement)) {
                            slideToAdvance.getVisibleSlideElementList().remove(toCheckVisible);
                            toCheckVisible.removeElement();
                        }
                    }
                }
            } catch (SequenceNotFoundException e) {
                logger.warn("Failed to find Element with Sequence number of " + slideToAdvance.getCurrentSequenceNumber() + " in slideElementList.");
            }
            currentSequenceNumber--;
            slideToAdvance.setCurrentSequenceNumber(slideToAdvance.getCurrentSequenceNumber() - 1);
            notifySequenceChangeListeners(currentSequenceNumber + 1, currentSequenceNumber);//Notify any sequence number listeners that there has been a change
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

        logger.info("Current Sequence is " + slideToAdvance.getCurrentSequenceNumber());
        //Fire animations
        for (SlideElement elementToAnimate : slideToAdvance.getVisibleSlideElementList()) {
            if (elementToAnimate.getStartSequence() == slideToAdvance.getCurrentSequenceNumber()) {
                if (((elementToAnimate instanceof AudioElement || elementToAnimate instanceof VideoElement) && studentSession != null) || this instanceof ThumbnailGenerationManager) {
                    elementToAnimate.setForceMute(true);
                }
                elementToAnimate.renderElement(Animation.ENTRY_ANIMATION); //Entry Sequence
            } else if ((elementToAnimate.getEndSequence() == slideToAdvance.getCurrentSequenceNumber()) && (!(this instanceof ThumbnailGenerationManager))) {
                if (((elementToAnimate instanceof AudioElement || elementToAnimate instanceof VideoElement) && studentSession != null) || this instanceof ThumbnailGenerationManager) {
                    elementToAnimate.setForceMute(true);
                }
                elementToAnimate.renderElement(Animation.EXIT_ANIMATION); //Exit Sequence
            }
        }

        //Sort by Layer
        Slide.sortElementsByLayer(slideToAdvance.getVisibleSlideElementList());
        for (SlideElement slideElement : slideToAdvance.getVisibleSlideElementList()) {
            slideElement.removeElement();
        }
        for (SlideElement slideElement : slideToAdvance.getVisibleSlideElementList()) {
            slideElement.addCoreNodeToSlide();
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

    protected void doCloseSequence() {
        destroyAllElements();
        close();
    }

    /**
     * Shutdown the presentation manager cleanly. End Teacher/Student Session
     */
    public void close() {
        //Reset sequence numbers for presentation
        for (Slide slide : presentationElement.getSlideList()) {
            slide.setCurrentSequenceNumber(0);
        }

        if (this instanceof PresentationManagerTeacher) {
            if (teacherSession != null) {
                teacherSession.endSession();
                teacherSession = null;
            }
        } else if (this instanceof PresentationManagerStudent) {
            if (studentSession != null) {
                studentSession.endSession();
                studentSession = null;
            }
        }


        if (ediManager != null) {
            ediManager.getPresentationLibraryManager().updatePresentations(); //Update presentation information
            if (ediManager.getPresentationManager() != null) {
                ediManager.setPresentationManager(null);
            }
        }
        presentationStage.close();
    }


    protected void displayCurrentSlide() {
        displayPane.getChildren().clear();
        if (!isEndPresentation) {
            Slide slide = presentationElement.getSlide(currentSlideNumber);
            slide.setBackground(new Background(new BackgroundFill(Color.valueOf(presentationElement.getTheme().getBackgroundColour()), null, null)));
            displayPane.getChildren().add(slide);
            slideProgress();
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
        controlsPane.setRight(questionQueueControls);

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
        if (targetSlideNumber == currentSlideNumber) return true;
        //If target slide invalid, do nothing and log warning
        if ((targetSlideNumber < 0) || (targetSlideNumber > presentationElement.getMaxSlideNumber())) {
            logger.warn("Target slide number lies outside that which is available in this presentation. ");
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
        slideProgress();

        return true;
    }

    public void goToSlideID(int targetSlideID) {
        int slideID = 0;
        int slideNumber = 0;
        boolean slideFound = false;
        for (int i = 0; i < presentationElement.getMaxSlideNumber(); i++) {
            slideID = presentationElement.getSlideList().get(i).getSlideID();
            if (slideID == targetSlideID) {
                slideNumber = i;
                slideFound = true;
                break;
            }
        }
        if (slideFound) {
            logger.info("Going to slide with ID: " + slideID);
            goToSlide(slideNumber);
        }
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

    protected void setCursorState(CursorState cursorState) {
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

    public void addSlideChangeListener(SimpleChangeListener listener) {
        slideChangeListeners.add(listener);
    }

    public void removeSlideChangeListener(SimpleChangeListener listener) {
        slideChangeListeners.remove(listener);
    }

    public void notifySlideChangeListener(int oldVal, int newVal) {
        for (SimpleChangeListener listener : slideChangeListeners) {
            listener.changed(oldVal, newVal);
        }
    }

    public void setWordCloudActive(boolean wordCloudActive) {
        this.wordCloudActive = wordCloudActive;
    }
}
