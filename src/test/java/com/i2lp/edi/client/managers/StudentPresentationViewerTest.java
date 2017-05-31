package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.utilities.ParserXML;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by Luke on 30/05/2017.
 */
public class StudentPresentationViewerTest extends PresentationViewerTest {
    private Stage questionStage;

    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

        EdiManager ediManager = new EdiManager();
        myPresentationManager = new PresentationManagerStudent(ediManager);

        File file = new File("projectResources/sampleFiles/xml/sampleXmlSimple.xml");
        ParserXML parser = new ParserXML("projectResources/sampleFiles/xml/sampleXmlSimple.xml");
        myPresentation = parser.parsePresentation();

        myPresentationManager.openPresentation(myPresentation, false);

        stage.toFront();
    }

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);

        presControls = myPresentationManager.presControls;
        drawControls = myPresentationManager.drawControls;
        commentPanel = myPresentationManager.commentPanel;
        drawPane = myPresentationManager.drawPane;
        displayPane = myPresentationManager.displayPane;

        leftButton = (ImageView) presControls.getChildren().get(0);
        rightButton = (ImageView) presControls.getChildren().get(1);
        fullscreenButton = (ImageView) presControls.getChildren().get(2);
        questionButton = (ImageView) presControls.getChildren().get(4);
        commentButton = (ImageView) presControls.getChildren().get(5);
        drawButton = (ImageView) presControls.getChildren().get(6);
        visibleButton = (ImageView) presControls.getChildren().get(7);
    }

    @Test
    public void testTeacherToolkitCreation() {
        assertEquals(null, ((PresentationManagerStudent) myPresentationManager).questionQueueStage);
        clickOn(questionButton);
        assertTrue(((PresentationManagerStudent) myPresentationManager).questionQueueStage.isShowing());
    }
}
