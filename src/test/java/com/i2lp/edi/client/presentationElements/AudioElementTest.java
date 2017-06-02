package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.animation.Animation;
import com.i2lp.edi.client.managers.PresentationManagerTeacher;
import com.i2lp.edi.client.utilities.ParserXML;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.FileNotFoundException;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;

/**
 * Created by Luke on 02/06/2017.
 */
@Ignore
public class AudioElementTest extends ApplicationTest {
    private AudioElement myAudioElement;
    private  PresentationManagerTeacher myPresentationManager;

    @Override
    public void start(Stage primaryStage){
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

        //Setup the root border pane, controls on the left.
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1000, 1000);

        //Setup the Element
        myAudioElement = new AudioElement();
        myAudioElement.setLayer(1);
        myAudioElement.setVisibility(true);
        myAudioElement.setStartSequence(1);
        myAudioElement.setEndSequence(2);
        myAudioElement.setPath("projectResources/sampleFiles/xmlTests/NeverGonnaGiveYouUp.mp3");

        //Do the functions which would normally be done by a presentation manager
        myAudioElement.setSlideCanvas(root);
        myAudioElement.renderElement(Animation.ENTRY_ANIMATION);

        primaryStage.setTitle("Audio Element Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);
    }

    @Test
    public void test() {

    }
}
