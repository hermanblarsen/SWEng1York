package com.i2lp.edi.client.presentationElements;

/**
 * Created by Koen on 06/04/2017.
 */

/**
 * Abstract class extending SlideElement, which will be used to
 * develop thge interactive elements.
 */
public abstract class InteractiveElement extends SlideElement {
    public static final String POLL = "poll";
    public static final String WORD_CLOUD = "wordcloud";

    protected int timeLimit;

    public boolean isElementActive() {
        return elementActive;
    }

    protected boolean elementActive = false;


    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
}
