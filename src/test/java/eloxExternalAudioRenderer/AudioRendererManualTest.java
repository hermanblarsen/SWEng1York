package eloxExternalAudioRenderer;

import com.elox.Parser.Audio.Audio;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by habl on 11/03/2017.
 */
public class AudioRendererManualTest extends Application {
    protected Audio xmlAudioElement;
    protected AudioRenderer audioRendererUnderTest;
    protected String audioStatus;
    protected MediaPlayer audioPlayer;
    protected GridPane gridPane;
    protected Timer myTimer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        setupUUT();
        myTimer = new Timer();
        primaryStage.setTitle("Elox Audio Test");

        gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        Scene testScene = new Scene(gridPane, 900, 300);
        primaryStage.setScene(testScene);

        addText();
        addButtons();

        primaryStage.show();
    }

    private void addButtons() {
        int i = 0;
        Button playAudioButton = new Button("Play");
        playAudioButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.play();
        });
        gridPane.add(playAudioButton, i, 1);

        i++;
        Button toggleAudioButton = new Button("Toggle");
        toggleAudioButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.togglePlaying();
            audioStatus = audioRendererUnderTest.getAudioPlayer().getStatus().toString();
        });
        gridPane.add(toggleAudioButton, i, 1);

        i++;
        Button pauseAudioButton = new Button("Pause");
        pauseAudioButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.pause();
        });
        gridPane.add(pauseAudioButton, i, 1);

        i++;
        Button stopAudioButton = new Button("Stop");
        stopAudioButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.stop();
        });
        gridPane.add(stopAudioButton, i, 1);

        i++;
        Button volumeUpButton = new Button("VolumeUp");
        volumeUpButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.setVolume(audioRendererUnderTest.getVolume() + 0.1f);
        });
        gridPane.add(volumeUpButton, i, 1);

        i++;
        Button volumeDownButton = new Button("VolumeDown");
        volumeDownButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.setVolume(audioRendererUnderTest.getVolume() - 0.1f);
        });
        gridPane.add(volumeDownButton, i, 1);
    }

    private void addText() {
        Text testText = new Text("Test for Elox Audio Module");
        gridPane.add(testText, 0, 0, 5, 1);


        Text statusText = new Text("Status: " + audioStatus);
        gridPane.add(statusText, 15, 0, 10, 5);
        TimerTask updateStatusField = new TimerTask() {
            @Override
            public void run() {
                audioStatus = audioPlayer.getStatus().toString();
                statusText.setText("Status: " + audioStatus + System.getProperty("line.separator")
                        + ", Volume: " + String.format("%.2f", audioPlayer.getVolume()) + System.getProperty("line.separator")
                        + ", CurrentTime: " + audioPlayer.getCurrentTime().toString());
            }
        };
        myTimer.schedule(updateStatusField, 0, 200);
    }

    public void setupUUT() {
        xmlAudioElement = new Audio();
        xmlAudioElement.setId(1);
        xmlAudioElement.setPath("externalResources/example.mp3");
        xmlAudioElement.setLooped(Boolean.TRUE);
        xmlAudioElement.setAutoplayOn(Boolean.TRUE);
        xmlAudioElement.setStartTime(5000);
        xmlAudioElement.setEndTime(15000);

        audioRendererUnderTest = new AudioRenderer(xmlAudioElement);
        audioPlayer = audioRendererUnderTest.getAudioPlayer();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        myTimer.cancel();
        Platform.exit();
    }
}