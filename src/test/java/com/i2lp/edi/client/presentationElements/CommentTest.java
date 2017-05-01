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
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import org.junit.*;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * Created by Luke on 29/04/2017.
 */
@Ignore
public class CommentTest extends ApplicationTest {

    private CommentPanel myCommentPanel;
    private Button saveButton;
    private Button submitButton;
    private String comment;
    private HTMLEditor htmlEditor;

    @Override
    public void start(Stage stage) throws Exception {
        myCommentPanel = new CommentPanel(true);

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
    public void saveTest() {
        clickOn(myCommentPanel).write("Test 123");
        clickOn(saveButton);
        assertEquals( "Test 123", myCommentPanel.comment);
    }

    @Test
    public void submitTest() {
        clickOn(myCommentPanel).write("Test 123");
        clickOn(submitButton);

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
