package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.animation.Animation;
import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.managers.PresentationManager;
import com.i2lp.edi.client.managers.PresentationManagerTeacher;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by Luke on 26/05/2017.
 */

@Ignore
public class WordCloudElementTest extends ApplicationTest {
    private WordCloudElement myWordCloudElement;
    private BorderPane wordCloudPane;

    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

        //EdiManager ediManager = new EdiManager();
        //PresentationManager presManager = new PresentationManagerTeacher(ediManager);
        //myWordCloudElement.setPresentationManager(presManager);

        wordCloudPane = new BorderPane();
        Scene scene = new Scene(wordCloudPane, 600, 600);

        myWordCloudElement = new WordCloudElement();
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
}
