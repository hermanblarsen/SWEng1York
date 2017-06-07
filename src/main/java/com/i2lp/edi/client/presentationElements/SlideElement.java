package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.animation.Animation;
import com.i2lp.edi.client.animation.PathAnimation;
import com.i2lp.edi.client.animation.TranslationAnimation;
import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.utilities.SimpleChangeListener;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by habl on 23/02/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class SlideElement {
    Logger logger = LoggerFactory.getLogger(getClass());



    protected EdiManager ediManager;
    protected float duration = -1;



    protected int slideID; //Needed for CSS generation, CSS filename needs this to identify what to apply

    protected String presentationID; //Needed for CSS generation
    protected int elementID;
    protected int layer = 1;
    protected boolean visibility = true;
    protected int startSequence = 0;
    protected int endSequence = -1;
    protected String onClickAction;
    protected String onClickInfo;
    protected Pane slideCanvas;
    Animation startAnimation, endAnimation;
    boolean onCanvas = false;
    protected double slideWidth;
    protected double slideHeight;
    protected boolean teacher;
    protected SlideElement mediaElement;
    protected boolean isForceMute;

    public abstract void doClassSpecificRender();

    public void removeElement() {
        destroyElement();
        if (onCanvas) {
            slideCanvas.getChildren().remove(getCoreNode());
            onCanvas = false;
        }
    }

    public void addCoreNodeToSlide() {
        if (!onCanvas) {
            onCanvas = true;
            slideCanvas.getChildren().add(getCoreNode());
        }
    }

    //Empty interface for tagging our actual slide elements
    public void renderElement(int animationType) {
        //Added to the canvas at render time, as otherwise negates use of VisibleSet
        //If we bind to canvas, the element is always visible. Ignoring the sequencing and anims.
        //Add CoreNode to the Pane
        if (getCoreNode() == null) {
            logger.error("Tried to set slide internalCanvas before Element constructor was called!");
        } else {
            //Ensure we only add an element to the Canvas once.
            addCoreNodeToSlide();
            doClassSpecificRender();
        }

        if (!(this instanceof AudioElement)) {
            switch (animationType) {
                case Animation.NO_ANIMATION: //No animation (click)
                    logger.info("No animation");
                    break;
                case Animation.ENTRY_ANIMATION: //Entry animation (playback)
                    if (startAnimation != null) {//animation Exists as StartSequence Present
                        startAnimation.setCoreNodeToAnimate(getCoreNode());

                        if(startAnimation instanceof TranslationAnimation ){
                            ((TranslationAnimation) startAnimation).setScaleFactor(getSlideWidth(), getSlideHeight());
                        } else if(startAnimation instanceof PathAnimation){
                            ((PathAnimation) startAnimation).setScaleFactor(getSlideWidth(), getSlideHeight());
                        }

                        getCoreNode().setVisible(isVisibility());
                        startAnimation.play();

                        logger.info("Entry animation playing");
                    } else {
                        // If there's no animation to show the element then just make it visible
                        getCoreNode().setVisible(isVisibility());
                    }
                    break;
                case Animation.EXIT_ANIMATION: //Exit animation (playback)
                    if (endAnimation != null) {//animation Exists as EndSequence Present
                        endAnimation.setCoreNodeToAnimate(getCoreNode());

                        if(endAnimation instanceof TranslationAnimation ){
                            ((TranslationAnimation) endAnimation).setScaleFactor(getSlideWidth(), getSlideHeight());
                        } else if(endAnimation instanceof PathAnimation){
                            ((PathAnimation) endAnimation).setScaleFactor(getSlideWidth(), getSlideHeight());
                        }

                        endAnimation.play();

                        logger.info("Exit animation playing");
                    } else {
                        getCoreNode().setVisible(false);
                    }
                    break;
            }
        }
        if (animationType == Animation.EXIT_ANIMATION) {
            destroyElement();
        }
    }

    public abstract Node getCoreNode();

    public abstract void setupElement();

    /**
     * Must be called whenever an element is no longer on screen / active.
     * This method should be used as an opportunity to halt, cleanup the element, and to return it to a state where no
     * elements of it have any effect.
     * If you disagree with this description then let me know -Zain
     */
    public abstract void destroyElement();

    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }

    public EdiManager getEdiManager() {
        return ediManager;
    }

    public void setSlideCanvas(Pane slideCanvas) {
        this.slideCanvas = slideCanvas;
        setupElement();
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public int getStartSequence() {
        return startSequence;
    }

    public void setStartSequence(int startSequence) {
        this.startSequence = startSequence;
    }

    public int getEndSequence() {
        if(endSequence == 0){
            endSequence = startSequence;
        }
        return endSequence;
    }

    public void setEndSequence(int endSequence) {
        this.endSequence = endSequence;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public int getElementID() {
        return elementID;
    }

    public void setElementID(int elementID) {
        this.elementID = elementID;
    }

    public String getOnClickAction() {
        return onClickAction;
    }

    public void setOnClickAction(String onClickAction) {
        this.onClickAction = onClickAction;
    }

    public String getOnClickInfo() {
        return onClickInfo;
    }

    public void setOnClickInfo(String onClickInfo) {
        this.onClickInfo = onClickInfo;
    }

    public void setSlideID(int slideID) {
        this.slideID = slideID;
    }

    public void setPresentationID(String presentationID) {
        this.presentationID = presentationID;
    }

    public boolean isTeacher() {
        return teacher;
    }

    public void setTeacher(boolean teacher) {
        this.teacher = teacher;
    }

    public double getSlideWidth() {
        return slideWidth;
    }

    public void setSlideWidth(double slideWidth) {
        this.slideWidth = slideWidth;
    }

    public double getSlideHeight() {
        return slideHeight;
    }

    public void setSlideHeight(double slideHeight) {
        this.slideHeight = slideHeight;
    }

    protected void scaleDimensions(float xPosition, float yPosition) {
        //Convert position percentages to multipliers against canvas size and update location
        getCoreNode().setTranslateX(xPosition * slideWidth);
        getCoreNode().setTranslateY(yPosition * slideHeight);
    }

    protected void performOnClickAction(){
        SlideElement slideElement = this;
        if (onClickAction != null) {
            logger.info("Performing onClickAction: \"" + onClickAction + "\" with onClickInfo: \"" + onClickInfo +"\"");
            switch (onClickAction) {
                case "openwebsite":
                    if(!(slideElement instanceof InteractiveElement)) {
                        logger.info("Opening Website: " + onClickInfo);
                        openEmbeddedBrowser();
                    }else{
                        if(!(((InteractiveElement) slideElement).elementActive)){
                            logger.info("Opening Website: " + onClickInfo);
                            openEmbeddedBrowser();
                        }
                    }

                    break;
                case "gotoslide":
                    if(!(slideElement instanceof InteractiveElement)) {
                        //ediManager.getPresentationManager().goToSlide(Integer.parseInt(onClickInfo));
                        ediManager.getPresentationManager().goToSlideID(Integer.parseInt(onClickInfo));
                    }else{
                        if(!(((InteractiveElement) slideElement).elementActive)){
                            //ediManager.getPresentationManager().goToSlide(Integer.parseInt(onClickInfo));
                            ediManager.getPresentationManager().goToSlideID(Integer.parseInt(onClickInfo));
                        }
                    }
                    break;

                case "dynamicmediatoggle":

                    SlideElement se = ediManager.getPresentationManager().getElement(Integer.parseInt(onClickInfo));
                    if (se instanceof VideoElement) {
                        if (((VideoElement) se).getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING) {
                            ((VideoElement) se).getMediaPlayer().pause();
                        } else {
                            ((VideoElement) se).getMediaPlayer().play();
                        }
                    }
                    if (se instanceof AudioElement) {
                        if (((AudioElement) se).getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING) {
                            ((AudioElement) se).getMediaPlayer().pause();
                        } else {
                            ((AudioElement) se).getMediaPlayer().play();
                        }
                    }
                    break;

                default:
                    logger.info("OnClickAction: " + onClickAction + " with onClick info: " + onClickInfo + " for ElementID: " + getElementID() + " not recognised.");
                    break;
            }
        }
        else logger.info("Element with ElementID: " + getElementID() + " has no OnClickAction");
    }

    BorderPane embeddedBrowserPane = new BorderPane();;
    WebView webView = new WebView();
    WebEngine engine = webView.getEngine();

    final SimpleChangeListener sequenceChangeListener = new SimpleChangeListener() {
        //Changing Sequence so close the browser
        @Override
        public void changed(Object oldVal, Object newVal) {
            closeEmbeddedBrowser();
        }
    };

    public void openEmbeddedBrowser() {
        HBox browserToolbar = new HBox();
        //webView = new WebView();


        //engine = webView.getEngine();

        //Load the page:
        webView.setPrefWidth(getSlideWidth());
        webView.setPrefHeight(getSlideHeight());

        Text exitButton = GlyphsDude.createIcon(FontAwesomeIcon.CLOSE);
        Text backButton = GlyphsDude.createIcon(FontAwesomeIcon.CHEVRON_LEFT);
        Text forwardButton = GlyphsDude.createIcon(FontAwesomeIcon.CHEVRON_RIGHT);
        Text reloadButton = GlyphsDude.createIcon(FontAwesomeIcon.REFRESH);
        TextField browserLocation = new TextField(engine.getLocation());

        //Setup the toolbar
        exitButton.setFill(Color.WHITE);
        backButton.setFill(Color.WHITE);
        forwardButton.setFill(Color.WHITE);
        reloadButton.setFill(Color.WHITE);
        browserLocation.setEditable(false);

        //Stylise the toolbar:
        browserToolbar.setHgrow(browserLocation, Priority.ALWAYS);
        browserToolbar.setAlignment(Pos.CENTER_LEFT);
        browserToolbar.setSpacing(10);
        browserToolbar.setPadding(new Insets(5, 12, 5, 12));
        browserToolbar.setStyle("-fx-background-color:#34495e");

        //Toolbar listeners
        engine.locationProperty().addListener((obs, oldVal, newVal) -> browserLocation.setText(newVal));

        backButton.setOnMouseClicked(event -> {
            logger.info("Web browser Going Back");
            WebHistory history = engine.getHistory();
            if (history.getCurrentIndex() > 0) {
                history.go(-1);
            }
        });

        forwardButton.setOnMouseClicked(event -> {
            logger.info("Web browser Going forward");
            WebHistory history = engine.getHistory();
            if (history.getCurrentIndex() != history.getEntries().size() - 1) {
                history.go(1);
            }
        });

        reloadButton.setOnMouseClicked(event -> {
            logger.info("Webrowser reloading page");
            engine.reload();
        });

        final RotateTransition rot = new RotateTransition(Duration.seconds(1.5), reloadButton);
        rot.setCycleCount(javafx.animation.Animation.INDEFINITE);
        rot.setFromAngle(0);
        rot.setToAngle(360);
        rot.setInterpolator(Interpolator.LINEAR);
        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.RUNNING) {
                rot.play();
            } else {
                rot.pause();
            }
        });

        //exitButton.setOnMouseClicked(event -> closeEmbeddedBrowser());
        exitButton.addEventFilter(MouseEvent.MOUSE_CLICKED,event->{
            logger.info("Closing browser");
            closeEmbeddedBrowser();
            event.consume();
        });
        //Add things to the toolbar
        browserToolbar.getChildren().add(exitButton);
        browserToolbar.getChildren().add(backButton);
        browserToolbar.getChildren().add(forwardButton);
        browserToolbar.getChildren().add(reloadButton);
        browserToolbar.getChildren().add(browserLocation);

        embeddedBrowserPane.setTop(browserToolbar);
        embeddedBrowserPane.setCenter(webView);

        ediManager.getPresentationManager().setIsEmbeddedBrowserOpen(true); //To disable the presentation control hotkeys
        slideCanvas.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                keyEvent.consume();
                closeEmbeddedBrowser();
                slideCanvas.setOnKeyPressed(null);
            }
        });
        if(!slideCanvas.getChildren().contains(embeddedBrowserPane)) {
            slideCanvas.getChildren().add(embeddedBrowserPane);
        }
        embeddedBrowserPane.toFront();

        //Listeners to resize and close the browser.
        slideCanvas.widthProperty().addListener(e -> webView.setPrefWidth(getSlideWidth()));
        slideCanvas.heightProperty().addListener(e -> webView.setPrefHeight(getSlideHeight()));
        ediManager.getPresentationManager().addSequenceChangeListener(sequenceChangeListener);

        engine.load(onClickInfo);
    }

    public void closeEmbeddedBrowser() {
        slideCanvas.getChildren().remove(embeddedBrowserPane);
        engine.load(null);
        //embeddedBrowserPane.setVisible(false);
        ediManager.getPresentationManager().setIsEmbeddedBrowserOpen(false);
        ediManager.getPresentationManager().removeSequenceChangeListener(sequenceChangeListener);
    }

    public boolean isForceMute() {
        return isForceMute;
    }

    public void setForceMute(boolean isForceMute) { this.isForceMute = isForceMute; }

    public void setStartAnimation(Animation startAnimation) {
        this.startAnimation = startAnimation;
    }

    public void setEndAnimation(Animation endAnimation) {
        this.endAnimation = endAnimation;
    }

    public Animation getStartAnimation() {
        return startAnimation;
    }

    public Animation getEndAnimation() {
        return endAnimation;
    }

    public int getSlideID() {
        return slideID;
    }
}
