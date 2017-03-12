package eloxExternalAudioRenderer;

import com.elox.Parser.Audio.Audio;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.media.MediaMarkerEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;


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
        Scene testScene = new Scene(gridPane, 1000, 300);
        primaryStage.setScene(testScene);

        addText();
        addButtons();

        setupAudioMarkerHandler();

        primaryStage.show();
    }

    private void addButtons() {
        int i = 0, c=3;
        Button playAudioButton = new Button("Play");
        playAudioButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.play();
        });
        gridPane.add(playAudioButton, i, c);

        i++;
        Button toggleAudioButton = new Button("Toggle");
        toggleAudioButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.togglePlaying();
            audioStatus = audioRendererUnderTest.getAudioPlayer().getStatus().toString();
        });
        gridPane.add(toggleAudioButton, i, c);

        i++;
        Button pauseAudioButton = new Button("Pause");
        pauseAudioButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.pause();
        });
        gridPane.add(pauseAudioButton, i, c);

        i++;
        Button stopAudioButton = new Button("Stop");
        stopAudioButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.stop();
        });
        gridPane.add(stopAudioButton, i, c);

        i++;
        Button volumeUpButton = new Button("VolumeUp");
        volumeUpButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.setVolume(audioRendererUnderTest.getVolume() + 0.1f);
        });
        gridPane.add(volumeUpButton, i, c);

        i++;
        Button volumeDownButton = new Button("VolumeDown");
        volumeDownButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.setVolume(audioRendererUnderTest.getVolume() - 0.1f);
        });
        gridPane.add(volumeDownButton, i, c);

        i++;
        Button skipForwardButton = new Button("Skip+ 5s");
        skipForwardButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.skip(Duration.seconds(5));
        });
        gridPane.add(skipForwardButton, i, c);

        i++;
        Button skipBackwardButton = new Button("Skip- 5s");
        skipBackwardButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.skip(Duration.seconds(-5));
        });
        gridPane.add(skipBackwardButton, i, c);

        i=0;
        c++;
        Button playFromButton = new Button("From 8s");
        playFromButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.playFrom(Duration.seconds(8));
        });
        gridPane.add(playFromButton, i, c);

        i++;
        Button playToButton = new Button("To 40s");
        playToButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.playTo(Duration.seconds(40));
        });
        gridPane.add(playToButton, i, c);

        i++;
        Button playFromAndToButton = new Button("From 2-25");
        playFromAndToButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.playFromTo(Duration.seconds(2), Duration.seconds(25));
        });
        gridPane.add(playFromAndToButton, i, c);

        i++;
        Button rateUpButton = new Button("Rate+");
        rateUpButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.setPlaybackSpeed(audioRendererUnderTest.getPlaybackSpeed() + 0.1f);
        });
        gridPane.add(rateUpButton, i, c);

        i++;
        Button rateDownButton = new Button("Rate-");
        rateDownButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.setPlaybackSpeed(audioRendererUnderTest.getPlaybackSpeed() - 0.1f);
        });
        gridPane.add(rateDownButton, i, c);

        i=0;
        c++;
        Button setNewMediaMarkerButton = new Button("New Interval 2s");
        setNewMediaMarkerButton.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.setMediaMarkerTimeInterval(Duration.seconds(2));
        });
        gridPane.add(setNewMediaMarkerButton, i, c, 2, 1);

        i+=2;
        Button updateMediaMarkers = new Button("Update Interval 3s");
        updateMediaMarkers.setOnAction((ActionEvent event) -> {
            audioRendererUnderTest.updateMediaMarkers(Duration.seconds(3));
        });
        gridPane.add(updateMediaMarkers, i, c, 2, 1);
    }

    private void addText() {
        Text testText = new Text("Test for Elox Audio Module");
        gridPane.add(testText, 1, 0, 5, 1);


        Text statusText = new Text("Status: " + audioStatus);
        gridPane.add(statusText, 10, 0, 5, 10);
        TimerTask updateStatusField = new TimerTask() {
            @Override
            public void run() {
                audioStatus = audioPlayer.getStatus().toString();
                statusText.setText("Status: " + audioStatus + System.getProperty("line.separator")
                        + "Volume: " + String.format("%.2f", audioRendererUnderTest.getVolume()) + System.getProperty("line.separator")
                        + "Playing: " + audioRendererUnderTest.isPlaying() + System.getProperty("line.separator")
                        + "Playbackspeed: " + String.format("%.2f", audioRendererUnderTest.getPlaybackSpeed()) + System.getProperty("line.separator")
                        + "StartTime: " + audioRendererUnderTest.getStartTime().toMillis() + " ms " + System.getProperty("line.separator")
                        + "EndTime: " + audioRendererUnderTest.getEndTime().toMillis() + " ms " + System.getProperty("line.separator")
                        + "CurrentTime: " + audioRendererUnderTest.getCurrentTime().toMillis() + " ms" + System.getProperty("line.separator")
                        + "MediaMarkerInterval: " + audioRendererUnderTest.getMediaMarkerTimeInterval().toMillis() + " ms");
            }
        };
        myTimer.schedule(updateStatusField, 0, 50);
    }

    public void setupUUT() {
        xmlAudioElement = new Audio();
        xmlAudioElement.setId(1);
        xmlAudioElement.setPath("externalResources/example.mp3");
        xmlAudioElement.setLooped(Boolean.TRUE);
        xmlAudioElement.setAutoplayOn(Boolean.TRUE);
        xmlAudioElement.setStartTime(0);
        xmlAudioElement.setEndTime(50000);

        audioRendererUnderTest = new AudioRenderer(xmlAudioElement);
        audioPlayer = audioRendererUnderTest.getAudioPlayer();
    }

    private void setupAudioMarkerHandler() {
        Text markerText = new Text("Marker INFO: ");
        gridPane.add(markerText, 1, 1, 5, 1);

        EventHandler mediaMarkerEventHandler = new EventHandler<MediaMarkerEvent>() {
            @Override
            public void handle(MediaMarkerEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        markerText.setText("Marker Content: " + event.getMarker().getKey()
                                + " at time " + event.getMarker().getValue());
                    }
                });
            }
        };
        audioRendererUnderTest.setMediaMarkerEventEventHandler(mediaMarkerEventHandler);

        boolean equalMarkers = false;
        if ( mediaMarkerEventHandler == audioRendererUnderTest.getMediaMarkerEventEventHandler()) equalMarkers = true;

        Text markeszEqualText = new Text("Eventhandlers equal comparing with getEventHandler(): " + equalMarkers);
        gridPane.add(markeszEqualText, 1, 2, 5, 1);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        myTimer.cancel();
        Platform.exit();
    }
}