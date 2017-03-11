package eloxExternalAudioRenderer;

import com.elox.Parser.Audio.Audio;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;


/**
 * Created by habl on 11/03/2017.
 */
public class AudioRendererManualTest extends Application{
    private Scene scene;
    private BorderPane border;
    private Stage primaryStage;
    private Audio xmlAudioElement;
    private AudioRenderer audioRendererUnderTest;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage){
        setupUUT();

        primaryStage.setTitle("Elox Audio Test");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Scene testScene = new Scene(grid, 300, 300);
        primaryStage.setScene(testScene);

        Text testText = new Text("Test for Elox Audio Module");
        grid.add(testText, 0, 0);

        Button playAudioButton = new Button("Play");
        playAudioButton.setOnAction((ActionEvent event)->{
            audioRendererUnderTest.play();
        });
        grid.add(playAudioButton, 0, 1);

        Button toggleAudioButton = new Button("Toggle");
        toggleAudioButton.setOnAction((ActionEvent event)->{
            audioRendererUnderTest.togglePlaying();
        });
        grid.add(toggleAudioButton, 0, 2);

        primaryStage.show();
    }

    public void setupUUT() {
        xmlAudioElement = new Audio();
        xmlAudioElement.setId(1);
        xmlAudioElement.setStartSequence(1);
        xmlAudioElement.setEndSequence(2);
        xmlAudioElement.setDuration(1);
        xmlAudioElement.setPath("externalResources/NorwegianPimsleur.mp3");
        xmlAudioElement.setLooped(Boolean.TRUE);
        xmlAudioElement.setAutoplayOn(Boolean.TRUE);
        xmlAudioElement.setStartTime(0);
        xmlAudioElement.setEndTime(10);

        audioRendererUnderTest = new AudioRenderer(xmlAudioElement);
    }
}
