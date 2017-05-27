package com.i2lp.edi.client.presentationElements;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaMarkerEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * An audio element based upon the JavaFX media player with additional control functions
 */
public class AudioElement extends SlideElement{

    // Fields

    /**
     * The audio start time.
     */
    protected Duration startTime;
    /**
     * The audio end time.
     */
    protected Duration endTime;
    /**
     * The media path. Must point to a valid '.mp3', '.wav' or '.m4a' file. Can be a local file or an URL.
     */
    protected String path;
    /**
     * If audio will loop playback automatically.
     */
    protected Boolean isLoop;
    /**
     * If audio is currently playing.
     */
    protected Boolean isPlaying;
    /**
     * If audio will auto-play.
     */
    protected Boolean isAutoPlay;
    /**
     * If audio is currently mute.
     */
    protected Boolean isMute;
    /**
     * The current volume, between 0 and 1.
     */
    protected float volume;
    /**
     * The current time aka playback progress.
     */
    protected Duration currentTime;
    /**
     * The current loop count, indicating how many times audio has repeated.
     */
    protected int currentCount;
    /**
     * The current playback rate, between 0 and 8
     */
    protected float playbackRate;
    /**
     * The media player element itself.
     */
    protected MediaPlayer mediaPlayer;
    /**
     * The current player status.
     */
    protected ReadOnlyObjectProperty<MediaPlayer.Status> status;
    /**
     * Event to perform upon end of media.
     */
    protected ObjectProperty<Runnable> onEndOfMedia;
    /**
     * Event to perform upon error.
     */
    protected ObjectProperty<Runnable> onError;
    /**
     * Event to perform upon player halted.
     */
    protected ObjectProperty<Runnable> onHalted;
    /**
     * Event to perform upon player paused.
     */
    protected ObjectProperty<Runnable> onPaused;
    /**
     * Event to perform upon playing.
     */
    protected ObjectProperty<Runnable> onPlaying;
    /**
     * Event to perform upon ready.
     */
    protected ObjectProperty<Runnable> onReady;
    /**
     * Event to perform upon repeat.
     */
    protected ObjectProperty<Runnable> onRepeat;
    /**
     * Event to perform upon stall.
     */
    protected ObjectProperty<Runnable> onStalled;
    /**
     * Event to perform upon stop.
     */
    protected ObjectProperty<Runnable> onStopped;
    /**
     * Event to perform upon reaching media-marker.
     */
    protected ObjectProperty<EventHandler<MediaMarkerEvent>> onMarker;
    /**
     * The collection of media-markers
     */
    protected ObservableMap<String, Duration> mediaMarkers;
    /**
     * The MediaView containing the player
     */
    protected MediaView mediaView;
    /**
     * The media to be played.
     */
    protected Media media;

    private Boolean prepared;

    /**
     * Instantiates a new Audio element.
     */
// Instantiate with default fields
    public AudioElement(){
        this.startTime = Duration.millis(0);
        this.isLoop = false;
        this.isPlaying = false;
        this.isAutoPlay = false;
        this.isMute = false;
        this.volume = 0.5f;
        this.currentTime = this.startTime;
        this.currentCount = 0;
        this.playbackRate = 1.0f;
        this.prepared = false;
    }

    /**
     * Starts audio playback.
     * Logs an error if media player does not exist or if the audio path has not yet been set.
     */
// Start the audio if it is able to be started
    public void startAudio(){

        if (!path.equals("")) {
            // If MediaPlayer is not null then play the audio
            if (mediaPlayer != null && prepared) {
                mediaPlayer.play();
                isPlaying = true;
            } else {
                logger.error("Error: Playback failed. Media player does not exist or isn't ready");
            }
        } else {
            logger.error("Error: Playback failed. Audio path has not been set.");
        }
    }

    /**
     * Pauses audio playback.
     * Logs an error if media player does not exist.
     */
    public void pauseAudio() {

        if (mediaPlayer != null && prepared) {
            if (isPlaying) {
                mediaPlayer.pause();
                isPlaying = false;
            }
        } else {
            logger.error("Error: Pause failed. Media player does not exist");
        }
    }

