package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.animation.Animation;
import com.i2lp.edi.client.managers.PresentationManagerTeacher;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.*;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.TimeoutException;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by Luke on 02/06/2017.
 */

@Ignore
public class AudioElementTest extends ApplicationTest {
    private AudioElement myAudioElement;
    private  PresentationManagerTeacher myPresentationManager;
    private BorderPane root;

    @Override
    public void start(Stage primaryStage){
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

        //Setup the root border pane, controls on the left.
        root = new BorderPane();
        Scene scene = new Scene(root, 1000, 1000);

        //Setup the Element
        myAudioElement = new AudioElement();
        myAudioElement.setLayer(1);
        myAudioElement.setVisibility(true);
        myAudioElement.setStartSequence(1);
        myAudioElement.setEndSequence(2);
        myAudioElement.setPath("projectResources/sampleFiles/NeverGonnaGiveYouUp.mp3");

        myAudioElement.setStartTime(Duration.millis(0));
        myAudioElement.isLoop(false);
        myAudioElement.isAutoPlay(false);
        myAudioElement.setMute(false);
        myAudioElement.setVolume(0.5f);
        myAudioElement.setPlaybackRate(1f);

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
    public void testCreation() {
        assertEquals(Duration.millis(0), myAudioElement.getStartTime());
        assertEquals(false, myAudioElement.getLoop());
        assertEquals(false, myAudioElement.getAutoPlay());
        assertEquals(false, myAudioElement.isMute());
        assertEquals(0.5f, myAudioElement.getVolume(), 0);
        assertEquals(1f, myAudioElement.getPlaybackRate(), 0);
    }

    @Test
    public void testStartTime() {
        myAudioElement.setStartTime(Duration.millis(100));
        assertEquals(Duration.millis(100), myAudioElement.getStartTime());
    }

    @Ignore
    @Test
    public void testEndTime() {
        myAudioElement.setEndTime(Duration.millis(1000));
        assertEquals(Duration.millis(1000), myAudioElement.getEndTime());

        //TODO @Luke
        /*
        myAudioElement.startAudio();
        assertTrue(myAudioElement.isPlaying());
        sleep(1500);
        assertFalse(myAudioElement.isPlaying());
        */
    }

    @Test
    public void testLoop() {
        myAudioElement.setCurrentTime(myAudioElement.getEndTime().subtract(Duration.millis(10)));

        myAudioElement.isLoop(true);
        assertEquals(true, myAudioElement.getLoop());

        myAudioElement.startAudio();
        sleep(1000);

        assertTrue(myAudioElement.isPlaying());
        assertEquals(1, myAudioElement.getCurrentCount());
    }

    @Test
    public void testAutoPlay() {
        myAudioElement.isAutoPlay(true);
        assertEquals(true, myAudioElement.getAutoPlay());

        myAudioElement.setSlideCanvas(root);
        myAudioElement.renderElement(Animation.ENTRY_ANIMATION);

        sleep(1000);
        assertTrue(myAudioElement.isPlaying());

        myAudioElement.startAudio();
    }

    @Test
    public void testMute() {
        myAudioElement.setMute(true);
        assertEquals(true, myAudioElement.isMute());
    }

    @Test
    public void testVolume() {
        myAudioElement.setVolume(0.2f);
        assertEquals(0.2f, myAudioElement.getVolume(), 0);
    }

    @Test
    public void testPlaybackRate() {
        myAudioElement.setPlaybackRate(0.5f);
        assertEquals(0.5f, myAudioElement.getPlaybackRate(), 0);
    }

    @Test
    public void testPlay() {
        myAudioElement.startAudio();
        sleep(200);
        assertTrue(myAudioElement.isPlaying());
    }

    @Test
    public void testPause() {
        myAudioElement.startAudio();
        sleep(1000);

        myAudioElement.pauseAudio();
        double currentTime = myAudioElement.getCurrentTime().toMillis();
        assertFalse(myAudioElement.isPlaying());

        myAudioElement.startAudio();
        assertEquals(currentTime, myAudioElement.getCurrentTime().toMillis(), 200);
    }

    @Test
    public void testStop() {
        myAudioElement.startAudio();
        sleep(100);

        myAudioElement.stopAudio();
        assertNull(myAudioElement.getMediaPlayer());
    }

    @Test
    public void testToggle() {
        myAudioElement.toggleAudio();
        sleep(200);
        assertTrue(myAudioElement.isPlaying());
        myAudioElement.toggleAudio();
        sleep(200);
        assertFalse(myAudioElement.isPlaying());
        myAudioElement.toggleAudio();
        sleep(200);
        assertTrue(myAudioElement.isPlaying());
    }

    @After
    public void tearDown() {
        myAudioElement.stopAudio();

        try {
            FxToolkit.hideStage();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }
}
