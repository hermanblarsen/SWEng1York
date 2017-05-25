package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.managers.PresentationManagerTeacher;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;

/**
 * Created by zain on 25/05/2017.
 */
public class ImageElementIntegrationTestbench extends Application{
	public static void main(String[] args){
		launch(args);
	}


	@Override
	public void start(Stage primaryStage){
		PresentationManagerTeacher presentationManager = new PresentationManagerTeacher();
		File file = new File("projectResources/sampleFiles/xmlTests/imageElementTestXml.xml");

		presentationManager.openPresentation(file.toURI().toString(),false);
	}


}

