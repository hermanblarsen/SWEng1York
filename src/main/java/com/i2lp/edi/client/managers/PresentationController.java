package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.animation.Animation;
import com.i2lp.edi.client.exceptions.SequenceNotFoundException;
import com.i2lp.edi.client.presentationElements.*;
import com.i2lp.edi.client.presentationViewer.StudentPresentationController;
import com.i2lp.edi.client.presentationViewer.TeacherPresentationController;
import com.i2lp.edi.client.utilities.ParserXML;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
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
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.i2lp.edi.client.utilities.Utils.getFileParentDirectory;


/**
 * Created by kma517 on 16/03/2017.
 */
public abstract class PresentationController {
    private static final float SLIDE_SIZE_ON_OPEN = 0.5f;
    private static final int PRES_CONTROLS_HEIGHT = 40;
    private static final int STAGE_MIN_WIDTH = 300;
    private static final int STAGE_MIN_HEIGHT = 300;
    private static final int HIDE_CURSOR_DELAY = 2000;
    private static final double MAX_ERASER_SIZE = 20;
    Logger logger = LoggerFactory.getLogger(PresentationController.class);

    protected String xmlPath = null;

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
    private boolean mouseDown = false;
    private EventHandler<MouseEvent> disabledCursorFilter;
    private EventHandler<MouseEvent> eraseCursorFilter;
    private BorderPane controlsPane;
    private HBox presControls;
    private VBox drawControls;
    private Region blackRegion;
    private DrawPane drawPane;
    private ImageView visibilityButton;
    private Popup colourPopup;
    private ImageView eraseCursor;

    private CursorState cursorState = CursorState.DEFAULT;

    protected double slideWidth;
    protected double slideHeight;
    protected int currentSlideNumber = 0; //Current slide number in presentation
    protected int currentSequenceNumber = 0; //Current slide number in presentation
    private boolean isMouseOverSlide = true;
    private double preFullscreenSlideWidth;
    private double preFullscreenSlideHeight;
    private boolean isMouseOverControls = false;
    private boolean isDrawModeOn = false;
    private boolean isDrawPaneVisible = true;


