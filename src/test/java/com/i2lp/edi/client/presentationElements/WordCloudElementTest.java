package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.animation.Animation;
import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.managers.PresentationManager;
import com.i2lp.edi.client.managers.PresentationManagerTeacher;
import com.i2lp.edi.client.utilities.ParserXML;
import com.i2lp.edi.server.packets.PresentationMetadata;
import com.i2lp.edi.server.packets.User;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.*;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.sql.Timestamp;
import java.util.concurrent.TimeoutException;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by Luke on 26/05/2017.
 */

@Ignore //TODO @Luke EdiManager fails to start
public class WordCloudElementTest extends ApplicationTest {
    private WordCloudElement myWordCloudElement;
    private BorderPane wordCloudPane;

    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

        /*
        myWordCloudElement = new WordCloudElement();

        EdiManager ediManager = new EdiManager();
        ediManager.start(stage);
        ediManager.loginSucceeded(true, new User(1, "First", "Last", "email", "teacher"));

        PresentationManager presManager = new PresentationManagerTeacher(ediManager);
        ediManager.setPresentationManager(presManager);
        myWordCloudElement.setEdiManager(ediManager);
        ParserXML parser = new ParserXML("projectResources/sampleFiles/xml/sampleXmlSimple.xml");
        Presentation pres = parser.parsePresentation();
        pres.setPresentationMetadata(new PresentationMetadata(
                0,0,0, "", true, null));

        Slide slide = new Slide();
        slide.addElement(0, myWordCloudElement);
        pres.addSlide(0, new Slide());

        ediManager.getPresentationManager().openPresentation(pres,false);
        */
        EdiManager ediManager = new EdiManager();
        ediManager.start(stage);
        //ediManager.loginSucceeded(true, new User(1, "First", "Last", "email", "teacher"));

        wordCloudPane = new BorderPane();
        Scene scene = new Scene(wordCloudPane, 600, 600);

        wordCloudPane.setBottom(myWordCloudElement.wordCloudElements());

        myWordCloudElement.setQuestion("Test Question?");
        myWordCloudElement.setTimeLimit(10);
        myWordCloudElement.setSlideWidth(scene.getWidth());
        myWordCloudElement.setSlideHeight(scene.getHeight());

        myWordCloudElement.setSlideCanvas(wordCloudPane);
        myWordCloudElement.renderElement(Animation.NO_ANIMATION);

        wordCloudPane.setTop(myWordCloudElement.remainingTime);

        stage.setTitle("Word Cloud Test");
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);
    }

    @Test
    public void testCreation() {
        assertEquals("Test Question?", myWordCloudElement.getQuestion());
        assertEquals(10, myWordCloudElement.getTimeLimit());
    }

    //@Ignore //TODO @Luke Fails due to null pointer to StudentSession
    @Test
    public void testAddWords() {
        clickOn(myWordCloudElement.words);
        write("Test1");
        clickOn(myWordCloudElement.sendWord);
        clickOn(myWordCloudElement.words);
        write("Test2");
        clickOn(myWordCloudElement.sendWord);

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
