package com.i2lp.edi.client.presentationElements;

/**
 * Created by Koen on 06/04/2017.
 */

public abstract class InteractiveElement extends SlideElement {
    public static final String POLL = "poll";
    public static final String WORD_CLOUD = "wordCloud";

    protected int timeLimit = 30; //TODO get from server

    public abstract void sendDataToServer();
    public abstract void receiveDataFromServer();
    protected boolean elementActive = false;


    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
}
