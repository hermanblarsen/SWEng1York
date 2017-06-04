package com.i2lp.edi.client.editor;

import javafx.scene.control.MenuBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * Created by Luke on 01/06/2017.
 */
public class PresentationEditorTest extends ApplicationTest {
    private PresentationEditor myPresentationEditor;

    private VBox vbox;
    private MenuBar menuBar;
    private HBox statusBar;

    @Override
    public void start(Stage stage) throws Exception {
        myPresentationEditor = new PresentationEditor("");
    }

    @Before
    public void setUp() {
        vbox = myPresentationEditor.vbox;
        menuBar = myPresentationEditor.menuBar;
        statusBar = myPresentationEditor.statusBar;
    }

    @Test
    public void testAddPoll() {
        assertTrue(vbox.getChildren().size() == 0);

        moveTo(menuBar);
        moveBy(-menuBar.getWidth()/2 + 20, 0);
        clickOn();
        push(KeyCode.DOWN);
        push(KeyCode.RIGHT);
        push(KeyCode.ENTER);

        assertTrue(vbox.getChildren().size() == 1);
        assertTrue(vbox.getChildren().get(0) instanceof PollEditorPanel);

        moveTo(menuBar);
        moveBy(-menuBar.getWidth()/2 + 20, 0);
        clickOn();
        push(KeyCode.DOWN);
        push(KeyCode.RIGHT);
        push(KeyCode.ENTER);

        assertTrue(vbox.getChildren().size() == 2);
        assertTrue(vbox.getChildren().get(1) instanceof PollEditorPanel);
    }

    @Test
    public void testStatusBar() {
        moveTo(menuBar);
        moveBy(-menuBar.getWidth()/2 + 20, 0);
        clickOn();
        push(KeyCode.DOWN);
        push(KeyCode.RIGHT);
        push(KeyCode.ENTER);

        PollEditorPanel pePanel = (PollEditorPanel) (vbox.getChildren().get(0));

        //This is needed as the status only updates when the mouse enters the main poll panel
        moveTo(vbox.getChildren().get(0));

        assertEquals("Number of Answers: 0", myPresentationEditor.getStatusText().getText());

        clickOn(pePanel.responseTypeChoiceBox).clickOn(pePanel.responseTypeChoiceBox.getItems().get(1));

        moveTo(vbox.getChildren().get(0));
        assertEquals("Number of Answers: 2", myPresentationEditor.getStatusText().getText());
    }

    @Ignore //TODO @Luke Not yet implemented
    @Test
    public void testSaveToXml() {

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
