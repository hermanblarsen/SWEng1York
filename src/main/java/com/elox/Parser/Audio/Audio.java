package com.elox.Parser.Audio;

/* Class containing information about every instance of audio */
public class Audio{
    private int id;
    private int startSequence;
    private int endSequence;
    private float duration;
    private String path;
    private boolean isLooped;
    private boolean isAutoplayOn;
    private int startTime;
    private int endTime;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public int getStartSequence() {
      return startSequence;
    }

    public void setStartSequence(int startSequence) {
      this.startSequence = startSequence;
    }

    public int getEndSequence() {
      return endSequence;
    }

    public void setEndSequence(int endSequence) {
      this.endSequence = endSequence;
    }

    public float getDuration() {
      return duration;
    }

    public void setDuration(float duration) {
      this.duration = duration;
    }

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public boolean isLooped() {
      return isLooped;
    }

    public void setLooped(boolean looped) {
      isLooped = looped;
    }

    public boolean isAutoplayOn() {
      return isAutoplayOn;
    }

    public void setAutoplayOn(boolean autoplayOn) {
      isAutoplayOn = autoplayOn;
    }

    public int getStartTime() {
      return startTime;
    }

    public void setStartTime(int startTime) {
      this.startTime = startTime;
    }

    public int getEndTime() {
      return endTime;
    }

    public void setEndTime(int endTime) {
      this.endTime = endTime;
    }
}
