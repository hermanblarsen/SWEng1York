package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.presentationElements.Slide;
import com.i2lp.edi.client.presentationViewerElements.CommentPanel;
import com.i2lp.edi.client.presentationViewerElements.DrawPane;
import com.i2lp.edi.client.utilities.CursorState;
import com.i2lp.edi.client.utilities.ParserXML;
import javafx.geometry.VerticalDirection;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * Created by Luke on 29/05/2017.
 */
public abstract class PresentationViewerTest extends ApplicationTest {
    protected Presentation myPresentation;
    protected PresentationManager myPresentationManager;

    protected HBox presControls;
    protected VBox drawControls;
    protected CommentPanel commentPanel;
    protected DrawPane drawPane;
    protected Pane displayPane;

    protected ImageView leftButton, rightButton, fullscreenButton, linkButton,
            toolkitButton, questionButton, commentButton, drawButton, visibleButton;

    @Test
    public void testFullscreen() {
        javafx.geometry.Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        Double initialPresWidth = myPresentationManager.displayPane.getWidth();
        Double initialPresHeight = myPresentationManager.displayPane.getHeight();

        clickOn(fullscreenButton);
        assertTrue(myPresentationManager.isFullscreen);

        javafx.geometry.Rectangle2D presBounds = new javafx.geometry.Rectangle2D(0.0,0.0,
                myPresentationManager.displayPane.getWidth(),myPresentationManager.displayPane.getHeight());
        assertEquals(screenBounds, presBounds);

        push(KeyCode.ESCAPE);
        assertFalse(myPresentationManager.isFullscreen);

        //TODO @Luke Out by one pixel
        assertEquals(initialPresWidth, myPresentationManager.displayPane.getWidth(), 1);
        assertEquals(initialPresHeight, myPresentationManager.displayPane.getHeight(), 1);
    }

    @Test
    public void testSequenceAdvance() {
        assertEquals(1, myPresentationManager.currentSequenceNumber);
        clickOn(rightButton);
        assertEquals(2, myPresentationManager.currentSequenceNumber);
        clickOn(rightButton);
        assertEquals(3, myPresentationManager.currentSequenceNumber);
        clickOn(leftButton);
        assertEquals(2, myPresentationManager.currentSequenceNumber);
    }

    @Test
    public void testProgressBar() {
        Double progress;
        int sequenceNumberMax = 0;
        for (Slide slide : myPresentation.getSlideList()) {
            sequenceNumberMax += slide.getMaxSequenceNumber();
            sequenceNumberMax++;
        }

        //progress = myPresentationManager.currentSequenceNumber / (double) sequenceNumberMax;
        progress = 0.0;
        assertEquals(progress, myPresentationManager.progressBar.getProgress(), 0.01);

        clickOn(leftButton);
        assertEquals(progress, myPresentationManager.progressBar.getProgress(), 0.01);

        clickOn(rightButton);
        clickOn(rightButton);
        clickOn(rightButton);
        progress = myPresentationManager.currentSequenceNumber / (double) sequenceNumberMax;
        assertEquals(progress, myPresentationManager.progressBar.getProgress(), 0.01);
    }

    @Test
    public void testDrawControlCreation() {
        assertEquals(null, myPresentationManager.controlsPane.getLeft());
        clickOn(drawButton);
        assertEquals(drawControls, myPresentationManager.controlsPane.getLeft());
        clickOn(drawButton);
        assertEquals(null, myPresentationManager.controlsPane.getLeft());
    }

    @Test
    public void testDrawVisibility() {
        assertTrue(myPresentationManager.isDrawPaneVisible);
        assertTrue(displayPane.getChildren().contains(drawPane));
        clickOn(visibleButton);
        assertFalse(myPresentationManager.isDrawPaneVisible);
        assertFalse(displayPane.getChildren().contains(drawPane));
        clickOn(visibleButton);
        assertTrue(myPresentationManager.isDrawPaneVisible);
        assertTrue(displayPane.getChildren().contains(drawPane));
    }

