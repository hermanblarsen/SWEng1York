package eloxExternalAudioRenderer;

import com.elox.Parser.Audio.Audio;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Luke on 05/03/2017.
 */
public class AudioRendererTest {

    Audio xmlAudioElement;
    AudioRenderer audioRenderer;

    @Before
    public void setUp() {
        xmlAudioElement = new Audio();
        setupEloxTestAudio();
        audioRenderer = new AudioRenderer(xmlAudioElement);

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