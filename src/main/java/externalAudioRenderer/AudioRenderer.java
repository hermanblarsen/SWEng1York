package externalAudioRenderer;
import com.elox.Parser.Audio.Audio;

import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.scene.media.*;
import javafx.util.Duration;

import java.io.File;

/**
 * Created by Herman Larsen on 05/03/2017.
 */
public class AudioRenderer {

    private static final float VOLUME_LOWER_RANGE = 0.0f;
    private static final float VOLUME_UPPER_RANGE = 1.0f;
    private static final Duration DURATION_LOWER_RANGE = Duration.ZERO;
    private static final Duration DURATION_UPPER_RANGE = Duration.INDEFINITE;
    private static final float PLAYBACK_LOWER_RANGE = Float.MIN_VALUE; //Specified as non-zero in Contract
    private static final float PLAYBACK_UPPER_RANGE = 8.0f;
    private static final Duration INTERVAL_LOWER_RANGE = Duration.ZERO;
    private static final Duration INTERVAL_UPPER_RANGE = Duration.INDEFINITE;

    protected boolean playing = false;
    protected float volume = 0.5f;
    protected Duration currentTime = new Duration(0);
    protected Duration startTime = new Duration(0);
    protected Duration endTime = new Duration(0);
    protected float playbackSpeed = 1.0f;
    protected Duration mediaMarkerTimeInterval = new Duration(1000);
    protected EventHandler<MediaMarkerEvent> mediaMarkerEventEventHandler = null;
    protected MediaPlayer audioPlayer = null;

    protected Media audioMedia;
    private Audio audioXmlData;


    /**
     *
     * @param audioXmlData
     */
    public AudioRenderer (Audio audioXmlData) {
        this.audioXmlData = audioXmlData;
        startTime = new Duration(audioXmlData.getStartTime());
        endTime = new Duration(audioXmlData.getEndTime());
        playing = false;

        String path = audioXmlData.getPath();

        //Create audio media object from URL or URi
        if (path.contains("http://") || path.contains("https://") || path.contains("://wwww") ) {
            audioMedia = new Media(path);
        } else {
            File file = new File(path);
            String mediaPath = file.toURI().toString();
            audioMedia = new Media(mediaPath);
        }

        createMediaPlayer();
        setupEventListeners();

        if (audioXmlData.isAutoplayOn()) play();

        setMediaMarkerTimeInterval(this.mediaMarkerTimeInterval);
    }

    private void createMediaPlayer(){
        audioPlayer = new MediaPlayer(audioMedia);
        audioPlayer.setAutoPlay(audioXmlData.isAutoplayOn());
        if (audioXmlData.isLooped()) audioPlayer.setCycleCount(MediaPlayer.INDEFINITE);

        audioPlayer.setRate(playbackSpeed);
        audioPlayer.setStartTime(startTime);
        audioPlayer.setStopTime(endTime);
        audioPlayer.setVolume(volume);
    }

    private void setupEventListeners() {
        audioPlayer.setOnEndOfMedia(() -> {
            if (audioXmlData.isLooped()) replay();
            else stop();
        });
    }

    /**
     *
     */
    public void play() {
        playing = true;
        audioPlayer.play();
    }

    /**
     *
     * @param endTime
     */
    public void playTo(Duration endTime) {
        setEndTime(endTime);
        audioPlayer.play();
    }

    /**
     *
     * @param startTime
     */
    public void playFrom(Duration startTime) {
        setStartTime(startTime);
        setCurrentTime(startTime);
        play();
    }

    /**
     *
     * @param startTime
     * @param endTime
     */
    public void playFromTo(Duration startTime, Duration endTime) {
        setStartTime(startTime);
        setEndTime(endTime);
        setCurrentTime(startTime);
        play();
    }

    /**
     *
     */
    public void pause() {
        playing = false;
        audioPlayer.pause();
    }

    /**
     *
     */
    public void stop() {
        playing = false;
        audioPlayer.stop();
        setCurrentTime(startTime);
    }

    /**
     *
     * @return
     */
    public boolean togglePlaying() {
        playing = !playing;
        if(playing) play();
        else pause();

        return playing;
    }

    /**
     *
     * @param duration
     * @return
     */
    public Duration skip(Duration duration) {
        setCurrentTime(audioPlayer.getCurrentTime().add(duration));
        return currentTime;
    }

    /**
     *
     */
    public void goToStart() {
        setCurrentTime(startTime);
    }

    /**
     *
     */
   public void replay() {
        goToStart();
        play();
    }

