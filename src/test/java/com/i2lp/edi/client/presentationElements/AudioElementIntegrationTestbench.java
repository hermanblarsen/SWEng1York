package com.i2lp.edi.client.presentationElements;

import com.i2lp.edi.client.managers.PresentationManagerTeacher;
import com.i2lp.edi.client.utilities.ParserXML;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertTrue;

/**
 * Created by zain
 * Initialises a presentation for verifying that Audio Elements work correctly.
 */
public class AudioElementIntegrationTestbench extends Application {
    public static void main(String[] args){
        launch(args);
    }


    @Override
    public void start(Stage primaryStage){
        PresentationManagerTeacher presentationManager = new PresentationManagerTeacher(null);
        ParserXML parserXML = null;
        try {
            parserXML = new ParserXML("file:projectResources/sampleFiles/xmlTests/audioElementTest.xml");
        } catch (FileNotFoundException e) {
            //FileNotFound.eat()
        }

        presentationManager.openPresentation(parserXML.parsePresentation(),false);
    }

    @Test
    public void emptyTest() {
        // this is to satisfy JUnits need for runnable methods
        assertTrue(true);
    }
}

