package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.animation.Animation;
import com.i2lp.edi.client.managers.EdiManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileNotFoundException;

/**
 * Created by Luke on 06/06/2017.
 */
public class MediaHandlerTestbench extends Application {
    private BorderPane root;
    private Scene scene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        root = new BorderPane();
        scene = new Scene(root, 600, 600);
        root.setBottom(addSelector());

        primaryStage.setTitle("Media Handlers Testbench");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public HBox addSelector() {
        //Add selector buttons for each media handler
        Button audioButton = new Button("Audio");
        audioButton.setOnAction(event -> root.setCenter(addAudioPane()));

        Button imageButton = new Button("Images");
        imageButton.setOnAction(event -> root.setCenter(addImagePane()));

        Button videoButton = new Button("Video");
        videoButton.setOnAction(event -> root.setCenter(addVideoPane()));

        Button textButton = new Button("Text");
        textButton.setOnAction(event -> root.setCenter(addTextPane()));

        HBox selectorBox = new HBox(audioButton, imageButton, videoButton, textButton);

        return selectorBox;
    }

    public GridPane addAudioPane() {
        AudioElement audioElement = new AudioElement();

        //Set up element
        audioElement.setPath("projectResources/sampleFiles/NeverGonnaGiveYouUp.mp3");
        audioElement.setStartTime(Duration.millis(0));
        audioElement.isLoop(false);
        audioElement.isAutoPlay(false);
        audioElement.setMute(false);
        audioElement.setVolume(0.5f);
        audioElement.setPlaybackRate(1f);

        audioElement.setSlideCanvas(root);
        audioElement.renderElement(Animation.ENTRY_ANIMATION);

        //Add Play/pause button
        Button playButton = new Button("Play");
        playButton.setOnAction(event -> {
            if(playButton.getText().equals("Play")) {
                audioElement.startAudio();
                playButton.setText("Pause");
            }
            else if(playButton.getText().equals("Pause")) {
                audioElement.pauseAudio();
                playButton.setText("Play");
            }
        });

        //Add mute button
        Button muteButton = new Button("Mute");
        muteButton.setOnAction(event -> {
            if(muteButton.getText().equals("Mute")) {
                audioElement.setForceMute(true);
                muteButton.setText("Unmute");
            }
            else if(muteButton.getText().equals("Unmute")) {
                audioElement.setForceMute(false);
                muteButton.setText("Mute");
            }
        });

        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(event -> audioElement.pauseAudio());

        Text volumeText = new Text("Volume");
        Slider volumeSlider = new Slider(0.0f, 1.0f, 0.5f);
        volumeSlider.setOnMouseDragged(event -> audioElement.setVolume((float) volumeSlider.getValue()));

        Text rateText = new Text("Rate");
        Slider rateSlider = new Slider(0.0f, 1.0f, 0.5f);
        rateSlider.setOnMouseDragged(event -> audioElement.setPlaybackRate((float) rateSlider.getValue()));

        //Populate grid pane
        GridPane audioPane = new GridPane();
        audioPane.setHgap(10);
        audioPane.setVgap(10);
        audioPane.add(playButton, 0, 0);
        audioPane.add(muteButton, 0, 1);
        audioPane.add(volumeText, 0, 2);
        audioPane.add(volumeSlider, 1, 2);
        audioPane.add(rateText, 0, 3);
        audioPane.add(rateSlider, 1, 3);

        return audioPane;
    }

    private VBox addVideoPane() {
        GridPane gridPane = new GridPane();
        BorderPane canvas  = new BorderPane();
        VBox vBox = new VBox(gridPane, canvas);

        //Set up video element
        VideoElement videoElement = new VideoElement();
        videoElement.setPath("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv");
        videoElement.setPath("projectResources/sampleFiles/prometheus.mp4");
        videoElement.setAutoplay(true);
        videoElement.setAspectRatioLock(true);
        videoElement.setyPosition(0.5f);
        videoElement.setxPosition(0.5f);
        videoElement.setStartTime(Duration.millis(0));
        videoElement.setEndTime(Duration.millis(10000));
        videoElement.setxSize(1f);
        videoElement.setySize(1f);
        videoElement.setSlideWidth(600);
        videoElement.setSlideHeight(600);
        videoElement.setSlideCanvas(canvas);

        videoElement.renderElement(Animation.NO_ANIMATION);

        return vBox;
    }

    private VBox addImagePane() {
        GridPane gridPane = new GridPane();
        BorderPane canvas  = new BorderPane();
        VBox vBox = new VBox(gridPane, canvas);

        ImageElement imageElementFile = new ImageElement();
        ImageElement imageElementHttp = new ImageElement();

        //Set up image element
        try {
            imageElementFile.setLayer(1);
            imageElementFile.setVisibility(true);
            imageElementFile.setStartSequence(1);
            imageElementFile.setEndSequence(2);
            imageElementFile.setPath("projectResources/logos/ediLogo400x400.png");
            imageElementFile.aspectRatioLock(false);

            imageElementFile.setPosX(0.7f);
            imageElementFile.setPosY(0f);
            imageElementFile.setHeight(0.5f);
            imageElementFile.setWidth(0.5f);
            imageElementFile.setRotation(45);
            imageElementFile.setBorder(true);
            imageElementFile.setBorderWidth(0.05f);
            imageElementFile.setBorderColour("#000000FF");

            imageElementHttp.setLayer(1);
            imageElementHttp.setStartSequence(1);
            imageElementHttp.setEndSequence(2);
            imageElementHttp.setPath("https://www.google.co.uk/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png");
            imageElementHttp.aspectRatioLock(true);
            imageElementHttp.setVisibility(true);

            imageElementHttp.setPosX(0.1f);
            imageElementHttp.setPosY(0.1f);
            imageElementHttp.setWidth(0.4f);

            imageElementHttp.setSlideWidth(scene.getWidth());
            imageElementHttp.setSlideHeight(scene.getHeight());
            imageElementHttp.setSlideWidth(scene.getWidth());
            imageElementHttp.setSlideHeight(scene.getHeight());
        } catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        }

        //Do the functions which would normally be done by a presentation manager
        imageElementHttp.setSlideCanvas(canvas);
        imageElementFile.setSlideCanvas(canvas);

        imageElementHttp.renderElement(Animation.ENTRY_ANIMATION);
        imageElementFile.renderElement(Animation.ENTRY_ANIMATION);

        return vBox;
    }

    private VBox addTextPane() {
        GridPane gridPane = new GridPane();
        BorderPane canvas = new BorderPane();
        VBox vBox = new VBox(gridPane, canvas);

        //Set up text element
        TextElement textElement = new TextElement();
        textElement.setPresentation(new Presentation());
        textElement.setEdiManager(new EdiManager());
        textElement.setTextContent("Test Text");

        textElement.setFont("Arial");
        textElement.setFontSize(12);
        textElement.setFontColour("#AF4567");
        textElement.setBgColour("#FFFFFF");
        textElement.setBorderColour("#000000");
        textElement.setBorderSize(20);
        textElement.setHasBorder(false);

        textElement.setSlideWidth(scene.getWidth());
        textElement.setSlideHeight(scene.getHeight());
        textElement.setSlideCanvas(canvas);

        //Add controls
        TextField textField = new TextField();
        Button setTextButton = new Button("Set Text");
        setTextButton.setOnAction(event -> {
            textElement.setTextContent(textField.getText());
            textElement.renderElement(Animation.NO_ANIMATION);
        });

        gridPane.add(textField, 0, 0);
        gridPane.add(setTextButton, 1, 0);

        return vBox;
    }
}
