package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.animation.Animation;
import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.managers.PresentationManager;
import com.i2lp.edi.client.managers.PresentationManagerStudent;
import com.i2lp.edi.client.managers.PresentationManagerTeacher;
import com.i2lp.edi.client.utilities.GraphicalTest;
import com.i2lp.edi.client.utilities.ParserXML;
import com.i2lp.edi.server.packets.PresentationMetadata;
import com.i2lp.edi.server.packets.User;
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

public class PollElementTest extends GraphicalTest {
    private PollElement myPollElement;
    private ToggleButton[] answerButton;

    private String question;
    private String answers;
    private int timeLimit;
    private Tile answerOutputTile;
    private Tile countdownTile;

    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

        EdiManager ediManager = new EdiManager();
        ediManager.start(stage);
        ediManager.loginSucceeded(true, new User(1, "First", "Last", "email", "teacher"));

        PresentationManager presManager = new PresentationManagerTeacher(ediManager);

        ParserXML parser = new ParserXML("projectResources/sampleFiles/xml/pollSample.xml");
        Presentation pres = parser.parsePresentation();

        pres.setPresentationMetadata(new PresentationMetadata(
                0,0,0, "", true, null));

        ediManager.setPresentationManager(presManager);
        ediManager.getPresentationManager().openPresentation(pres, false);

        myPollElement = (PollElement) ediManager.getPresentationManager().getElement(0);

        stage.toFront();
    }

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);

        question = myPollElement.question;
        answers = myPollElement.answers;
        timeLimit = myPollElement.timeLimit;
        answerOutputTile = myPollElement.answerOutputTile;
        countdownTile = myPollElement.countdownTile;
    }

    @Test
    public void testCreation() {
        assertEquals("Test question?" ,question);
        assertEquals("ANS1,ANS2", answers);
        assertEquals(3, timeLimit);
    }

    @Test
    public void testStartElement() {
        assertFalse(myPollElement.isTimerStart());
        clickOn(myPollElement.getCoreNode());
        assertTrue(myPollElement.isTimerStart());
    }

    @Ignore //TODO @Luke Dependent on testAnswerButtons
    @Test
    public void testRestartElement() {
        clickOn(myPollElement.getCoreNode());
        sleep(4000);
        assertTrue(myPollElement.isTimerStart());

        clickOn(myPollElement.getCoreNode(), MouseButton.SECONDARY);
        push(KeyCode.RIGHT);
        push(KeyCode.ENTER);
        assertFalse(myPollElement.isTimerStart());
    }

    @Test
    public void testTimer() {
        clickOn(myPollElement.getCoreNode());
        countdownTile = myPollElement.countdownTile;

        sleep(500);
        assertEquals(3.0, countdownTile.getCurrentValue(), 0);
        sleep(1000);
        assertEquals(2.0, countdownTile.getCurrentValue(), 0);
        sleep(1000);
        assertEquals(1.0, countdownTile.getCurrentValue(), 0);
        sleep(1000);
        assertEquals(0.0, countdownTile.getCurrentValue(), 0);
    }

    @Ignore //TODO @Luke No longer works
    @Test
    public void testAnswerButtons() {
        clickOn(myPollElement.getCoreNode());
        answerButton = myPollElement.answerButton;

        clickOn(answerButton[0]);
        sleep(1000);
        clickOn(answerButton[1]);
        sleep(1000);

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