    /**
     * Stops  playback.
     * Logs an error if media player does not exist.
     */
// Same as dispose method?
    public void stopAudio() {
        if (mediaPlayer != null && prepared) {
            mediaPlayer.dispose();
            mediaPlayer = null;
            prepared = false;
        } else {
            logger.error("Error: Stop failed. Media player does not exist");
        }
    }

    /**
     * Toggle state of playback between play and pause.
     * Starts audio if audio is not playing, pauses audio if it's playing.
     * Logs an error if media player does not exist or if the audio path has not yet been set.
     */
    public void toggleAudio() {
        if (isPlaying) {
            pauseAudio();
        } else {
            startAudio();
        }
    }

    /**
     * Gets the duration of audio-cycle (in seconds)
     * Returns the duration of the "cropped" audioclip (eg. time between startTime and endTime) in seconds
     * Returns -1 and logs an error if there is an error
     * @return the cropped duration
     */
    public float getCycleDuration(){

        if (!prepared) {
            logger.error("Error: Duration cannot be determined as media not yet prepared");
            return -1f;
        }

        if (path.equals("")) {
            logger.error("Error: Duration cannot be determined as media path is not set.");
            return -1f;
        } else {
            return (float)(endTime.toSeconds() - startTime.toSeconds());
        }
    }

    /**
     * Adds a media marker at given time.
     *
     * @param key      Description of media-marker
     * @param duration Time at which it occurs
     */
    public void addMediaMarker(String key, Duration duration) {
        if (mediaMarkers != null) {
            mediaMarkers.put(key, duration);
        }
    }

    /**
     * Remove all media markers.
     */
    public void removeAllMediaMarkers() {
        if (mediaMarkers != null){
            mediaMarkers.clear();
        }
    }

    /**
     * Dispose of media-player resources.
     */
    public void dispose(){
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
    }

    @Override
    public Node getCoreNode() {
        return mediaView;
    }

    @Override
    public void doClassSpecificRender() {
        if(mediaPlayer == null) {//Run Once
            initMediaPlayer();

            // Get properties from the mediaplayer
            status = mediaPlayer.statusProperty();
            onEndOfMedia = mediaPlayer.onEndOfMediaProperty();
            onError = mediaPlayer.onErrorProperty();
            onHalted = mediaPlayer.onHaltedProperty();
            onPlaying = mediaPlayer.onPlayingProperty();
            onPaused = mediaPlayer.onPausedProperty();
            onReady = mediaPlayer.onReadyProperty();
            onRepeat = mediaPlayer.onRepeatProperty();
            onStalled = mediaPlayer.onStalledProperty();
            onStopped = mediaPlayer.onStoppedProperty();
            onMarker = mediaPlayer.onMarkerProperty();


            // Set properties for media player
            isLoop(isLoop);
            isAutoPlay(isAutoPlay);


            mediaPlayer.setOnReady(() -> {

                // Mark the player as prepared and ready to play
                prepared = true;

                // Set default end-time to the duration of the media
                if (endTime == null) {
                    endTime = media.getDuration();
                } else {
                    setEndTime(endTime);
                }
                mediaPlayer.seek(startTime);
                mediaPlayer.setStartTime(startTime);
                mediaPlayer.setStopTime(endTime);
                mediaPlayer.setVolume(volume);
                if (isAutoPlay) {
                    startAudio();
                }
            });

            // Add player to a MediaView
            mediaView.setMediaPlayer(mediaPlayer);

        } else {
            if (isAutoPlay) {
                startAudio();
            }

            if (isThumbnailGen) {
                mediaPlayer.stop();
            }
        }
    }

    @Override
    public void setupElement() {
        mediaView = new MediaView();
    }



