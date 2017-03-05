package eloxExternalAudioRenderer;
import com.elox.Parser.Audio.Audio;

import javafx.event.EventHandler;
import javafx.scene.media.MediaMarkerEvent;
import javafx.util.Duration;

/**
 * Created by habl on 05/03/2017.
 */
public class AudioRenderer {
    protected boolean playing;
    protected float volume;
    protected Duration currentTime;
    protected Duration startTime;
    protected Duration endTime;
    protected float playbackSpeed ;
    protected Duration mediaMarkerTimeInterval;
    protected EventHandler<MediaMarkerEvent> mediaMarkerEventEventHandler;

    public AudioRenderer (Audio audioXmlData) {

    }

    public void play() {

    }

    public void playTo(Duration endTime) {

    }

    public void playFrom(Duration startTime) {

    }

    public void playFromTo(Duration startTime, Duration endTime) {

    }

    public void pause() {

    }

    public void stop() {

    }

    public boolean togglePlaying() {
        return togglePlaying();
    }

    public Duration skip(Duration duration) {
        return null;
    }

    public void goToStart() {

    }

   public void replay() {

    }

    public void updateMediaMarkers(Duration newMediaMarkerTimeInterval) {

    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public Duration getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Duration currentTime) {
        this.currentTime = currentTime;
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

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public void setPlaybackSpeed(float playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
    }

    public Duration getMediaMarkerTimeInterval() {
        return mediaMarkerTimeInterval;
    }

    public void setMediaMarkerTimeInterval(Duration mediaMarkerTimeInterval) {
        this.mediaMarkerTimeInterval = mediaMarkerTimeInterval;
    }

    public EventHandler<MediaMarkerEvent> getMediaMarkerEventEventHandler() {
        return mediaMarkerEventEventHandler;
    }

    public void setMediaMarkerEventEventHandler(EventHandler<MediaMarkerEvent> mediaMarkerEventEventHandler) {
        this.mediaMarkerEventEventHandler = mediaMarkerEventEventHandler;
    }
}
