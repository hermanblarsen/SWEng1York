package eloxExternalAudioRenderer;
import com.elox.Parser.Audio.Audio;

import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.media.*;
import javafx.util.Duration;

import java.io.File;

/**
 * Created by habl on 05/03/2017.
 * TODO Rename package: externalAudioRenderer before handover
 */
public class AudioRenderer {

    private static final float VOLUME_LOWER_RANGE = 0f;
    private static final float VOLUME_UPPER_RANGE = 1f;
    private static final Duration DURATION_LOWER_RANGE = Duration.ZERO;
    private static final Duration DURATION_UPPER_RANGE = Duration.INDEFINITE;
    private static final float PLAYBACK_LOWER_RANGE = Float.MIN_VALUE; //Specified as non-zero in Contract
    private static final float PLAYBACK_UPPER_RANGE = 10f;
    private static final Duration INTERVAL_LOWER_RANGE = Duration.ZERO;
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

    private Audio audioXmlData;

    public AudioRenderer (Audio audioXmlData) {
        this.audioXmlData = audioXmlData;
        this.duration = new Duration(audioXmlData.getDuration());
        this.startTime = new Duration(audioXmlData.getStartTime());
        this.endTime = new Duration(audioXmlData.getEndTime());
        this.playing = audioXmlData.isAutoplayOn();

        Media audioMedia;
        String path = audioXmlData.getPath();

        boolean pathFromURL= false; //TODO migh not be needed
        //Create audio media object
        if(path.contains("http://")){
            audioMedia = new Media(path);
            pathFromURL = true;
        }else {
            File file = new File(path);
            String mediaPath = file.toURI().toString();
            audioMedia = new Media(mediaPath);
        }

        //Create media player
        audioPlayer = new MediaPlayer(audioMedia);
        audioPlayer.setAutoPlay(audioXmlData.isAutoplayOn());
        updateAudioPlayer();

        if (playing) play();

        audioPlayer.onEndOfMediaProperty().addListener((ObservableValue<? extends Runnable> observable, Runnable oldValue, Runnable newValue) -> {
//            if (oldValue.toString() == )
            if (audioXmlData.isAutoplayOn()){
                replay();
            } else {
                stop();
                //TODO do we want it to be playable after initial thing or not? I assume so..
            }
        });
    }

    private void updateAudioPlayer(){
        this.audioPlayer.setRate(playbackSpeed);
        this.audioPlayer.setStartTime(startTime);
        this.audioPlayer.setStopTime(endTime);
        this.audioPlayer.setVolume(volume);
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
        setCurrentTime(startTime);
        play();
    }

    public void playFromTo(Duration startTime, Duration endTime) {
        setStartTime(startTime);
        setEndTime(endTime);
        setCurrentTime(startTime);
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
        if(playing) play();
        else pause();

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
        this.playing = playing;
        if(this.playing != playing){ //TODO contact Rhys and ask why they want a setter..
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
        currentTime = audioPlayer.getCurrentTime();
        return currentTime;
    }

    public void setCurrentTime(Duration currentTime) {
        this.currentTime = verifyDurationRange(currentTime, DURATION_LOWER_RANGE, DURATION_UPPER_RANGE);
        audioPlayer.seek(this.currentTime);
    }

    public Duration getStartTime() {
        return startTime;
    }

    public void setStartTime(Duration startTime) {
        this.startTime = verifyDurationRange(startTime, DURATION_LOWER_RANGE, DURATION_UPPER_RANGE);
        updateAudioPlayer();
    }

    public Duration getEndTime() {
        return endTime;
    }

    public void setEndTime(Duration endTime) {
        this.endTime = verifyDurationRange(endTime, DURATION_LOWER_RANGE, DURATION_UPPER_RANGE);
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
        this.mediaMarkerTimeInterval = verifyDurationRange(mediaMarkerTimeInterval, INTERVAL_LOWER_RANGE, INTERVAL_UPPER_RANGE);
    }

    public EventHandler<MediaMarkerEvent> getMediaMarkerEventEventHandler() {
        return mediaMarkerEventEventHandler;
    }

    public void setMediaMarkerEventEventHandler(EventHandler<MediaMarkerEvent> mediaMarkerEventEventHandler) {
        this.mediaMarkerEventEventHandler = mediaMarkerEventEventHandler;
    }

    private void updateCurrentTime(){
        currentTime = audioPlayer.getCurrentTime();
    }

    private Duration verifyDurationRange(Duration value, Duration lowerRange, Duration upperRange){
        if(value.greaterThan(upperRange)) return upperRange;
        else if(value.lessThan(lowerRange)) return lowerRange;
        else return value;
    }


    private Float verifyFloatRange(Float value, Float lowerRange, Float upperRange){
        if(value > upperRange) return upperRange;
        else if(value < lowerRange) return lowerRange;
        else return value;
    }

    public MediaPlayer getAudioPlayer() {
        return audioPlayer;
    }
}