    /**
     * Get current playback state.
     *
     * @return if player is currently playing
     */
// Getters and Setters
    public Boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Gets current loop count.
     * Return the amount of times playback has looped
     * @return the current loop count
     */
    public int getCurrentCount() {
        this.currentCount = mediaPlayer.getCurrentCount();
        return currentCount;
    }

    /**
     * Property describing current mediaplayer getStatus.
     *
     * @return the read only object property getStatus
     */
    public ReadOnlyObjectProperty<MediaPlayer.Status> getStatus() {
        return status;
    }

    /**
     * Allows action upon end of media to be set.
     *
     * @return the object property
     */
    public ObjectProperty<Runnable> getOnEndOfMedia() {
        return onEndOfMedia;
    }

    /**
     * Allows action upon error to be set.
     *
     * @return the object property
     */
    public ObjectProperty<Runnable> getOnError() {
        return onError;
    }

    /**
     * Allows action upon player halting to be set.
     *
     * @return the object property
     */
    public ObjectProperty<Runnable> getOnHalted() {
        return onHalted;
    }

    /**
     * Allows action upon pausing to be set.
     *
     * @return the object property
     */
    public ObjectProperty<Runnable> getOnPaused() {
        return onPaused;
    }

    /**
     * Allows action upon playing to be set.
     *
     * @return the object property
     */
    public ObjectProperty<Runnable> getOnPlaying() {
        return onPlaying;
    }

    /**
     * Allows action upon player-ready to be set.
     *
     * @return the object property
     */
    public ObjectProperty<Runnable> getOnReady() {
        return onReady;
    }

    /**
     * Allows action upon repeat to be set.
     *
     * @return the object property
     */
    public ObjectProperty<Runnable> getOnRepeat() {
        return onRepeat;
    }

    /**
     * Allows action upon player stall to be set.
     *
     * @return the object property
     */
    public ObjectProperty<Runnable> getOnStalled() {
        return onStalled;
    }

    /**
     * Allows action upon player stopping to be set.
     *
     * @return the object property
     */
    public ObjectProperty<Runnable> getOnStopped() {
        return onStopped;
    }

    /**
     * Allows action upon reaching a media-marker to be set
     *
     * @return the object property
     */
    public ObjectProperty<EventHandler<MediaMarkerEvent>> getOnMarker() {
        return onMarker;
    }

    /**
     * Gets the collection of media markers.
     *
     * @return the media markers
     */
    public ObservableMap<String, Duration> getMediaMarkers() {
        return mediaMarkers;
    }

    /**
     * Gets audio start time.
     *
     * @return the start time
     */
    public Duration getStartTime() {
        return startTime;
    }

    /**
     * Sets audio start time.
     * Cannot be greater than end-time (if set yet) or less than 0.
     * Errors are logged and start-time is normalised to within valid range
     *
     * @param startTime the start time
     */
    public void setStartTime(Duration startTime) {

        boolean lessThanEndTime;
        if (endTime == null) {
            lessThanEndTime = true;
        } else lessThanEndTime = startTime.lessThanOrEqualTo(endTime);

        if ((startTime.greaterThanOrEqualTo(Duration.millis(0))) && lessThanEndTime) {
            this.startTime = startTime;
        } else if (startTime.lessThanOrEqualTo(Duration.millis(0))) {
            this.startTime = Duration.millis(0);
            logger.error("Error: Cannot set start-time to less than 0 seconds. " +
                    "End time set to 0 instead.");
        } else if (startTime.greaterThanOrEqualTo(this.endTime)) {
            this.startTime = this.endTime;
            logger.error("Error: Cannot set start-time to more than end-time. " +
                    "Start time set equal to end-time instead.");
        }

        if (mediaPlayer != null) {
            mediaPlayer.setStartTime(startTime);
        }
    }

    /**
     * Gets audio end time.
     *
     * @return the end time
     */
    public Duration getEndTime() {
        return endTime;
    }

