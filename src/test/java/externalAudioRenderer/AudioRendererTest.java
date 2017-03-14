package externalAudioRenderer;

import com.elox.Parser.Audio.Audio;

import javafx.scene.media.MediaPlayer;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Luke on 05/03/2017.
 */
public class AudioRendererTest {

    //TODO won't work without javaFX envionment to instantiate in. See Manual test suite.

    Audio xmlAudioElement;
    AudioRenderer audioRendererUnderTest;

    @Before
    public void setUp() {
        xmlAudioElement = new Audio();
        setupEloxTestAudio();

        audioRendererUnderTest = new AudioRenderer(xmlAudioElement);
    }

    public void setupEloxTestAudio () {
        xmlAudioElement.setId(1);
        xmlAudioElement.setStartSequence(1);
        xmlAudioElement.setEndSequence(2);
        xmlAudioElement.setDuration(1);
        xmlAudioElement.setPath("externalResources/NorwegianPimsleur.mp3");
        xmlAudioElement.setLooped(Boolean.TRUE);
        xmlAudioElement.setAutoplayOn(Boolean.TRUE);
        xmlAudioElement.setStartTime(0);
        xmlAudioElement.setEndTime(10);
    }

    @Test
    public void verifyMediaPlayer()  {
        assertNotNull(audioRendererUnderTest.getAudioPlayer());
    }

    @Test
    public void verifyStatus() {
        assertEquals(MediaPlayer.Status.PLAYING, audioRendererUnderTest.getAudioPlayer().getStatus().toString());
    }

    @Test
    public void stop(){

    }

    @Test
    public void togglePlaying() {

    }

    @Test
    public void goToStart() {

    }

}