    public PresentationController() {
        presentationStage = new Stage();
        Image ediLogoSmall = new Image("file:projectResources/logos/ediLogo32x32.png");
        presentationStage.getIcons().add(ediLogoSmall);
        presentationStage.setMinWidth(STAGE_MIN_WIDTH);
        presentationStage.setMinHeight(STAGE_MIN_HEIGHT);
        presentationStage.setOnCloseRequest(event -> destroyAllElements());

        sceneBox = new VBox();
        sceneBox.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if(colourPopup != null) {
                if (colourPopup.isShowing() && !event.getTarget().equals(colourPopup)) {
                    colourPopup.hide();
                }
            }
        });
        displayPane = new StackPane();
        displayPane.addEventFilter(MouseEvent.ANY, event -> {
            if(event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
                mouseDown = true;
            } else if(event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
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
        controlsPane.addEventFilter(MouseEvent.ANY, event -> {
            //logger.info("Caught event: " + event.toString());
            if((event.getEventType().equals(MouseEvent.MOUSE_PRESSED)
                    || event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)
                    || event.getEventType().equals(MouseEvent.MOUSE_RELEASED))
                    && event.getTarget().equals(controlsPane)) {
                //logger.info("Diverting event " + event.toString() + " to canvas");
                Event.fireEvent(drawPane.getCanvas(), (Event) event.clone());
            }
        });
        controlsPane.setPickOnBounds(false);
        //displayPane.setPickOnBounds(false);
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
                toBeAssigned.setPresentationController(this); //Needed for onClickAction
                toBeAssigned.setSlideID(toAssign.getSlideID());
                toBeAssigned.setPresentationID(myPresentationElement.getDocumentID());
                if (this instanceof TeacherPresentationController) {
                    toBeAssigned.setTeacher(true);
                } else {
                    toBeAssigned.setTeacher(false);
                }
                //setupElement is called from within setSlideCanvas.  setupElement needs to be done for all elements.
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
        loadPresentation(path);

        presentationStage.setTitle(presentationElement.getDocumentTitle());
        slideNumber.setText("Slide 1 of " + presentationElement.getSlideList().size());

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
        resize();

        if (presentationElement.isAutoplayPresentation()){
            autoPlay();
        }
    }

    private void addKeyboardListeners() {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
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
                if (!mouseDown && !mouseMoved && cursorState.equals(CursorState.DEFAULT) && isMouseOverSlide && !isMouseOverControls)
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
        this.xmlPath = getFileParentDirectory(path);

        ParserXML xmlParser = new ParserXML(path);
        presentationElement = xmlParser.parsePresentation();

        //TEST PRESENTATION, STOP USING
        //logger.info("Bypassing file located at: " + path + ", programmatically making tet presentation instead.");
        //presentationElement = Presentation.generateTestPresentation();  //TODO REMOVE AND STOP USING

        assignAttributes(presentationElement);
        displayCurrentSlide();
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
            setDrawPaneVisible(true);
            setCursorState(CursorState.DRAW);
            drawPane.setActive(true);
            controlsFadeIn(drawControls);
        } else {
            isDrawModeOn = false;
            setCursorState(CursorState.DEFAULT);
            drawPane.setActive(false);
        }

        displayCurrentSlide();
    }

    private void setDrawPaneVisible(boolean setVisible) {
        if(setVisible) {
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

        Image hiddenIcon = new Image("file:projectResources/icons/eyeHidden.png", 30, 30, true, true);
        Image visibleIcon = new Image("file:projectResources/icons/eyeVisible.png", 30, 30, true, true);

        if(isDrawPaneVisible)
            visibilityButton = new ImageView(hiddenIcon);
        else
            visibilityButton = new ImageView(visibleIcon);

        visibilityButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(isDrawPaneVisible) {
                setDrawPaneVisible(false);
            } else {
                setDrawPaneVisible(true);
            }
        });
        visibilityButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> visibilityButton.setEffect(shadow));
        visibilityButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> visibilityButton.setEffect(null));

        StackPane progressBar = new StackPane();
        this.progressBar.setMinSize(200, 10);
        progressBar.getChildren().addAll(this.progressBar, slideNumber);

        presControls.getChildren().addAll(backButton, nextButton, fullScreenButton, specificFeats, commentButton, drawButton, visibilityButton, progressBar);
        if (this instanceof StudentPresentationController) {

        }

        addMouseHandlersToControls(presControls);

        presControls.setMaxHeight(PRES_CONTROLS_HEIGHT);
        presControls.setAlignment(Pos.BOTTOM_LEFT);
        return presControls;
    }

    private VBox addDrawControls() {
        VBox drawControls = new VBox(5);
        drawControls.setStyle("-fx-background-color:transparent");//#34495e
        drawControls.setPadding(new Insets(5, 12, 5, 12));

        DropShadow shadow = new DropShadow();

        Image undoIcon = new Image("file:projectResources/icons/undo.png", 30, 30, true, true);
        ImageView undoButton = new ImageView(undoIcon);
        undoButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            drawPane.setSlideDrawing(presentationElement.getSlide(currentSlideNumber).getPreviousSlideDrawing());
        });
        undoButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> undoButton.setEffect(shadow));
        undoButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> undoButton.setEffect(null));

        Image redoIcon = new Image("file:projectResources/icons/redo.png", 30, 30, true, true);
        ImageView redoButton = new ImageView(redoIcon);
        redoButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            drawPane.setSlideDrawing(presentationElement.getSlide(currentSlideNumber).getNextSlideDrawing());
        });
        redoButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> redoButton.setEffect(shadow));
        redoButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> redoButton.setEffect(null));

        Image eraserIcon = new Image("file:projectResources/icons/erase.png", 30, 30, true, true);
        ImageView eraserButton = new ImageView(eraserIcon);
        eraserButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(drawPane.isEraserMode()) {
                drawPane.setEraserMode(false);
                setCursorState(CursorState.DRAW);
            } else {
                drawPane.setEraserMode(true);
                //setCursorState(CursorState.ERASE); //TODO: Fix
            }
        });
        eraserButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> eraserButton.setEffect(shadow));
        eraserButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> eraserButton.setEffect(null));

        Image colourIcon = new Image("file:projectResources/icons/selectBrushColour.png", 30, 30, true, true);
        ImageView colourButton = new ImageView(colourIcon);
        ColorPicker colorPicker = new ColorPicker(drawPane.getBrushColor());
        colourButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            colourPopup = new Popup();
            colorPicker.setOnAction(event1 -> {
                drawPane.setBrushColor(colorPicker.getValue());
                colourPopup.hide();
            });
            colourPopup.getContent().add(colorPicker);
            colourPopup.show(presentationStage, event.getScreenX(), event.getScreenY());
        });
        colourButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> colourButton.setEffect(shadow));
        colourButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> colourButton.setEffect(null));

        Image widthIcon = new Image("file:projectResources/icons/selectBrushWidth.png", 30, 30, true, true);
        ImageView widthButton = new ImageView(widthIcon);
        widthButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            Popup widthPopup = new Popup();
            Slider widthSlider;
            widthPopup.setAutoHide(true);
            if(!drawPane.isEraserMode()) {
                widthSlider = new Slider(0.1, 10, drawPane.getBrushWidth());
            } else {
                widthSlider = new Slider(1, MAX_ERASER_SIZE, drawPane.getEraserSize());
            }
            widthPopup.setOnAutoHide(event1 -> {
                if(!drawPane.isEraserMode()) {
                    drawPane.setBrushWidth(widthSlider.getValue());
                } else {
                    drawPane.setEraserSize(widthSlider.getValue()); //TODO: adjust cursor size
                    //setCursorState(CursorState.ERASE); //TODO: fix
                }
            });
            widthSlider.setOnMouseReleased(event1 -> {
                if(!drawPane.isEraserMode()) {
                    drawPane.setBrushWidth(widthSlider.getValue());
                } else {
                    drawPane.setEraserSize(widthSlider.getValue()); //TODO: adjust cursor size
                    //setCursorState(CursorState.ERASE); //TODO: fix
                }
                widthPopup.hide();
            });
            widthPopup.getContent().add(widthSlider);
            widthPopup.show(presentationStage, event.getScreenX(), event.getScreenY());
        });
        widthButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            widthButton.setEffect(shadow);
            //setCursorState(CursorState.DEFAULT); //TODO: fix
        });
        widthButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            widthButton.setEffect(null);
            //setCursorState(CursorState.ERASE); //TODO: fix
        });

        Image deleteIcon = new Image("file:projectResources/icons/trash.png", 30, 30, true, true);
        ImageView deleteButton= new ImageView(deleteIcon);
        deleteButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            drawPane.clear();
            presentationElement.getSlide(currentSlideNumber).addSlideDrawing(drawPane.getSlideDrawing());
        });
        deleteButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> deleteButton.setEffect(shadow));
        deleteButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> deleteButton.setEffect(null));

        drawControls.getChildren().addAll(undoButton, redoButton, eraserButton, colourButton, widthButton, deleteButton);
        drawControls.setAlignment(Pos.CENTER_LEFT);

        addMouseHandlersToControls(drawControls);

        return drawControls;
    }

    private void addMouseHandlersToControls(Node controls) {
        controls.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            controlsFadeIn(controls);
            isMouseOverControls = true;
        });
        controls.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            controlsFadeOut(controls);
            isMouseOverControls = false;
        });

        controlsFadeIn(controls);
    }

    private void controlsFadeIn(Node controls) {
        FadeTransition ft0 = new FadeTransition(Duration.millis(500), controls);
        ft0.setFromValue(0.0);
        ft0.setToValue(1.0);
        ft0.play();

        Timer hideControlsTimer = new Timer(true);
        hideControlsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                FadeTransition ft0 = new FadeTransition(Duration.millis(500), controls);
                ft0.setFromValue(1.0);
                ft0.setToValue(0.0);
                if(!isMouseOverControls)
                    ft0.play();
            }
        }, (long) HIDE_CURSOR_DELAY);
    }

    private void controlsFadeOut(Node controls) {
        FadeTransition ft0 = new FadeTransition(Duration.millis(500), controls);
        ft0.setFromValue(1.0);
        ft0.setToValue(0.0);
        ft0.play();
    }

    protected void slideProgress(Presentation presentation) {
        //Make sure currentSlideNumber doesn't overflow

        int slideNumber = currentSlideNumber + 1;

        //Calculate the total number of sequences in the presentation
        int sequenceNumberMax=0;
        for (Slide slide: presentation.getSlideList()) {
            sequenceNumberMax+=slide.getMaxSequenceNumber();
            sequenceNumberMax++;
        }
        //Make sure the current sequence doesn't go out of bounds
        if (currentSequenceNumber >= sequenceNumberMax) currentSequenceNumber = sequenceNumberMax;
        if (currentSequenceNumber <=0) currentSequenceNumber = 0;

        int slideNumberMax = presentation.getSlideList().size();

        //Calculate progress and reset text in progressbar
        float slideProgress = (float) (currentSequenceNumber) / sequenceNumberMax;
        progressBar.setProgress((double)slideProgress);
        this.slideNumber.setText("Slide " + slideNumber + " of " + slideNumberMax);
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
                        presentationElement.getSlide(currentSlideNumber).destroyAllVisible(); // Destroy any remaining elements
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
                    presentationElement.getSlide(currentSlideNumber).destroyAllVisible(); //Destroy any remaining elements on the slide
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
        currentSequenceNumber++;
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
            currentSequenceNumber--;
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
//            if(direction == Slide.SLIDE_FORWARD) {
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
            logger.error("Couldnt finalize PresCon");
        }
    }

    private void setCursorState(CursorState state) {
        cursorState = state;
        eraseCursor = null;
        if(eraseCursorFilter != null)
            displayPane.removeEventFilter(MouseEvent.ANY, eraseCursorFilter);
        eraseCursorFilter = null;
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
                Dimension2D drawCursorDimension = ImageCursor.getBestSize(32, 32); //TODO use constants for size
                ImageCursor drawCursor = new ImageCursor(new Image("file:projectResources/cursors/drawCursor.png", drawCursorDimension.getWidth(), drawCursorDimension.getHeight(), true, true), 0, Double.MAX_VALUE);
                scene.getRoot().setCursor(drawCursor);
                break;
            case ERASE:
                scene.getRoot().setCursor(Cursor.NONE);
                Image eraseIcon = new Image("file:projectResources/cursors/eraseCursor.png", drawPane.getEraserSize(), drawPane.getEraserSize(), true, true);
                eraseCursor = new ImageView(eraseIcon);
                displayPane.getChildren().add(eraseCursor);
                StackPane.setAlignment(eraseCursor, Pos.TOP_LEFT);
                eraseCursorFilter = event -> {
                    if(event.getEventType().equals(MouseEvent.MOUSE_MOVED) || event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)){
                        eraseCursor.setTranslateX(event.getX());
                        eraseCursor.setTranslateY(event.getY());
                    }
                };
                sceneBox.addEventFilter(MouseEvent.ANY, eraseCursorFilter);
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

        if(isDrawPaneVisible) {
            drawPane.setSlideDrawing(presentationElement.getSlide(currentSlideNumber).getCurrentSlideDrawing());
            displayPane.getChildren().add(drawPane);
        }

        if (isShowBlack)
            displayPane.getChildren().add(blackRegion);

        controlsPane.setBottom(presControls); //TODO: Fix glitch when transitioning between slides

        if(isDrawModeOn)
            controlsPane.setLeft(drawControls);
        else
            controlsPane.setLeft(null);
        displayPane.getChildren().add(controlsPane);

        if(eraseCursor != null) {
            displayPane.getChildren().add(eraseCursor);
            StackPane.setAlignment(eraseCursor, Pos.TOP_LEFT);
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
        } else if (targetSlideNumber > currentSlideNumber) { //If we need to go forwards, go forwards
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

    private void destroyAllElements() {
        for(SlideElement slideElement : presentationElement.getSlide(currentSlideNumber).getSlideElementList())
            slideElement.destroyElement();
    }

    public String getXmlPath() {
        return xmlPath;
    }
}

enum CursorState {
    DEFAULT,
    HIDDEN,
    DRAW,
    ERASE
}