    /**
     * Sets audio end time.
     * Cannot be less than audio-start time or greater than audio duration.
     * Errors are logged and end-time is normalised to within valid range
     *
     * @param endTime the end time
     */
    public void setEndTime(Duration endTime) {

        if (!prepared) {
            this.endTime = endTime;
        } else if (endTime.greaterThanOrEqualTo(this.startTime) && endTime.lessThanOrEqualTo(media.getDuration())) {
            this.endTime = endTime;
        } else if (endTime.lessThanOrEqualTo(this.startTime)) {
            this.endTime = this.startTime;
            logger.error("Error: Cannot set end-time to less than start-time. " +
                    "End time set equal to start-time instead.");
        } else if (endTime.greaterThanOrEqualTo(media.getDuration())) {
            this.endTime = media.getDuration();
            logger.error("Error: Cannot set end-time to greater than audio duration. " +
                    "End time set to full audio duration instead.");
        }

        if (mediaPlayer != null) {
            mediaPlayer.setStopTime(this.endTime);
        }

    }

    /**
     * Gets media path.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets media path.
     *
     * @param path the path
     */
    /**
     * Sets media path to a valid audio format ('.mp3, '.wav' or '.m4a')
     * Displays an en error and FileNotFoundException if path is not valid.
     *
     * @param path the path
     */
    public void setPath(String path) {

        try {
            // Validate file type
            String lowercasePath = path.toLowerCase();
            if (lowercasePath.endsWith(".m4a") || lowercasePath.endsWith(".mp3") || lowercasePath.endsWith(".wav")) {
                this.path = path;

            }
            else {
                throw new FileNotFoundException("Error: Path '" + path + "' is not a valid path. " +
                        "Paths must point to files of type 'mp3', 'm4a', or 'wav'");
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
    }


    /**
     * Gets whether playback is set to isLoop.
     *
     * @return the isLoop
     */
    public Boolean getLoop() {
        return isLoop;
    }

    /**
     * Sets whether playback should isLoop
     *
     * @param isLoop if to isLoop
     */
    public void isLoop(Boolean isLoop) {

        if (mediaPlayer != null) {

            if (isLoop){
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            } else {
                mediaPlayer.setCycleCount(1); //Check this works if already looped some amount of times
            }
        }
        this.isLoop = isLoop;
    }

    /**
     * Gets current auto play getStatus.
     *
     * @return the auto play
     */
    public Boolean getAutoPlay() {

        if (mediaPlayer != null) {
            isAutoPlay = mediaPlayer.isAutoPlay();
        }
        return isAutoPlay;
    }

    /**
     * Sets auto play getStatus.
     *
     * @param isAutoPlay if to autoplay
     */
    public void isAutoPlay(Boolean isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
    }

    /**
     * Gets current mute getStatus.
     *
     * @return if mute
     */
    public Boolean isMute() {
        if (mediaPlayer != null) {
            this.isMute = mediaPlayer.isMute();
        }
        return isMute;
    }

    /**
     * Sets mute getStatus.
     *
     * @param mute the mute getStatus
     */
    public void setMute(Boolean mute) {
        this.isMute = mute;
        if (mediaPlayer != null) {
            mediaPlayer.setMute(isMute);
        }
    }

    /**
     * Gets current volume.
     *
     * @return the current volume
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Sets the volume.
     *
     * @param volume new volume
     */
    public void setVolume(float volume) {
        if ( (volume >= 0) && (volume <= 1)){
            this.volume = volume;

            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volume);
            }
        } else {
            logger.error("Error: Cannot set volume to '" + volume +"'. Valid range is between 0 and 1.");
        }
    }

    /**
     * Gets current playback position.
     *
     * @return the current time
     */
    public Duration getCurrentTime() {
        if (mediaPlayer != null) {
            this.currentTime = mediaPlayer.getCurrentTime();
        }
        return currentTime;
    }