    @Test
    public void testCommentPanelCreation() {
        assertEquals(1, myPresentationManager.sceneBox.getChildren().size());
        clickOn(commentButton);
        assertEquals(2, myPresentationManager.sceneBox.getChildren().size());
        assertEquals(commentPanel, myPresentationManager.sceneBox.getChildren().get(1));
        clickOn(commentButton);
        assertEquals(1, myPresentationManager.sceneBox.getChildren().size());
    }

    @Test
    public void testKeyboardListeners() {
        assertEquals(1, myPresentationManager.currentSequenceNumber);
        push(KeyCode.SPACE);
        assertEquals(2, myPresentationManager.currentSequenceNumber);
        push(KeyCode.BACK_SPACE);
        assertEquals(1, myPresentationManager.currentSequenceNumber);
        push(KeyCode.PAGE_UP);
        assertEquals(2, myPresentationManager.currentSequenceNumber);
        push(KeyCode.PAGE_DOWN);
        assertEquals(1, myPresentationManager.currentSequenceNumber);
        push(KeyCode.UP);
        assertEquals(2, myPresentationManager.currentSequenceNumber);
        push(KeyCode.DOWN);
        assertEquals(1, myPresentationManager.currentSequenceNumber);
        push(KeyCode.RIGHT);
        assertEquals(2, myPresentationManager.currentSequenceNumber);
        push(KeyCode.LEFT);
        assertEquals(1, myPresentationManager.currentSequenceNumber);

        assertFalse(myPresentationManager.isFullscreen);
        push(KeyCode.F5);
        assertTrue(myPresentationManager.isFullscreen);
        push(KeyCode.F5);
        push(KeyCode.ESCAPE);
        assertFalse(myPresentationManager.isFullscreen);

        assertFalse(myPresentationManager.isShowBlack);
        push(KeyCode.B);
        assertTrue(myPresentationManager.isShowBlack);
        push(KeyCode.B);
        assertFalse(myPresentationManager.isShowBlack);

        push(KeyCode.END);
        assertEquals(myPresentation.getMaxSlideNumber() - 1, myPresentationManager.currentSlideNumber);
        push(KeyCode.HOME);
        assertEquals(0, myPresentationManager.currentSlideNumber);
    }

    @Test //TODO @Luke Presentation elements interfere with test
    public void testMouseListeners() {
        moveTo(0, 0);
        assertFalse(myPresentationManager.isMouseOverSlide);
        moveTo(Screen.getPrimary().getBounds().getMaxX()/2, Screen.getPrimary().getBounds().getMaxY()/2);
        assertTrue(myPresentationManager.isMouseOverSlide);

        assertEquals(1, myPresentationManager.currentSequenceNumber);
        scroll(1, VerticalDirection.DOWN);
        assertEquals(2, myPresentationManager.currentSequenceNumber);
        scroll(1, VerticalDirection.UP);
        assertEquals(1, myPresentationManager.currentSequenceNumber);

        //clickOn(MouseButton.PRIMARY);
        //assertEquals(2, myPresentationManager.currentSequenceNumber);
        //clickOn(MouseButton.SECONDARY);
        //assertEquals(1, myPresentationManager.currentSequenceNumber);

        moveBy(20,0);
        sleep(1500);
        assertEquals(CursorState.DEFAULT, myPresentationManager.currentCursorState);
        sleep(2500);
        assertEquals(CursorState.HIDDEN, myPresentationManager.currentCursorState);

        moveBy(20,0);
        assertEquals(CursorState.DEFAULT, myPresentationManager.currentCursorState);

        //assertFalse(myPresentationManager.cMenu.isShowing());
        //clickOn(MouseButton.SECONDARY);
        //assertTrue(myPresentationManager.cMenu.isShowing());
    }

    @After
    public void tearDown() {
        try {
            FxToolkit.hideStage();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }
}
