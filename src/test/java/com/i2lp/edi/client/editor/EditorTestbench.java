package com.i2lp.edi.client.editor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Luke on 06/05/2017.
 */
public class EditorTestbench extends Application {
    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        PresentationEditor presEd = new PresentationEditor("");
    }
}
