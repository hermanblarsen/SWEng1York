package com.i2lp.edi.client.presentationElements;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * Created by Luke on 06/05/2017.
 */
public class ResponseIndicatorTest extends ApplicationTest {
    private static ResponseIndicator myResponseIndicator;

    private ProgressIndicator progressIndicator;
    private Text numberText;

    @Override public void start(Stage stage) {
        myResponseIndicator = new ResponseIndicator();
        myResponseIndicator.setNumberOfStudents(0);
        myResponseIndicator.setNumberOfResponses(0);

        Scene scene = new Scene(myResponseIndicator);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Before
    public void setUp() {
        progressIndicator = myResponseIndicator.progressIndicator;
        numberText = myResponseIndicator.numberText;
    }

    @Test
    public void verifyProgressIndicator() {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                myResponseIndicator.setNumberOfStudents(0);
                myResponseIndicator.setNumberOfResponses(0);
            }
        });
        sleep(500);

        assertEquals(-1, progressIndicator.getProgress(), 0);
        assertEquals("", numberText.getText());

        Platform.runLater(new Runnable() {
            @Override public void run() {
                myResponseIndicator.setNumberOfStudents(10);
                myResponseIndicator.setNumberOfResponses(0);
            }
        });
        sleep(500);

        assertEquals(0, progressIndicator.getProgress(), 0);
        assertEquals("0", numberText.getText());

        Platform.runLater(new Runnable() {
            @Override public void run() {
                myResponseIndicator.setNumberOfStudents(10);
                myResponseIndicator.setNumberOfResponses(5);
            }
        });
        sleep(500);

        assertEquals(0.5, progressIndicator.getProgress(), 0);
        assertEquals("5", numberText.getText());

        Platform.runLater(new Runnable() {
            @Override public void run() {
                myResponseIndicator.setNumberOfStudents(10);
                myResponseIndicator.setNumberOfResponses(10);
            }
        });
        sleep(500);

        assertEquals(1, progressIndicator.getProgress(), 0);
        assertEquals("", numberText.getText());
    }

    @Test
    public void testIncrement() {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                myResponseIndicator.setNumberOfStudents(10);
                myResponseIndicator.setNumberOfResponses(0);
                myResponseIndicator.incrementResponses();
            }
        });
        sleep(500);

        assertEquals(0.1, progressIndicator.getProgress(), 0);
        assertEquals("1", numberText.getText());
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
