package client.presentationElements;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.File;

/**
 * Created by Koen on 11/04/2017.
 */
public class VideoElement extends SlideElement{
    protected boolean mediaControl = true;
    protected float xPosition;
    protected float yPosition;
    protected float xSize = 0;
    protected float ySize = 0;
    protected String path;
    protected boolean loop = false;
    protected boolean aspectRatioLock = true;
    protected float elementAspectRatio;
    protected boolean autoplay = false;
    protected boolean fullscreen = false;
    protected Duration startTime;
    protected Duration endTime;
    protected MediaView mv;
    protected MediaPlayer mp;
    protected Media media;
    private StackPane mediaPane;


    @Override
    public void doClassSpecificRender() {
        if(autoplay){
            mp.play();
        }
    }

    @Override
    public Node getCoreNode() {
        return mediaPane;
    }

    @Override
    public void setupElement() {
        mediaPane = new StackPane();

        if (path.contains("http") || path.contains("www.")) {
            media = new Media(path);
        } else {
            File file = new File(path);
            String mediaPath = file.toURI().toString();
            media = new Media(mediaPath);
        }

        mp = new MediaPlayer(media);
        mv = new MediaView(mp);
        //mediaPane.getChildren().add(mv);


        if (startTime != null) {
            mp.setStartTime(startTime);
        } else {
            mp.setStartTime(javafx.util.Duration.ZERO);
        }
        if (endTime != null) {
            mp.setStopTime(endTime);
        } else {
            mp.setStopTime(media.getDuration());
        }
        if (loop) {
            mp.setCycleCount(MediaPlayer.INDEFINITE);
        }

        mv.setPreserveRatio(aspectRatioLock);
        mv.setTranslateX(xPosition);
        mv.setTranslateY(yPosition);
        if (xSize == 0 || ySize == 0) {
            mv.fitWidthProperty().bind(slideCanvas.widthProperty());
            mv.fitHeightProperty().bind(slideCanvas.heightProperty());
        } else {
            mv.setFitHeight(ySize);
            mv.setFitWidth(xSize);
        }
        mediaPane.getChildren().add(mv);
        mediaPane.setStyle("-fx-background-color: black");
        if (mediaControl) {
            mp.setOnReady(() -> mediaPane.setMaxSize(mv.getFitWidth(), mv.getFitHeight()));
            mediaPane.getChildren().add(mediaControl());
        }

        startAnimation = new Animation();
        startAnimation.setCoreNodeToAnimate(getCoreNode());
        startAnimation.setAnimationType(Animation.SIMPLE_APPEAR);
        endAnimation = new Animation();
        endAnimation.setCoreNodeToAnimate(getCoreNode());
        endAnimation.setAnimationType(Animation.SIMPLE_DISAPPEAR);
    }

    @Override
    public void destroyElement() {
        mv.getMediaPlayer().stop();
    }

    public boolean isMediaControl() {
        return mediaControl;
    }

    public void setMediaControl(boolean mediaControl) {
        this.mediaControl = mediaControl;
    }

    public float getxPosition() {
        return xPosition;
    }

    public void setxPosition(float xPosition) {
        this.xPosition = xPosition;
    }

    public float getyPosition() {
        return yPosition;
    }

    public void setyPosition(float yPostition) {
        this.yPosition = yPostition;
    }

    public float getxSize() {
        return xSize;
    }

    public void setxSize(float xSize) {
        this.xSize = xSize;
    }

    public float getySize() {
        return ySize;
    }

    public void setySize(float ySize) {
        this.ySize = ySize;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
    }

    public float getElementAspectRatio() {
        return elementAspectRatio;
    }

    public void setElementAspectRatio(float elementAspectRatio) {
        this.elementAspectRatio = elementAspectRatio;
    }

    public boolean isAutoplay() {
        return autoplay;
    }

    public void setAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public Duration getStartTime() {
        return startTime;
    }

    public void setStartTime(Duration startTime) {
        this.startTime = startTime;
    }

    public Duration getEndTime() {
        return endTime;
    }

