package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.managers.PresentationManager;
import com.i2lp.edi.client.managers.PresentationManagerTeacher;
import com.i2lp.edi.client.utilities.ParserXML;
import com.i2lp.edi.server.packets.PresentationMetadata;
import com.i2lp.edi.server.packets.User;
import eu.hansolo.tilesfx.Tile;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.*;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.TimeoutException;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by Luke on 26/05/2017.
 */

public class WordCloudElementTest extends ApplicationTest {
    private WordCloudElement myWordCloudElement;

    private Tile countdownTile;
    private String question;
    private int timeLimit;

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

        ParserXML parser = new ParserXML("projectResources/sampleFiles/xml/wordcloudSample.xml");
        Presentation pres = parser.parsePresentation();

        pres.setPresentationMetadata(new PresentationMetadata(
                0,0,0, "", true, null));

        ediManager.setPresentationManager(presManager);
        ediManager.getPresentationManager().openPresentation(pres, false);

        myWordCloudElement = (WordCloudElement) ediManager.getPresentationManager().getElement(0);

        stage.toFront();
    }

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);

        question = myWordCloudElement.question;
        timeLimit = myWordCloudElement.timeLimit;
    }

    @Test
    public void testCreation() {
        assertEquals("Test Question?", question);
        assertEquals(3, timeLimit);
    }

    @Test
    public void testStartElement() {
        assertFalse(myWordCloudElement.isTimerStart());
        clickOn(myWordCloudElement.getCoreNode());
        assertTrue(myWordCloudElement.isTimerStart());
    }

    @Ignore //TODO @Luke Dependent on testAddWords
    @Test
    public void testRestartElement() {
        clickOn(myWordCloudElement.getCoreNode());
        sleep(4000);
        assertTrue(myWordCloudElement.isTimerStart());

        clickOn(myWordCloudElement.getCoreNode(), MouseButton.SECONDARY);
        push(KeyCode.RIGHT);
        push(KeyCode.ENTER);
        assertFalse(myWordCloudElement.isTimerStart());
    }

    @Test
    public void testTimer() {
        clickOn(myWordCloudElement.getCoreNode());
        countdownTile = myWordCloudElement.countdownTile;

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
    public void testAddWords() {
        clickOn(myWordCloudElement.getCoreNode());

        clickOn(myWordCloudElement.words);
        write("Test1");
        clickOn(myWordCloudElement.sendWord);
        clickOn(myWordCloudElement.words);
        write("Test2");
        clickOn(myWordCloudElement.sendWord);

        sleep(4000);

        assertEquals("Test1", myWordCloudElement.wordList.get(0));
        assertEquals("Test2", myWordCloudElement.wordList.get(1));
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
