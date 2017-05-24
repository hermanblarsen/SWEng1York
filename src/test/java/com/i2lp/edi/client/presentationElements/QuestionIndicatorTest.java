package com.i2lp.edi.client.presentationElements;

import javafx.scene.Scene;
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
 * Created by Luke on 07/05/2017.
 */
public class QuestionIndicatorTest extends ApplicationTest {
    private static QuestionIndicator myQuestionIndicator;

    private Text numberText;

    @Override
    public void start(Stage stage) {
        myQuestionIndicator = new QuestionIndicator();
        myQuestionIndicator.setNumberOfQuestions(0);

        Scene scene = new Scene(myQuestionIndicator);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Before
    public void setUp() {
        numberText = myQuestionIndicator.numberText;
    }

    @Test
    public void verifyNumberOfQuestions() {
        myQuestionIndicator.setNumberOfQuestions(-1);
        assertEquals("", numberText.getText());

        myQuestionIndicator.setNumberOfQuestions(5);
        assertEquals("5", numberText.getText());

        myQuestionIndicator.setNumberOfQuestions(15);
        assertEquals("15", numberText.getText());
    }

    @Test
    public void testIncrement() {
        myQuestionIndicator.incrementQuestions();
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
