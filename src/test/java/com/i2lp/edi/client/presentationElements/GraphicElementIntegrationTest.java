package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.managers.PresentationManagerTeacher;
import com.i2lp.edi.client.utilities.ParserXML;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


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
        PresentationManagerTeacher presentationManager = new PresentationManagerTeacher();
        File file = new File("projectResources/sampleFiles/xmlTests/graphicElementTestXml.xml");

        ParserXML parserXML = new ParserXML(file.toURI().toString());

        presentationManager.openPresentation(parserXML.parsePresentation(),false);
    }

    @Test
    public void emptyTest() {
        //TODO Fill in actual tests, this is to satisfy JUnit
        assertTrue(true);
    }
}

