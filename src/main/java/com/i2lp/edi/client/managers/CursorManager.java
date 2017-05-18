package com.i2lp.edi.client.managers;

/**
 * Created by Kacper on 2017-05-08.
 */
public class CursorManager {

    private CursorState currentState;
    private CursorState previousState;

    public CursorManager() {
        currentState = CursorState.DEFAULT;
        previousState = CursorState.DEFAULT;
    }

    public void setCursorState(CursorState state) {
        previousState = currentState;
        currentState = state;
    }

    public CursorState getCurrentState() { return currentState; }

    public CursorState getPreviousState() {
        CursorState tempState = currentState;
        currentState = previousState;
        previousState = tempState;
        return currentState;
    }

    public CursorState peekPreviousState() { return previousState; }
}

enum CursorState {
    DEFAULT,
    HIDDEN,
    DRAW,
    ERASE
}
