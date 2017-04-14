package com.i2lp.edi.client.presentationElements;

/**
 * Created by Koen on 06/04/2017.
 */
public abstract class InteractiveElement extends SlideElement {
    public abstract void sendDataToServer();
    public abstract void receiveDataFromServer();
}
