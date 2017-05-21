package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.dashboard.TeacherDashboard;
import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.presentationElements.CommentPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import org.junit.*;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * Created by Luke on 29/04/2017.
 */
@Ignore
public class CommentPanelTest extends ApplicationTest {

    private CommentPanel myCommentPanel;
    private Button saveButton;
    private Button submitButton;
    private String comment;
    private HTMLEditor htmlEditor;
    private Slide slide;

    @Override
    public void start(Stage stage) throws Exception {
        slide = new Slide();
        myCommentPanel = new CommentPanel(false);
        myCommentPanel.setSlide(slide);
        Scene scene = new Scene(myCommentPanel);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Before
    public void setUp() {
        saveButton = myCommentPanel.saveButton;
        submitButton = myCommentPanel.submitButton;
        comment = myCommentPanel.comment;
        htmlEditor = myCommentPanel.htmlEditor;
    }

    @Test
    public void commentTest() {
        clickOn(myCommentPanel).write("Test 123");
//        clickOn(saveButton);
        assertTrue(htmlEditor.getHtmlText().contains("Test 123"));
        assertTrue(slide.getUserComments().contains("Test 123"));
    }

    @Test
    public void reCommentTest() {
        clickOn(myCommentPanel).write("Test 123");
//        clickOn(saveButton);
        assertTrue(slide.getUserComments().contains("Test 123"));
        eraseText("Test 123".length()).write("New Test 456");
        assertTrue(slide.getUserComments().contains("New Test 456"));
    }

    @Test
    public void submitTest() {
        clickOn(myCommentPanel).write("Test 123");
//        clickOn(submitButton);
        assertTrue(true); //TODO
        //Comment submission not yet implemented
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
