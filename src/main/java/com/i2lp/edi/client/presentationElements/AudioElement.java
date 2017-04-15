package com.i2lp.edi.client.presentationElements;

import javafx.scene.Node;

/**
 * Created by habl on 25/02/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AudioElement extends SlideElement {
    //Dummy class for parsing
    protected String path;
    protected boolean loop;
    protected boolean autoplay;
    protected int startTime;
    protected int endTime;
    private float durationSequence;

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

    public boolean isAutoplay() {
        return autoplay;
    }

    public void setAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
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


    @Override
    public void doClassSpecificRender() {

    }

    @Override
    public Node getCoreNode() {
        return null;
    }

    @Override
    public void setupElement() {

    }

    @Override
    public void destroyElement() {

    }

    public void setDurationSequence(float durationSequence){
        this.durationSequence = durationSequence;
    }
}