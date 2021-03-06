package com.i2lp.edi.client.presentationElements;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

/**
 * Created by Koen on 11/04/2017.
 */

/**
 * VideoElement, displaying video through JavaFX mediaplayer
 */
public class VideoElement extends SlideElement{
    protected boolean mediaControl = true;
    protected float xPosition = 0.5f;
    protected float yPosition = 0.5f;
    protected float xSize = 0.5f;
    protected float ySize = 0.5f;
    protected String path;
    protected boolean loop = false;
    protected boolean aspectRatioLock = true;
    protected float elementAspectRatio;
    protected boolean autoplay = false;
    protected boolean fullscreen = false;
    protected Duration startTime = Duration.ZERO;
    protected Duration endTime;
    protected MediaView mediaView;
    protected MediaPlayer mediaPlayer;
    protected Media media;
    private StackPane mediaPane;
    protected boolean started = false;
    protected boolean controlActive = false;
    protected ImageView playPauseButton;
    protected Stage videoFullscreenStage;
    protected boolean isAutoPlayOverridden = false;


    @Override
    public void doClassSpecificRender() {

        if (xSize == 0 || ySize == 0) {
            mediaView.fitWidthProperty().bind(slideCanvas.widthProperty());
            mediaView.fitHeightProperty().bind(slideCanvas.heightProperty());
        } else {
            mediaView.setFitWidth(slideWidth * xSize);
            mediaView.setFitHeight(slideHeight * ySize);
        }

        mediaPane.setTranslateX(slideWidth * xPosition);
        mediaPane.setTranslateY(slideHeight * yPosition);
        //System.out.println("CURRENT PATH: "+ media.getSource());
        if(autoplay && !started && !isForceMute && !isAutoPlayOverridden) {
            mediaPlayer.play();
            started = true;
        }
        getCoreNode().addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            if(!controlActive){
                performOnClickAction();
            }
        });

        if(isForceMute){
            mediaPlayer.setVolume(0);
        }
        getCoreNode().setPickOnBounds(false);

    }

    @Override
    public Node getCoreNode() {
        return mediaPane;
    }

    @Override
    public void setupElement() {
        mediaPane = new StackPane();

        if (path.contains("http://") || path.contains("https://") || path.contains("www.")) {
            media = new Media(path);
        } else {
            File file = new File(path);
            String mediaPath = file.toURI().toString();
            media = new Media(mediaPath);
        }

        mediaPlayer = new MediaPlayer(media);
        mediaView = new MediaView(mediaPlayer);

        if (startTime != null) {
            mediaPlayer.setStartTime(startTime);
        } else {
            mediaPlayer.setStartTime(javafx.util.Duration.ZERO);
        }
        if (endTime != null) {
            mediaPlayer.setStopTime(endTime);
        } else {
            mediaPlayer.setStopTime(media.getDuration());
        }
        if (loop) {
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        }

        mediaView.setPreserveRatio(aspectRatioLock);

        mediaPane.getChildren().add(mediaView);
        mediaPane.setStyle("-fx-background-color: black");

        if (mediaControl) {
            mediaPlayer.setOnReady(() -> mediaPane.setMaxSize(mediaView.getFitWidth(), mediaView.getFitHeight()));
            mediaPane.getChildren().add(mediaControl());
        }
        getCoreNode().setPickOnBounds(false);
        mediaPane.setVisible(visibility);
    }

    @Override
    public void destroyElement() {
        mediaView.getMediaPlayer().stop();
        Image play = new Image("file:projectResources/icons/PLAY_WHITE.png",20,20,true,true);
        playPauseButton.setImage(play);
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

    public void setAutoplay(boolean autoPlay) {
        this.autoplay = autoPlay;
    }

    public void setAutoPlayOverridden(boolean autoPlayOverridden) { isAutoPlayOverridden = autoPlayOverridden; }

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

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public MediaView getMediaView() {
        return mediaView;
    }

    private HBox mediaControl() {

        HBox mediaBar = new HBox();
        mediaBar.setStyle("-fx-background-color: transparent");
        mediaBar.setAlignment(Pos.BOTTOM_CENTER);
        mediaBar.setPadding(new Insets(5, 10, 5, 10));

        //Play/Pause Button
        Image play = new Image("file:projectResources/icons/PLAY_WHITE.png",20,20,true,true);
        Image pause = new Image("file:projectResources/icons/PAUSE_WHITE.png",20,20,true,true);
        playPauseButton = new ImageView(play);
        if (autoplay) {
            playPauseButton.setImage(pause);
        }
        playPauseButton.addEventHandler(MouseEvent.MOUSE_CLICKED, evt->{
            if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                playPauseButton.setImage(pause);
                mediaPlayer.play();
            } else {
                playPauseButton.setImage(play);
                mediaPlayer.pause();
            }
        });
        playPauseButton.addEventHandler(MouseEvent.MOUSE_ENTERED,evt->{
            controlActive = true;
        });
        playPauseButton.addEventHandler(MouseEvent.MOUSE_EXITED,evt->{
            controlActive = false;
        });

        //Stop Button
        Image stop = new Image("file:projectResources/icons/STOP_WHITE.png",20,20,true,true);
        ImageView stopButton = new ImageView(stop);
        stopButton.addEventHandler(MouseEvent.MOUSE_CLICKED,evt -> {
            mediaPlayer.stop();
            playPauseButton.setImage(play);
        });
        stopButton.addEventHandler(MouseEvent.MOUSE_ENTERED,evt->{
            controlActive = true;
        });
        stopButton.addEventHandler(MouseEvent.MOUSE_EXITED,evt->{
            controlActive = false;
        });

        // Seek Control
        final Slider videoTime = new Slider(startTime.toMillis(), 0, 0);
        mediaPlayer.statusProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == MediaPlayer.Status.READY) {
                videoTime.setMax(mediaPlayer.getCycleDuration().toMillis() + startTime.toMillis());
            }
        });
        videoTime.addEventHandler(MouseEvent.MOUSE_ENTERED,evt->{
            controlActive = true;
        });
        videoTime.addEventHandler(MouseEvent.MOUSE_EXITED,evt->{
            controlActive = false;
        });
        //Update the time bar to match the current playback time.
        final VideoElement.Holder<Boolean> isProgrammaticChange = new VideoElement.Holder<>(false);
        mediaPlayer.currentTimeProperty().addListener((observableValue) -> {
            isProgrammaticChange.setValue(true);
            videoTime.setValue(mediaPlayer.getCurrentTime().toMillis());
            isProgrammaticChange.setValue(false);
        });
        //Handle any seeking as dictated by the scroll bar
        videoTime.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!isProgrammaticChange.getValue())
                mediaPlayer.seek(new javafx.util.Duration(videoTime.getValue()));
        });

        //Remaining Time
        Label playTime = new Label();
        playTime.setTextFill(Color.web("#ffffff"));
        mediaPlayer.currentTimeProperty().addListener((observableValue, oldValue, newValue) -> {
            double currentTime = mediaPlayer.getCurrentTime().toSeconds();
            double totalDuration = mediaPlayer.getCycleDuration().toSeconds() + startTime.toSeconds();
            playTime.setText(String.format("%02.0f:%02.0f/%02.0f:%02.0f",
                    Math.floor(currentTime / 60),
                    Math.floor(currentTime % 60),
                    Math.floor(totalDuration / 60),
                    Math.floor(totalDuration % 60)));
        });
        playTime.addEventHandler(MouseEvent.MOUSE_ENTERED,evt->{
            controlActive = true;
        });
        playTime.addEventHandler(MouseEvent.MOUSE_EXITED,evt->{
            controlActive = false;
        });

        //Volume Label
        final Label volume = new Label("  Volume: ");
        volume.setTextFill(Color.web("#ffffff"));
        volume.addEventHandler(MouseEvent.MOUSE_ENTERED,evt->{
            controlActive = true;
        });
        volume.addEventHandler(MouseEvent.MOUSE_EXITED,evt->{
            controlActive = false;
        });
        //Volume Slider
        final Slider volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.addEventHandler(MouseEvent.MOUSE_ENTERED,evt->{
            controlActive = true;
        });
        volumeSlider.addEventHandler(MouseEvent.MOUSE_EXITED,evt->{
            controlActive = false;
        });
        volumeSlider.valueProperty().addListener((observable) -> {
            if (!isForceMute) {
                mediaPlayer.setVolume(volumeSlider.getValue());
            }
        });

        //Fullscreen Button
        Image fullscreenIcon = new Image("file:projectResources/icons/Fullscreen_NEW.png",20,20,true,true);
        ImageView fullscreenButton = new ImageView(fullscreenIcon);
        fullscreenButton.addEventHandler(MouseEvent.MOUSE_ENTERED,evt->{
            controlActive = true;
        });
        fullscreenButton.addEventHandler(MouseEvent.MOUSE_EXITED,evt->{
            controlActive = false;
        });
        fullscreenButton.addEventHandler(MouseEvent.MOUSE_CLICKED,(event) -> {
            // TODO: Implement this properly
            if (!fullscreen) {
                videoFullscreenStage = new Stage();
                StackPane mediaplayerPane = new StackPane();
                mediaplayerPane.getStylesheets().add("bootstrapfx.css");
                mediaplayerPane.setStyle("-fx-background-color: black");
                Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
                videoFullscreenStage.setFullScreen(true);
                Scene videoScene = new Scene(mediaplayerPane);
                videoFullscreenStage.setScene(videoScene);
                mediaplayerPane.getChildren().add(mediaView);
                if(!aspectRatioLock) {
                    mediaView.setFitHeight(screenSize.getHeight());
                }
                mediaView.setFitWidth(screenSize.getWidth());

                StackPane.setAlignment(mediaView,Pos.CENTER);
                mediaplayerPane.getChildren().add(mediaControl());
                videoFullscreenStage.show();
                videoFullscreenStage.addEventHandler(KeyEvent.KEY_PRESSED,evt->{
                    KeyCode key = evt.getCode();
                    if (key== KeyCode.ESCAPE){
                        videoFullscreenStage.close();
                        mediaPane.getChildren().add(mediaView);
                        mediaPane.getChildren().add(mediaControl());
                        mediaPane.setTranslateX(slideWidth*xPosition);
                        mediaPane.setTranslateY(slideHeight*yPosition);
                        mediaView.setFitHeight(slideHeight*ySize);
                        mediaView.setFitWidth(slideWidth*xSize);
                        fullscreen = false;
                    }
                });
                fullscreen = true;
            } else {
                videoFullscreenStage.close();
                mediaPane.getChildren().add(mediaView);
                mediaPane.getChildren().add(mediaControl());
                mediaPane.setTranslateX(slideWidth*xPosition);
                mediaPane.setTranslateY(slideHeight*yPosition);
                mediaView.setFitHeight(slideHeight*ySize);
                mediaView.setFitWidth(slideWidth*xSize);
                fullscreen = false;
            }
        });
        //mediaBar.getChildren().add(fullscreenButton);
        mediaBar.getChildren().addAll(playPauseButton,stopButton,videoTime,playTime,volume,volumeSlider,fullscreenButton);
        mediaBar.addEventHandler(MouseEvent.MOUSE_ENTERED, evt->{
            FadeTransition ft = new FadeTransition(javafx.util.Duration.millis(500),mediaBar);
            ft.setFromValue(mediaBar.getOpacity());
            ft.setToValue(1.0);
            ft.play();
        });

        mediaBar.addEventHandler(MouseEvent.MOUSE_EXITED, evt->{
            FadeTransition ft = new FadeTransition(javafx.util.Duration.millis(500),mediaBar);
            ft.setFromValue(mediaBar.getOpacity());
            ft.setToValue(0.0);
            ft.play();
        });

        mediaBar.setMinSize(0, 0); //TODO: Figure out what happens when mediaView is to small to contain mediaBar
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