package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.presentationElements.Slide;
import com.i2lp.edi.client.presentationViewerElements.CommentPanel;
import com.i2lp.edi.client.presentationViewerElements.DrawPane;
import com.i2lp.edi.client.utilities.ParserXML;
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

    protected ImageView leftButton, rightButton, fullscreenButton, toolkitButton,
            questionButton, commentButton, drawButton, visibleButton;

    @Test
    public void testFullscreen() {
        javafx.geometry.Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        javafx.geometry.Rectangle2D initialPresBounds = new javafx.geometry.Rectangle2D(0.0,0.0,
                myPresentationManager.displayPane.getWidth(),myPresentationManager.displayPane.getHeight());

        clickOn(fullscreenButton);
        assertTrue(myPresentationManager.isFullscreen);

        javafx.geometry.Rectangle2D presBounds = new javafx.geometry.Rectangle2D(0.0,0.0,
                myPresentationManager.displayPane.getWidth(),myPresentationManager.displayPane.getHeight());
        assertEquals(screenBounds, presBounds);

        push(KeyCode.ESCAPE);
        assertFalse(myPresentationManager.isFullscreen);

        //TODO @Luke Out by one pixel
        /*
        presBounds = new javafx.geometry.Rectangle2D(0.0,0.0,
                myPresentationManager.displayPane.getWidth(),myPresentationManager.displayPane.getHeight());
        assertEquals(initialPresBounds, presBounds);
        */
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
