package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.animation.Animation;
import com.i2lp.edi.client.utilities.GraphicalTest;
import eu.hansolo.tilesfx.Tile;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.junit.*;
import org.testfx.api.FxToolkit;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by Luke on 26/05/2017.
 */

@Ignore //TODO @Luke Fails due to null start timer
public class PollElementTest extends GraphicalTest {
    private PollElement myPollElement;
    private HBox controlPanel;
    private ToggleButton[] answerButton;
    private Button startTimer;
    private Label remainingTime;

    private String question;
    private String answers;
    private boolean timerStart;
    private int timeLimit;
    private Tile answerOutputTile;

    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

        BorderPane pane = new BorderPane();
        Scene scene = new Scene(pane, 600, 600);

        myPollElement = new PollElement();
        myPollElement.setTeacher(true);
        myPollElement.setAnswers("ANS1,ANS2");
        myPollElement.setQuestion("Test question?");
        myPollElement.setTimeLimit(3);

        myPollElement.setSlideWidth(scene.getWidth());
        myPollElement.setSlideHeight(scene.getHeight());
        myPollElement.setSlideCanvas(pane);
        myPollElement.renderElement(Animation.NO_ANIMATION);

        answerButton = myPollElement.answerButton;
        startTimer = myPollElement.startTimer;
        controlPanel = new HBox();
        controlPanel.getChildren().add(startTimer);
        pane.setBottom(controlPanel);
        pane.setTop(remainingTime);

        stage.setTitle("Poll Element Test");
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);

        question = myPollElement.question;
        answers = myPollElement.answers;
        timerStart = myPollElement.timerStart;
        timeLimit = myPollElement.timeLimit;
        remainingTime = myPollElement.remainingTime;
        answerOutputTile = myPollElement.answerOutputTile;
    }

    @Test
    public void testCreation() {
        assertEquals("Test question?" ,question);
        assertEquals("ANS1,ANS2", answers);
        assertFalse(timerStart);
        assertEquals(3, timeLimit);
    }

    @Test
    public void testTimer() {
        assertFalse(timerStart);

        clickOn(startTimer);
        answerButton = myPollElement.answerButton;

        sleep(500);
        assertEquals("Time Remaining: 3", remainingTime.getText());
        sleep(1000);
        assertEquals("Time Remaining: 2", remainingTime.getText());
        sleep(1000);
        assertEquals("Time Remaining: 1", remainingTime.getText());
        sleep(1000);
        assertEquals("Time Remaining: 0", remainingTime.getText());
    }

    @Ignore
    @Test
    public void testAnswerButtons() {
        clickOn(startTimer);
        answerButton = myPollElement.answerButton;

        Platform.runLater(new Runnable() {
            @Override public void run() {
                for(ToggleButton temp : answerButton) {
                    controlPanel.getChildren().add(temp);
                }
            }
        });
        sleep(500);

        //TODO @Luke No longer works due to no instance of StudentSession
        clickOn(answerButton[0]);
        sleep(1000);
        clickOn(answerButton[1]);
        sleep(2000);

        answerOutputTile = myPollElement.answerOutputTile;
        assertEquals(1.0, answerOutputTile.getRadialChartData().get(1).getValue(), 0);
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
