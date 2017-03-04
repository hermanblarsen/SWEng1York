package utilities;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Screen;

import java.time.Duration;

/**
 * Created by habl on 26/02/2017.
 */
public class VideoElement implements SlideElement{
    protected int elementID;
    protected int layer;
    protected boolean visibility;
    protected boolean mediaControl;
    protected int startSequence;
    protected int endSequence;
    protected float duration;
    protected float xPosition;
    protected float yPosition;
    protected float xSize;
    protected float ySize;
    protected String path;
    protected String onClickAction;
    protected boolean loop;
    protected String onClickInfo;
    protected boolean aspectRatioLock;
    protected float elementAspectRatio;
    protected boolean autoplay;
    protected javafx.util.Duration startTime;
    protected javafx.util.Duration endTime;
    protected Animation startAnimation, endAnimation;
    protected Pane slideCanvas;
    protected MediaView mv;
    protected MediaPlayer mp;
    protected Media media;
    protected BorderPane mediaPane;

//todo: 1) Add Error Handling
//todo: 2) Commenting

    public VideoElement() {
        mv = new MediaView();
        mediaPane = new BorderPane();
    }

    @Override
    public void renderElement(int animationType) {
        switch(animationType){
            case 0: //No animation (click)
                media = new Media(path);
                mp = new MediaPlayer(media);
                mv.setMediaPlayer(mp);

                if(mediaControl == true) {
                    mediaPane.setBottom(mediaControl());
                }
                if(loop == true){
                    mp.setCycleCount(MediaPlayer.INDEFINITE);
                }
                mp.setAutoPlay(autoplay);
                //getSlideCanvas().getChildren().add(mediaControl());
                //mp.play();
                break;
            case 1: //Entry Animation (playback)
                startAnimation.play();
                break;
            case 2: //Exit Animation (playback)
                endAnimation.play();
                break;
        }
    }

    @Override
    public Node getCoreNode() {
        return mv;
    }
    public MediaPlayer getMediaPlayer() {return mp;}

    @Override
    public void setSlideCanvas(Pane slideCanvas) {
        this.slideCanvas = slideCanvas;
        //BorderPane mediaPane = new BorderPane();
        mediaPane.setCenter(mv);
        slideCanvas.getChildren().add(mediaPane);
    }
    public Pane getSlideCanvas() {return slideCanvas;}


    public void setLayer(int layer) {this.layer = layer;}
    public int getLayer() {
        return this.layer;
    }

    public void setStartSequence(int startSequence) {this.startSequence = startSequence;}
    public int getStartSequence() {
        return startSequence;
    }

    public void setEndSequence(int endSequence) {this.endSequence = endSequence;}
    public int getEndSequence() {return endSequence;}

    public void setMediaPath(String mediaPath) {
        this.path = mediaPath;
    }
    public String getMediaPath() {return path;}

    public void setAutoPlay(boolean isAutoPlay){
        this.autoplay = isAutoPlay;
    }
    public boolean getAutoPlay() {return autoplay;}

