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
import javafx.util.Duration;


/**
 * Created by habl on 26/02/2017.
 */
public class VideoElement extends SlideElement{
    protected boolean mediaControl;

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
    protected Duration startTime;
    protected Duration endTime;
    //protected Animation startAnimation, endAnimation;
    protected Pane slideCanvas;
    protected MediaView mv;
    protected MediaPlayer mp;
    protected Media media;
    protected BorderPane mediaPane;

//todo: 1) Add Error Handling
//todo: 2) Commenting
//TODO 3) move all testing out of this class and into a proper test class - Herman
    public VideoElement() {
        //Leaver this empty of testing and canvas things: testing should be done in separate testclass
    }

//    public void testVideoELement() {
//        mv = new MediaView();
//        mediaPane = new BorderPane();
//
//        mv.setVisible(true);
//
//        mp.setStartTime(new Duration(0));
//        mp.setStartTime(new Duration(2));
//        //TODO rearrange and whatever, but cannot be in setter and getter. Maybe in an update method.
//        getCoreNode().setTranslateX(xPosition);
//        getCoreNode().setTranslateX(yPosition);
//        mv.setPreserveRatio(aspectRatioLock);
//
//    }

    public void setUpVideoElement(BorderPane mediaPane) {
        mv = new MediaView();

        media = new Media(path);
        mp = new MediaPlayer(media);
        mv.setMediaPlayer(mp);

        mp.setStartTime(startTime);
        mp.setStopTime(endTime);
        mv.setPreserveRatio(aspectRatioLock);
        mv.setTranslateX(xPosition);
        mv.setTranslateY(yPosition);
        mv.setFitHeight(ySize);
        mv.setFitWidth(xSize);




        if(mediaControl == true) {
            mediaPane.setBottom(mediaControl());
        }
        if(loop == true){
            mp.setCycleCount(MediaPlayer.INDEFINITE);
        }
        mp.setAutoPlay(autoplay);
    }

    @Override
    void doClassSpecificRender() {

    }

    @Override
    public void renderElement(int animationType) {
        switch(animationType){
            case 0: //No animation (click)

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

    @Override
    void setupElement() {

    }

    public MediaPlayer getMediaPlayer() {return mp;}

    @Override
    public void setSlideCanvas(Pane slideCanvas) {
        this.slideCanvas = slideCanvas;
        BorderPane mediaPane = new BorderPane();
        setUpVideoElement(mediaPane);
        mediaPane.setCenter(mv);
        slideCanvas.getChildren().add(mediaPane);
    }


    public void setMediaPath(String mediaPath) {
        this.path = mediaPath;
    }
    public String getMediaPath() {return path;}

    public void setAutoPlay(boolean isAutoPlay){
        this.autoplay = isAutoPlay;
    }
    public boolean getAutoPlay() {return autoplay;}

    public void setVideoStartTime(Duration startTime) {
        this.startTime = startTime;

    }
    public Duration getVideoStartTime(){return startTime;}

    public void setVideoEndTime(Duration endTime) {
        this.endTime = endTime;

    }
    public Duration getVideoEndTime(){return endTime;}

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

    public void setxPosition(float xPosition){
        this.xPosition = xPosition;
    }
    public float getxPosition(){return xPosition;}

    public void setyPosition(float yPosition){
        this.yPosition = yPosition;
    }
    public float getyPosition(){return yPosition;}

    public void setxSize(float xSize){this.xSize = xSize;}
    public float getxSize() {return xSize;}

    public void setySize(float ySize){this.ySize = ySize;}
    public float getySize() {return ySize;}

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
    }//TODO: MAKE THIS DO SOMETHING

    public boolean isMediaControl() {
        return mediaControl;
    }

    public void setMediaControl(boolean mediaControl) {
        this.mediaControl = mediaControl;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isAutoplay() {
        return autoplay;
    }

    public void setAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
    }

    public MediaPlayer.Status getVideoStatus(){
        return mp.getStatus();
    }

    public void playVideo(){
        mp.play();
    }

    public void pauseVideo(){
        mp.pause();
    }

    public void stopVideo(){
        mp.stop();
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
                mp.seek(new Duration(videoTime.getValue()));
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
