package client.utilities;

import client.presentationElements.VideoElement;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kma517 on 08/03/2017.
 */
public class VideoElementTest {
    private VideoElement myVideoElement;
    Pane videoPane = new Pane();

    @Before
    public void setUp() throws Exception {
        myVideoElement = new VideoElement();
        myVideoElement.setMediaPath("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv");
        myVideoElement.setAutoPlay(true);
        myVideoElement.setAspectRatioLock(true);
        myVideoElement.setyPosition(5);
        myVideoElement.setxPosition(6);
        myVideoElement.setVideoStartTime(Duration.seconds(5));
        myVideoElement.setVideoEndTime(Duration.seconds(7));
        myVideoElement.setxSize(2000);
        myVideoElement.setySize(3000);
        myVideoElement.setSlideCanvas(videoPane);

    }

    @Test
    public void verifyVideoPath(){
        assertEquals("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv",myVideoElement.getMediaPlayer().getMedia().getSource());
    }

    @Test
    public void verifyAutoPlay(){
        assertEquals(true, myVideoElement.getMediaPlayer().isAutoPlay());
    }

    @Test
    public void verifyStartAndEndTime(){

        assertEquals(Duration.seconds(5),myVideoElement.mp.getStartTime());

        assertEquals(Duration.seconds(7),myVideoElement.mp.getStopTime());
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
        assertEquals(true,myVideoElement.mv.isPreserveRatio() );
    }

    @Test
    public void verifyTranslate(){
        assertEquals(5,myVideoElement.mv.getTranslateY(),1e-8);
    }

    @Test
    public void verifySize(){
        assertEquals(2000,myVideoElement.mv.getFitWidth(),1e-8);
        assertEquals(3000,myVideoElement.mv.getFitHeight(),1e-8);
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("Done");
    }

}