    /**
     * Sets current playback position.
     * Cannot be set to greater than the end-time
     * @param currentTime the current time
     */
    public void setCurrentTime(Duration currentTime) {

        // Check if this is how they want it to work
        if (currentTime.lessThanOrEqualTo(startTime)) {
            this.currentTime = startTime;
            logger.error("Error: Cannot set current time to less than the start time. " +
                    "Current time has been set to the start time instead.");
        } else if (currentTime.greaterThanOrEqualTo(endTime)) {
            logger.error("Error: Cannot set current time to more than the end time. " +
                    "Current time has been set to the end time instead.");
            this.currentTime = this.endTime;
        } else if ((currentTime.greaterThanOrEqualTo(startTime)) && (currentTime.lessThanOrEqualTo(endTime))) {
            this.currentTime = currentTime;
        }

        if (mediaPlayer != null) {
            mediaPlayer.seek(this.currentTime);

        }

    }

    /**
     * Gets current playback rate.
     *
     * @return the playback rate
     */
    public float getPlaybackRate() {

        if (mediaPlayer != null) {
            this.playbackRate = (float)mediaPlayer.getRate();
        }
        return playbackRate;
    }

    /**
     * Sets playback rate.
     * Valid range is between 1 and 8. Inputs outside this range will be normalised to range and an error logged.
     * @param playbackRate the playback rate
     */
    public void setPlaybackRate(float playbackRate) {

        if ( (playbackRate >= 0) && (playbackRate <= 8)) {
            this.playbackRate = playbackRate;

        } else if(playbackRate < 0) {
            this.playbackRate = 0;
            logger.error("Error: Cannot set playback rate to " + playbackRate + ". Rate must be between 0 and 8");
        } else if (playbackRate > 8) {
            this.playbackRate = 8;
            logger.error("Error: Cannot set playback rate to " + playbackRate + ". Rate must be between 0 and 8");
        }

        if (mediaPlayer != null) {
            mediaPlayer.setRate(this.playbackRate);
        }
    }

    /**
     * Gets media player element.
     * Returns the base media player element
     * @return the media player
     */
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    /**
     * Sets media player.
     * Sets custom media player element
     * @param mediaPlayer the media player
     */
    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    /**
     * Adds markers at the specified intervals between the start and end time of the audio
     *
     * @param interval the interval at which to space markers
     */

    public void setMediaMarkerTimeInterval(Duration interval) {

        Task t = new Task() {
            @Override
            protected Object call() throws Exception {
                while (!prepared) {
                    // Wait for media player to be ready
                    Thread.sleep(1);
                }
                float cycle = getCycleDuration();
                // Cannot proceed if start and end-time are not yet set properly
                if (cycle == -1) {
                    throw new IllegalStateException("Cannot add markers. Start and end-time not yet set.");
                } else {
                    // Calculate the integer number of media markers required over the entire period
                    int numberOfMediaMarkers = (int) (cycle / interval.toSeconds());
                    // Add each of the media-markers, at intervals of interval
                    for (int i = 0; i < numberOfMediaMarkers; i++) {
                        Duration mediaMarkerTime = startTime.add(Duration.seconds(i * interval.toSeconds()));
                        String key = "Marker: " + (i+1) + " of " + numberOfMediaMarkers + " at time " + mediaMarkerTime.toMillis();
                        addMediaMarker(key, mediaMarkerTime);
                    }
                }
                return null;
            }
        };
        new Thread(t).start();
    }

    /**
     * Set the event handler that occurs when a media-marker is reached during playback
     *
     * @param handler the event handler to call when a marker is reached
     */

    public void setMediaMarkerEventHandler(EventHandler<MediaMarkerEvent> handler) {
        if (mediaPlayer != null) {
            mediaPlayer.setOnMarker(handler);
        }
    }

    @Override
    public void destroyElement(){
        mediaPlayer.stop();
    }

    private void initMediaPlayer(){
        // Check if path is an URL or local file
        if (path.contains("http://") || path.contains("https://") || path.contains("://www")) {
            media = new Media(path);
        } else {
            File file = new File(path);
            String mediaPath = file.toURI().toString();
            media = new Media(mediaPath);
        }

        mediaPlayer = new MediaPlayer(media);
        mediaMarkers = media.getMarkers();
    }
}