    public void setElementID(int ID) {this.elementID = ID;}
    public int getElementID() {return elementID;}

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
        mv.setVisible(visibility);
    }

    public boolean getVisibility() {return visibility;}

    public void setStartTime(javafx.util.Duration startTime) {
        this.startTime = startTime;
        mp.setStartTime(startTime);
    }
    public javafx.util.Duration getStartTime(){return startTime;}

    public void setEndTime(javafx.util.Duration endTime) {
        this.startTime = endTime;
        mp.setStartTime(endTime);
    }
    public javafx.util.Duration getEndTime(){return endTime;}

    public void setDuration(float duration) {this.duration = duration;}
    public float getDuration() {return duration;}

    public void setxPosition(float xPosition){
        this.xPosition = xPosition;
        getCoreNode().setTranslateX(xPosition);
    }
    public float getxPosition(){return xPosition;}

    public void setyPosition(float yPosition){
        this.yPosition = yPosition;
        getCoreNode().setTranslateX(yPosition);
    }
    public float getyPosition(){return yPosition;}

    public void setxSize(float xSize){this.xSize = xSize;}
    public float getxSize() {return xSize;}

    public void setySize(float ySize){this.ySize = ySize;}
    public float getySize() {return ySize;}

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

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean isAspectRatioLock() {
        return aspectRatioLock;
    }

    public void setAspectRatioLock(boolean aspectRatioLock) {
        this.aspectRatioLock = aspectRatioLock;
        mv.setPreserveRatio(aspectRatioLock);
    }

    public float getElementAspectRatio() {
        return elementAspectRatio;
    }

    public void setElementAspectRatio(float elementAspectRatio) {
        this.elementAspectRatio = elementAspectRatio;
    }

    public boolean isMediaControl() {
        return mediaControl;
    }

    public void setMediaControl(boolean mediaControl) {
        this.mediaControl = mediaControl;
    }

    private HBox mediaControl() {

        HBox mediaBar = new HBox();
        mediaBar.setStyle("-fx-background-color: whitesmoke");
        mediaBar.setAlignment(Pos.CENTER);
        mediaBar.setPadding(new Insets(5, 10, 5, 10));
        BorderPane.setAlignment(mediaBar, Pos.CENTER);

        //Play/Pause Button
        final Button playPauseButton = new Button(">");
        if(autoplay == true){
            playPauseButton.setText("||");
        }
        playPauseButton.setOnAction((event) -> {
            if(mp.getStatus() != MediaPlayer.Status.PLAYING) {
                playPauseButton.setText("||");
                mp.play();
            }else {
                playPauseButton.setText(">");
                mp.pause();
            }
        });
        mediaBar.getChildren().add(playPauseButton);

        //Stop Button
        final Button stopButton = new Button("STOP");
        stopButton.setOnAction((event) -> {
            mp.stop();
            playPauseButton.setText(">");
        });
        mediaBar.getChildren().add(stopButton);

        // Seek Control
        final Slider videoTime = new Slider(0.0d, 0, 0);
        mp.statusProperty().addListener((observableValue,  oldValue,  newValue)-> {
            if(newValue == MediaPlayer.Status.READY) {
                videoTime.setMax(mp.getTotalDuration().toMillis());
            }
        });
        //Update the time bar to match the current playback time.
        final Holder<Boolean> isProgrammaticChange = new Holder<>(false);
        mp.currentTimeProperty().addListener((observableValue) -> {
            isProgrammaticChange.setValue(true);
            videoTime.setValue(mp.getCurrentTime().toMillis());
            isProgrammaticChange.setValue(false);
        });
        //Handle any seeking as dictated by the scroll bar
        videoTime.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!isProgrammaticChange.getValue())
                mp.seek(new javafx.util.Duration(videoTime.getValue()));
        });
        mediaBar.getChildren().add(videoTime);

        //Remaining Time
        Label playTime = new Label();
        mp.currentTimeProperty().addListener((observableValue, oldValue, newValue) -> {
            double currentTime = mp.getCurrentTime().toSeconds();
            double totalDuration = mp.getTotalDuration().toSeconds();
            playTime.setText(String.format("%02.0f:%02.0f/%02.0f:%02.0f",
                    Math.floor(currentTime/60),
                    Math.floor(currentTime%60),
                    Math.floor(totalDuration/60),
                    Math.floor(totalDuration%60)));
        });
        mediaBar.getChildren().add(playTime);

        //Volume Label
        final Label volume = new Label("  Volume: ");
        mediaBar.getChildren().add(volume);
        //Volume Slider
        final Slider volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.valueProperty().addListener((observable) -> {
            mp.setVolume(volumeSlider.getValue());
        });
        mediaBar.getChildren().add(volumeSlider);

        //Fullscreen Button
        final ToggleButton fullscreenButton = new ToggleButton("Fullscreen");
        final Rectangle2D initialBounds = new Rectangle2D(mv.getFitWidth(), mv.getFitWidth(), mv.getFitHeight(), mv.getFitWidth());
        fullscreenButton.setOnAction((event) -> {
            // TODO: Implement this properly
            if(fullscreenButton.isSelected()) {
                mv.setFitHeight(Screen.getPrimary().getBounds().getHeight());
                mv.setFitWidth(Screen.getPrimary().getBounds().getWidth());
            } else {
                mv.setFitHeight(initialBounds.getHeight());
                mv.setFitWidth(initialBounds.getWidth());
            }
        });
        mediaBar.getChildren().add(fullscreenButton);

        return mediaBar;
    }

    private class Holder<T>
    {
        private T value;

        private Holder(T value){this.value = value;}

        private T getValue(){return value;}

        private void setValue(T value){this.value = value;}
    }
}
