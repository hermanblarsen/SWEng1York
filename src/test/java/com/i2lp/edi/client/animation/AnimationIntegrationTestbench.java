package com.i2lp.edi.client.animation;

import com.i2lp.edi.client.managers.PresentationManagerTeacher;
import com.i2lp.edi.client.utilities.ParserXML;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.FileNotFoundException;

/**
 * Created by zain on 22/04/2017.
 */
public class AnimationIntegrationTestbench extends Application {

	public static void main(String[] args){
		launch(args);
	}


    @Override
    public void start(Stage primaryStage){
        PresentationManagerTeacher presentationManager = new PresentationManagerTeacher(null);
        ParserXML parserXML = null;
        try {
            parserXML = new ParserXML("file:projectResources/sampleFiles/xml/i2lpSampleXml.xml");
        } catch (FileNotFoundException e) {
            //FileNotFound.eat()
        }

        presentationManager.openPresentation(parserXML.parsePresentation(), false);
    }
}
