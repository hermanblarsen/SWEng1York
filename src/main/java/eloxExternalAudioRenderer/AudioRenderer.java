package eloxExternalAudioRenderer;
import com.elox.Parser.Audio.Audio;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.*;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Created by habl on 05/03/2017.
 * TODO Rename package: externalAudioRenderer before handover
 */
public class AudioRenderer extends Application{ //TODO TEST remove before handover

    private static final float VOLUME_LOWER_RANGE = 0f;
    private static final float VOLUME_UPPER_RANGE = 0.5f;
    private static final Duration DURATION_LOWER_RANGE = Duration.ZERO;
    private static final Duration DURATION_UPPER_RANGE = Duration.INDEFINITE;
    private static final float PLAYBACK_LOWER_RANGE = 0f;
    private static final float PLAYBACK_UPPER_RANGE = 10f;
    private static final Duration INTERVAL_LOWER_RANGE = new Duration(0);
    private static final Duration INTERVAL_UPPER_RANGE = Duration.INDEFINITE;

    protected boolean playing = false;
    protected float volume = 0.5f;
    protected Duration duration = new Duration(0);
    protected Duration currentTime = new Duration(0);
    protected Duration startTime = new Duration(0);
    protected Duration endTime = new Duration(0);
    protected float playbackSpeed = 1.0f;
    protected Duration mediaMarkerTimeInterval = new Duration(1000);
    protected EventHandler<MediaMarkerEvent> mediaMarkerEventEventHandler = null;
    protected MediaPlayer audioPlayer = null;

    public static void main(String[] args){launch(args);}

    public AudioRenderer (Audio audioXmlData) {
        duration = new Duration(audioXmlData.getDuration());
        startTime = new Duration(audioXmlData.getStartTime());
        endTime = new Duration(audioXmlData.getEndTime());

        //Create audio media object
        Media audioMedia = new Media(audioXmlData.getPath());

        //Create media player
        audioPlayer = new MediaPlayer(audioMedia);
        audioPlayer.setAutoPlay(audioXmlData.isAutoplayOn());
        playing = audioXmlData.isAutoplayOn();
        updateAudioPlayer();


    }


    Scene scene;
    BorderPane border;
    Stage primaryStage;
    Audio xmlAudioElement;
    @Override
    public void start(Stage primaryStage){
        primaryStage.setTitle("Test");

        border = new BorderPane();
        scene = new Scene(border, 600, 600);
//        scene.getStylesheets().add("bootstrapfx.css");
        primaryStage.setScene(scene);
        border.setCenter(new StackPane());

        primaryStage.show();
        play();
    }

    public void setupEloxTestAudio () {
        xmlAudioElement.setId(1);
        xmlAudioElement.setStartSequence(1);
        xmlAudioElement.setEndSequence(2);
        xmlAudioElement.setDuration(1);
        xmlAudioElement.setPath("externalResources/NorwegianPimsleur.mp3");
        xmlAudioElement.setLooped(Boolean.TRUE);
        xmlAudioElement.setAutoplayOn(Boolean.TRUE);
        xmlAudioElement.setStartTime(0);
        xmlAudioElement.setEndTime(10);
    }

    public void play() {
        playing = true;
        audioPlayer.play();
    }

    public void playTo(Duration endTime) {
        setEndTime(endTime);
        audioPlayer.play();
    }

    public void playFrom(Duration startTime) {
        setStartTime(startTime);
        play();
    }

    public void playFromTo(Duration startTime, Duration endTime) {
        setStartTime(startTime);
        setEndTime(endTime);
        play();
    }

    public void pause() {
        playing = false;
        audioPlayer.pause();
    }

    public void stop() {
        pause();
        goToStart();
    }

    public boolean togglePlaying() {
        playing = !playing;
        if(playing){
            play();
        }
        else{
            pause();
        }
        return playing;
    }

    public Duration skip(Duration duration) {
        setCurrentTime(audioPlayer.getCurrentTime().add(duration));
        return currentTime;
    }

    public void goToStart() {
        setCurrentTime(startTime);
    }

   public void replay() {
        goToStart();
        play();
    }

    public void updateMediaMarkers(Duration newMediaMarkerTimeInterval) {

    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        if(this.playing != playing){
            togglePlaying();
        }
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = verifyFloatRange(volume, VOLUME_LOWER_RANGE, VOLUME_UPPER_RANGE);
        updateAudioPlayer();
    }

    public Duration getCurrentTime() {
        updateCurrentTime();
        return currentTime;
    }

    public void setCurrentTime(Duration currentTime) {
        this.currentTime = checkDurationRange(currentTime, DURATION_LOWER_RANGE, DURATION_UPPER_RANGE);
        updateAudioPlayer();
    }

    public Duration getStartTime() {
        return startTime;
    }

    public void setStartTime(Duration startTime) {
        this.startTime = checkDurationRange(startTime, DURATION_LOWER_RANGE, DURATION_UPPER_RANGE);
        updateAudioPlayer();
    }

    public Duration getEndTime() {
        return endTime;
    }

    public void setEndTime(Duration endTime) {
        this.endTime = checkDurationRange(endTime, DURATION_LOWER_RANGE, DURATION_UPPER_RANGE);
        updateAudioPlayer();
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public void setPlaybackSpeed(float playbackSpeed) {
        this.playbackSpeed = verifyFloatRange(playbackSpeed, PLAYBACK_LOWER_RANGE, PLAYBACK_UPPER_RANGE);
        updateAudioPlayer();
    }

    public Duration getMediaMarkerTimeInterval() {
        return mediaMarkerTimeInterval;
    }

    public void setMediaMarkerTimeInterval(Duration mediaMarkerTimeInterval) {
        this.mediaMarkerTimeInterval = checkDurationRange(mediaMarkerTimeInterval, INTERVAL_LOWER_RANGE, INTERVAL_UPPER_RANGE);
    }

    public EventHandler<MediaMarkerEvent> getMediaMarkerEventEventHandler() {
        return mediaMarkerEventEventHandler;
    }

    public void setMediaMarkerEventEventHandler(EventHandler<MediaMarkerEvent> mediaMarkerEventEventHandler) {
        this.mediaMarkerEventEventHandler = mediaMarkerEventEventHandler;
    }

    private void updateAudioPlayer(){
        audioPlayer.setRate((float) playbackSpeed);
        audioPlayer.setStartTime(startTime);
        audioPlayer.setStopTime(endTime);
        audioPlayer.setVolume(volume);
        audioPlayer.seek(currentTime);
    }

    private void updateCurrentTime(){
        currentTime = audioPlayer.getCurrentTime();
    }

    private static Duration checkDurationRange(Duration value, Duration lowerRange, Duration upperRange){
        if(value.greaterThan(upperRange)){
            return upperRange;
        }
        else if(value.lessThan(lowerRange)){
            return lowerRange;
        }
        else{
            return value;
        }
    }

    private static Float verifyFloatRange(Float value, Float lowerRange, Float upperRange){
        if(value > upperRange){
            return upperRange;
        }
        else if(value < lowerRange){
            return lowerRange;
        }
        else{
            return value;
        }
    }
}
