package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.animation.Animation;
import com.i2lp.edi.client.managers.PresentationManagerTeacher;
import com.i2lp.edi.client.utilities.ParserXML;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.Ignore;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.FileNotFoundException;

/**
 * Created by Luke on 02/06/2017.
 */
@Ignore
public class AudioElementTest extends ApplicationTest {
    private AudioElement myAudioElement;
    private  PresentationManagerTeacher myPresentationManager;

    @Override
    public void start(Stage primaryStage){
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
    }

    @Test
    public void setUp() {

    }
}
