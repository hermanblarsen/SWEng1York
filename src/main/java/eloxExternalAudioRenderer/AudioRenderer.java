package eloxExternalAudioRenderer;
import com.elox.Parser.Audio.Audio;

import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.scene.media.*;
import javafx.util.Duration;

import java.io.File;

/**
 * Created by habl on 05/03/2017.
 * TODO Rename package: externalAudioRenderer before handover
 */
public class AudioRenderer {

    private static final float VOLUME_LOWER_RANGE = 0.0f;
    private static final float VOLUME_UPPER_RANGE = 1.0f;
    private static final Duration DURATION_LOWER_RANGE = Duration.ZERO;
    private static final Duration DURATION_UPPER_RANGE = Duration.INDEFINITE;
    private static final float PLAYBACK_LOWER_RANGE = Float.MIN_VALUE; //Specified as non-zero in Contract
    private static final float PLAYBACK_UPPER_RANGE = 10.0f;
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

    private Media audioMedia;
    private Audio audioXmlData;
    private ObservableMap<String,Duration> mediaMarkers;

    /**
     *
     * @param audioXmlData
     */
    public AudioRenderer (Audio audioXmlData) {
        this.audioXmlData = audioXmlData;
        this.duration = new Duration(audioXmlData.getDuration());
        this.startTime = new Duration(audioXmlData.getStartTime());
        this.endTime = new Duration(audioXmlData.getEndTime());
        this.playing = false;

        String path = audioXmlData.getPath();

        boolean pathFromURL = false; //TODO might not be needed
        //Create audio media object
        if (path.contains("http://")) {
            audioMedia = new Media(path);
            pathFromURL = true;
        } else {
            File file = new File(path);
            String mediaPath = file.toURI().toString();
            audioMedia = new Media(mediaPath);
        }

        createMediaPlayer();
        setupEventListeners();
        updateMediaMarkers(mediaMarkerTimeInterval);

        if (audioXmlData.isAutoplayOn()) play();
    }

    private void createMediaPlayer(){
        audioPlayer = new MediaPlayer(audioMedia);
        audioPlayer.setAutoPlay(audioXmlData.isAutoplayOn());
        if (audioXmlData.isLooped()) audioPlayer.setCycleCount(MediaPlayer.INDEFINITE);

        this.audioPlayer.setRate(playbackSpeed);
        this.audioPlayer.setStartTime(startTime);
        this.audioPlayer.setStopTime(endTime);
        this.audioPlayer.setVolume(volume);
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
        audioPlayer.stop();
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

    //TODO contact Rhys and ask why they want a setter..

    /**
     *
     * @param playing
     */
    public void setPlaying(boolean playing) {
        this.playing = playing;
//        if(this.playing != playing){
//            togglePlaying();
//        }
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
        this.audioPlayer.setVolume(this.volume);
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
        this.audioPlayer.seek(this.currentTime);
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
        this.audioPlayer.setStartTime(this.startTime);
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
        this.audioPlayer.setStopTime(this.endTime);
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
        this.audioPlayer.setRate(this.playbackSpeed);
    }

    /**
     *
     * @return
     */
    public Duration getMediaMarkerTimeInterval() {
        return this.mediaMarkerTimeInterval;
    }

    /**
     *
     * @param mediaMarkerTimeInterval
     */
    public void setMediaMarkerTimeInterval(Duration mediaMarkerTimeInterval) {
        this.mediaMarkerTimeInterval = verifyDurationRange(mediaMarkerTimeInterval, INTERVAL_LOWER_RANGE, INTERVAL_UPPER_RANGE);
        //TODO update something or just set the interval?? Or just remove the "Update and have everything within the setter?"
    }

    /**
     *
     * @return
     */
    public EventHandler<MediaMarkerEvent> getMediaMarkerEventEventHandler() {
        this.mediaMarkerEventEventHandler = audioPlayer.getOnMarker();
        return this.mediaMarkerEventEventHandler;
    }

    /**
     *
     * @param mediaMarkerEventEventHandler
     */
    public void setMediaMarkerEventEventHandler(EventHandler<MediaMarkerEvent> mediaMarkerEventEventHandler) {
        this.mediaMarkerEventEventHandler = mediaMarkerEventEventHandler;
        this.audioPlayer.setOnMarker(mediaMarkerEventEventHandler);
    }

    /**
     *
     * @param newMediaMarkerTimeInterval
     */
    public void updateMediaMarkers(Duration newMediaMarkerTimeInterval) {
        int numberOfMediaMarkers = (int)(this.audioPlayer.getCycleDuration().toMillis()/newMediaMarkerTimeInterval.toMillis());

        this.mediaMarkers = this.audioMedia.getMarkers();
        this.mediaMarkers.clear(); //Clear if any old markers are present
        for(int i = 0; i < numberOfMediaMarkers; i++){
            this.mediaMarkers.put("Marker: " + Integer.toString(i) + " of " + numberOfMediaMarkers, newMediaMarkerTimeInterval.multiply(i));
        }
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