    public void setEndTime(Duration endTime) {
        this.endTime = endTime;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public MediaPlayer getMp() {
        return mp;
    }

    public MediaView getMv() {
        return mv;
    }

    private HBox mediaControl() {

        HBox mediaBar = new HBox();
//        mediaBar.setMaxSize(media.getWidth(),media.getHeight());
        mediaBar.setStyle("-fx-background-color: transparent");
        mediaBar.setAlignment(Pos.BOTTOM_CENTER);
//        if(xSize != 0 && ySize !=0) {
//            mediaBar.setTranslateX(0);
//            mediaBar.setTranslateY(ySize);
//        }else{
//            mediaBar.setAlignment(Pos.BOTTOM_CENTER);
//        }
        mediaBar.setPadding(new Insets(5, 10, 5, 10));
        //BorderPane.setAlignment(mediaBar, Pos.CENTER);

        //Play/Pause Button
        Image play = new Image("file:projectResources/PLAY_WHITE.png",20,20,true,true);
        Image pause = new Image("file:projectResources/PAUSE_WHITE.png",20,20,true,true);
        ImageView playPauseButton = new ImageView(play);
        if (autoplay) {
            playPauseButton.setImage(pause);
        }
        playPauseButton.addEventHandler(MouseEvent.MOUSE_CLICKED, evt->{
            if (mp.getStatus() != MediaPlayer.Status.PLAYING) {
                playPauseButton.setImage(pause);
                mp.play();
            } else {
                playPauseButton.setImage(play);
                mp.pause();
            }
        });
        // mediaBar.getChildren().add(playPauseButton);

        //Stop Button
        Image stop = new Image("file:projectResources/STOP_WHITE.png",20,20,true,true);
        ImageView stopButton = new ImageView(stop);
        stopButton.addEventHandler(MouseEvent.MOUSE_CLICKED,evt -> {
            mp.stop();
            playPauseButton.setImage(play);
        });
        // mediaBar.getChildren().add(stopButton);

        // Seek Control
        final Slider videoTime = new Slider(startTime.toMillis(), 0, 0);
        mp.statusProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == MediaPlayer.Status.READY) {
                videoTime.setMax(mp.getCycleDuration().toMillis() + startTime.toMillis());
            }
        });
        //Update the time bar to match the current playback time.
        final VideoElement.Holder<Boolean> isProgrammaticChange = new VideoElement.Holder<>(false);
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
        //mediaBar.getChildren().add(videoTime);

        //Remaining Time
        Label playTime = new Label();
        playTime.setTextFill(Color.web("#ffffff"));
        mp.currentTimeProperty().addListener((observableValue, oldValue, newValue) -> {
            double currentTime = mp.getCurrentTime().toSeconds();
            double totalDuration = mp.getCycleDuration().toSeconds() + startTime.toSeconds();
            playTime.setText(String.format("%02.0f:%02.0f/%02.0f:%02.0f",
                    Math.floor(currentTime / 60),
                    Math.floor(currentTime % 60),
                    Math.floor(totalDuration / 60),
                    Math.floor(totalDuration % 60)));
        });
        //mediaBar.getChildren().add(playTime);

        //Volume Label
        final Label volume = new Label("  Volume: ");
        volume.setTextFill(Color.web("#ffffff"));
        //mediaBar.getChildren().add(volume);
        //Volume Slider
        final Slider volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.valueProperty().addListener((observable) -> mp.setVolume(volumeSlider.getValue()));
        //mediaBar.getChildren().add(volumeSlider);

        //Fullscreen Button
        //final ToggleButton fullscreenButton = new ToggleButton("Fullscreen");
        Image fullscreenIcon = new Image("file:projectResources/Fullscreen_NEW.png",20,20,true,true);
        ImageView fullscreenButton = new ImageView(fullscreenIcon);
        final Rectangle2D initialBounds = new Rectangle2D(mv.getFitWidth(), mv.getFitWidth(), mv.getFitHeight(), mv.getFitWidth());
        fullscreenButton.addEventHandler(MouseEvent.MOUSE_CLICKED,(event) -> {
            // TODO: Implement this properly
            if (!fullscreen) {
                mv.setFitHeight(slideCanvas.getHeight());
                mv.setFitWidth(slideCanvas.getWidth());
                fullscreen = true;
            } else {
                mv.setFitHeight(initialBounds.getHeight());
                mv.setFitWidth(initialBounds.getWidth());
                fullscreen = false;
            }
        });
        //mediaBar.getChildren().add(fullscreenButton);
        mediaBar.getChildren().addAll(playPauseButton,stopButton,videoTime,playTime,volume,volumeSlider,fullscreenButton);
        mediaBar.addEventHandler(MouseEvent.MOUSE_ENTERED, evt->{

            FadeTransition ft = new FadeTransition(javafx.util.Duration.millis(500),mediaBar);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        });

        mediaBar.addEventHandler(MouseEvent.MOUSE_EXITED, evt->{
            FadeTransition ft = new FadeTransition(javafx.util.Duration.millis(500),mediaBar);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.play();

        });
        return mediaBar;
    }

    private class Holder<T> {
        private T value;

        private Holder(T value) {
            this.value = value;
        }

        private T getValue() {
            return value;
        }

        private void setValue(T value) {
            this.value = value;
        }
    }

}