    /**
     *
     * @return
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     *
     * @return
     */
    public float getVolume() {
        return volume;
    }

    /**
     *
     * @param volume
     */
    public void setVolume(float volume) {
        this.volume = verifyFloatRange(volume, VOLUME_LOWER_RANGE, VOLUME_UPPER_RANGE);
        audioPlayer.setVolume(this.volume);
    }

    /**
     *
     * @return
     */
    public Duration getCurrentTime() {
        currentTime = audioPlayer.getCurrentTime();
        return currentTime;
    }

    /**
     *
     * @param currentTime
     */
    public void setCurrentTime(Duration currentTime) {
        this.currentTime = verifyDurationRange(currentTime, DURATION_LOWER_RANGE, DURATION_UPPER_RANGE);
        audioPlayer.seek(this.currentTime);
    }

    /**
     *
     * @return
     */
    public Duration getStartTime() {
        return startTime;
    }

    /**
     *
     * @param startTime
     */
    public void setStartTime(Duration startTime) {
        this.startTime = verifyDurationRange(startTime, DURATION_LOWER_RANGE, DURATION_UPPER_RANGE);
        audioPlayer.setStartTime(this.startTime);
    }

    /**
     *
     * @return
     */
    public Duration getEndTime() {
        return endTime;
    }

    /**
     *
     * @param endTime
     */
    public void setEndTime(Duration endTime) {
        this.endTime = verifyDurationRange(endTime, DURATION_LOWER_RANGE, DURATION_UPPER_RANGE);
        audioPlayer.setStopTime(this.endTime);
    }

    /**
     *
     * @return
     */
    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    /**
     *
     * @param playbackSpeed
     */
    public void setPlaybackSpeed(float playbackSpeed) {
        this.playbackSpeed = verifyFloatRange(playbackSpeed, PLAYBACK_LOWER_RANGE, PLAYBACK_UPPER_RANGE);
        audioPlayer.setRate(this.playbackSpeed);
    }

    /**
     *
     * @return
     */
    public Duration getMediaMarkerTimeInterval() {
        return mediaMarkerTimeInterval;
    }

    /**
     *
     * @param mediaMarkerTimeInterval
     */
    public void setMediaMarkerTimeInterval(Duration mediaMarkerTimeInterval) {
        ObservableMap<String,Duration> mediaMarkers = this.audioMedia.getMarkers();
        mediaMarkers.clear(); //Clear any old markers

        this.mediaMarkerTimeInterval = verifyDurationRange(mediaMarkerTimeInterval, INTERVAL_LOWER_RANGE, INTERVAL_UPPER_RANGE);

        int numberOfMediaMarkers = (int)(audioPlayer.getStopTime().toMillis()/this.mediaMarkerTimeInterval.toMillis());

        for(int i = 0; i < numberOfMediaMarkers; i++){
            Duration mediaMarkerTime = this.mediaMarkerTimeInterval.multiply(i).add(startTime);

            mediaMarkers.put("Marker: " + Integer.toString(i) + " of " + numberOfMediaMarkers
                + " at time " + mediaMarkerTime.toMillis(), mediaMarkerTime);
        }
        audioPlayer.setOnMarker(mediaMarkerEventEventHandler);
    }

    /**
     *
     * @return mediaMarkerEventEventHandler
     */
    public EventHandler<MediaMarkerEvent> getMediaMarkerEventEventHandler() {
        mediaMarkerEventEventHandler = audioPlayer.getOnMarker();
        return mediaMarkerEventEventHandler;
    }

    /**
     *
     * @param mediaMarkerEventEventHandler
     */
    public void setMediaMarkerEventEventHandler(EventHandler<MediaMarkerEvent> mediaMarkerEventEventHandler) {
        this.mediaMarkerEventEventHandler = mediaMarkerEventEventHandler;
        audioPlayer.setOnMarker(mediaMarkerEventEventHandler);
    }

    private Duration verifyDurationRange(Duration value, Duration lowerRange, Duration upperRange){
        if(value.greaterThanOrEqualTo(upperRange)) return upperRange;
        else if(value.lessThanOrEqualTo(lowerRange)) return lowerRange;
        else return value;
    }

    private Float verifyFloatRange(Float value, Float lowerRange, Float upperRange){
        if(value >= upperRange) return upperRange;
        else if(value <= lowerRange) return lowerRange;
        else return value;
    }

    protected MediaPlayer getAudioPlayer() {
        return audioPlayer;
    }
}