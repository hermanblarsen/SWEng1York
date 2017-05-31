package com.i2lp.edi.client.presentationViewerElements;

import com.i2lp.edi.client.presentationElements.Slide;
import com.i2lp.edi.client.presentationViewerElements.CommentPanel;
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

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by Luke on 29/04/2017.
 */
//@Ignore
public class CommentPanelTest extends ApplicationTest {

    private CommentPanel myCommentPanel;
    private Button saveButton;
    private Button submitButton;
    private String comment;
    private HTMLEditor htmlEditor;
    private Slide slide;

    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

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
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);

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

    @Ignore //TODO @Luke
    @Test
    public void submitTest() {
        clickOn(myCommentPanel).write("Test 123");
//        clickOn(submitButton);
        assertTrue(true);
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
