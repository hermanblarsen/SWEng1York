package com.i2lp.edi.client.presentationElements;

/**
 * Created by Koen on 06/04/2017.
 */

public abstract class InteractiveElement extends SlideElement {
    public static final String POLL = "poll";
    public static final String WORD_CLOUD = "wordcloud";

    protected int timeLimit; //TODO get from server

    protected boolean elementActive = false;


    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
}
