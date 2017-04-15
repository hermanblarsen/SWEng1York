package com.i2lp.edi.client.utilities;

import com.i2lp.edi.client.presentationElements.VideoElement;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kma517 on 08/03/2017.
 */
@Ignore
public class VideoElementOLDTest {
    private VideoElement myVideoElement;
    Pane videoPane = new Pane();

    @Before
    public void setUp() throws Exception {
        myVideoElement = new VideoElement();
        myVideoElement.setPath("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv");
        myVideoElement.setAutoplay(true);
        myVideoElement.setAspectRatioLock(true);
        myVideoElement.setyPosition(5);
        myVideoElement.setxPosition(6);
        myVideoElement.setStartTime(Duration.seconds(5));
        myVideoElement.setEndTime(Duration.seconds(7));
        myVideoElement.setxSize(2000);
        myVideoElement.setySize(3000);
        myVideoElement.setSlideCanvas(videoPane);
        myVideoElement.setupElement();
        myVideoElement.doClassSpecificRender();
    }

    @Test
    public void verifyVideoPath(){
        assertEquals("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv",myVideoElement.getMp().getMedia().getSource());
    }

    @Test
    public void verifyAutoPlay(){
        assertEquals(true, myVideoElement.getMp().isAutoPlay());
    }

    @Test
    public void verifyStartAndEndTime(){

        assertEquals(Duration.seconds(5),myVideoElement.getMp().getStartTime());

        assertEquals(Duration.seconds(7),myVideoElement.getMp().getStopTime());
    }

//    @Test
//    public void verifyMediaPlayerStatus(){
//        //TODO: Getting the video player status doesn't currently work, will fix later
//        assertEquals(MediaPlayer.Status.PLAYING,myVideoElement.getVideoStatus());
//        myVideoElement.pauseVideo();
//        assertEquals(MediaPlayer.Status.PAUSED,myVideoElement.getVideoStatus());
//        myVideoElement.stopVideo();
//        assertEquals(MediaPlayer.Status.STOPPED,myVideoElement.getVideoStatus());
//    }

    @Test
    public void verifyAspectRatioLock(){
        assertEquals(true,myVideoElement.getMv().isPreserveRatio() );
    }

    @Test
    public void verifyTranslate(){
        assertEquals(5,myVideoElement.getMv().getTranslateY(),1e-8);
    }

    @Test
    public void verifySize(){
        assertEquals(2000,myVideoElement.getMv().getFitWidth(),1e-8);
        assertEquals(3000,myVideoElement.getMv().getFitHeight(),1e-8);
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("Done");
    }

}