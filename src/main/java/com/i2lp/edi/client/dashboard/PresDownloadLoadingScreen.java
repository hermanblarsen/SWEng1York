package com.i2lp.edi.client.dashboard;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Created by Kacper on 2017-06-02.
 *
 * Displays a loading indicator for downloading presentations.
 */
public class PresDownloadLoadingScreen extends Application {

    private static final double SCENE_WIDTH = 300;
    private static final double SCENE_HEIGHT = 200;
    private BorderPane rootPane;
    private ProgressBar progressBar;
    private int numOfMissingPres = -1; //Set it to -1 so if not initialised, progress in progress bar will be set negative resulting in a nice animation

    @Override
    public void start(Stage primaryStage) {
        rootPane = new BorderPane();
        Scene scene = new Scene(rootPane, SCENE_WIDTH, SCENE_HEIGHT);

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        //primaryStage.show();
    }

    public void goToPresDownloadingState() {
        Label downloadLabel = new Label("Downloading new presentations from server...");
        downloadLabel.setPadding(new Insets(10));
        rootPane.setTop(downloadLabel);

        progressBar = new ProgressBar();
        progressBar.setPadding(new Insets(10));
        rootPane.setCenter(progressBar);
    }

    public void setNumOfMissingPres(int num) {
        numOfMissingPres = num;
    }

    public void updateDownloadState(int progress) {
        progressBar.setProgress(progress/numOfMissingPres);
    }

    public void exitPresDownloadingState() {
        //Platform.exit();
    }
}
