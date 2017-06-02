package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.utilities.ParserXML;
import com.i2lp.edi.server.packets.PresentationMetadata;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by Luke on 30/05/2017.
 */
public class TeacherPresentationViewerTest extends PresentationViewerTest {

    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

        EdiManager ediManager = new EdiManager();
        myPresentationManager = new PresentationManagerTeacher(ediManager);

        ParserXML parser = new ParserXML("projectResources/sampleFiles/xml/sampleXmlSimple.xml");
        myPresentation = parser.parsePresentation();

        myPresentation.setPresentationMetadata(new PresentationMetadata(
                0,0,0, "", false, null));
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
        //toolkitButton = (ImageView) presControls.getChildren().get(3);
        commentButton = (ImageView) presControls.getChildren().get(3);
        drawButton = (ImageView) presControls.getChildren().get(4);
        visibleButton = (ImageView) presControls.getChildren().get(5);
    }

    @Ignore //TODO @Luke Find way to test online features
    @Test
    public void testTeacherToolkitCreation() {
        assertEquals(null, ((PresentationManagerTeacher) myPresentationManager).teacherToolKit);
        clickOn(toolkitButton);
        assertTrue(((PresentationManagerTeacher) myPresentationManager).teacherToolKit.isShowing());
    }
}
