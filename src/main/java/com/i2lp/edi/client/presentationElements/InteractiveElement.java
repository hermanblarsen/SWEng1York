package com.i2lp.edi.client.presentationElements;

/**
 * Created by Koen on 06/04/2017.
 */

public abstract class InteractiveElement extends SlideElement {
    public static final String POLL = "poll";
    public static final String WORD_CLOUD = "wordCloud";


    public abstract void sendDataToServer();
    public abstract void receiveDataFromServer();
    protected boolean elementActive = false;
}
