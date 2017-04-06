package client.presentationViewer;

import client.managers.PresentationManager;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


/**
 * Created by kma517 on 16/03/2017.
 */
public class StudentPresentationManager extends PresentationManager {
    public StudentPresentationManager(Stage primaryStage, Scene scene, BorderPane border, String path) {
        super(primaryStage, scene, border, path);
    }


    @Override
    protected void questionQueueFunction() {
        System.out.println("Question Queue: Not yet implemented");
    }

    @Override
    protected void commentFunction() {
        System.out.println("Commenting: Not yet implemented");
    }
}

