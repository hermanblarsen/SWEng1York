package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.presentationElements.VideoElement;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.*;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.util.concurrent.TimeoutException;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by kma517 on 08/03/2017.
 */
public class VideoElementTest extends ApplicationTest {
    private VideoElement myVideoElement;
    Pane videoPane;

    //This operation comes from ApplicationTest and loads the GUI to test.
    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }
        videoPane = new Pane();
        // Put the GUI in front of windows
        stage.toFront();
        stage.show();
    }

    @Before
    public void setUp() throws Exception {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);

        myVideoElement = new VideoElement();
        myVideoElement.setPath("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv");
        myVideoElement.setPath("projectResources/sampleFiles/prometheus.mp4");
        myVideoElement.setAutoplay(true);
        myVideoElement.setAspectRatioLock(true);
        myVideoElement.setyPosition(0f);
        myVideoElement.setxPosition(0f);
        myVideoElement.setStartTime(Duration.millis(0));
        myVideoElement.setEndTime(Duration.millis(300));
        myVideoElement.setxSize(0.5f);
        myVideoElement.setySize(0.5f);
        myVideoElement.setSlideWidth(1000);
        myVideoElement.setSlideHeight(1000);
        myVideoElement.setSlideCanvas(videoPane);
        myVideoElement.setupElement();
        myVideoElement.doClassSpecificRender();
    }

    @Test
    public void verifyVideoPath(){
        File source = new File("projectResources/sampleFiles/prometheus.mp4");
        assertEquals(source.toURI().toString(), myVideoElement.getMediaPlayer().getMedia().getSource());
    }

    @Test
    public void verifyStartAndEndTime(){
        assertEquals(Duration.millis(0),myVideoElement.getMediaPlayer().getStartTime());
        assertEquals(Duration.millis(300),myVideoElement.getMediaPlayer().getStopTime());
    }

    @Test
    public void verifyMediaPlayerStatus(){
        sleep(200);
        assertEquals(MediaPlayer.Status.PLAYING,myVideoElement.getMediaPlayer().getStatus());
        myVideoElement.getMediaPlayer().pause();
        sleep(200);
        assertEquals(MediaPlayer.Status.PAUSED,myVideoElement.getMediaPlayer().getStatus());
        myVideoElement.getMediaPlayer().stop();
        sleep(200);
        assertEquals(MediaPlayer.Status.STOPPED,myVideoElement.getMediaPlayer().getStatus());
    }

    @Test
    public void verifyAspectRatioLock(){
        assertEquals(true,myVideoElement.getMediaView().isPreserveRatio() );
    }

//    @Test //TODO seems to produce fatal error SIGBUS (0xa) at pc=0x00007fecaacd0bd0, pid=2993, tid=0x0000000000018807. Fix
//    public void verifyTranslate(){
//        assertEquals(0*1000,myVideoElement.getMediaView().getTranslateY(),1e-8);
//    }

    @Test
    public void verifySize(){
        assertEquals(0.5*1000,myVideoElement.getMediaView().getFitWidth(),1e-8);
        assertEquals(0.5*1000,myVideoElement.getMediaView().getFitHeight(),1e-8);
    }

    @After
    public void tearDown()  {
        myVideoElement.getMediaPlayer().stop();
        myVideoElement.getMediaPlayer().dispose();
        try {
            FxToolkit.hideStage();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }
}