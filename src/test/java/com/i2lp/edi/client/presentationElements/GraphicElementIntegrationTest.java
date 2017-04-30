package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.presentationViewer.TeacherPresentationManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;


/**
 * Created by zain on 22/04/2017.
 * Initialises a presentation for verifying that Graphic Elements work correctly.
 */
public class GraphicElementIntegrationTest extends Application{
		public static void main(String[] args){
		launch(args);
	}


	@Override
    public void start(Stage primaryStage){
        TeacherPresentationManager presentationManager = new TeacherPresentationManager();
        File file = new File("projectResources/sampleFiles/xmlTests/graphicElementTestXml.xml");

        presentationManager.openPresentation(file.toURI().toString());
    }
}

