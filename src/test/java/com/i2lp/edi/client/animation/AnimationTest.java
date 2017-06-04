package com.i2lp.edi.client.animation;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.managers.PresentationManager;
import com.i2lp.edi.client.managers.PresentationManagerStudent;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.utilities.ParserXML;
import com.i2lp.edi.server.packets.PresentationMetadata;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.TimeoutException;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;

/**
 * Created by Luke on 03/06/2017.
 */
public class AnimationTest extends ApplicationTest {
    private PresentationManager myPresentationManager;

    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

        EdiManager ediManager = new EdiManager();
        myPresentationManager = new PresentationManagerStudent(ediManager);

        ParserXML parser = new ParserXML("projectResources/sampleFiles/xml/sampleXmlSimple.xml");
        Presentation pres = parser.parsePresentation();

        pres.setPresentationMetadata(new PresentationMetadata(
                0,0,0, "", false, null));
        myPresentationManager.openPresentation(pres, false);

        stage.toFront();
    }

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);
    }

    @Test
    public void test() {
        //TODO @Luke
    }

    @After
    public void tearDown() {
        try {
            FxToolkit.hideStage();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }
}
