package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.server.packets.User;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.*;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by Luke on 06/05/2017.
 */
public class TeacherDashboardTest extends ApplicationTest {
    private static TeacherDashboard myDashboard;

    private TextField searchField;
    private ArrayList<PresentationPanel> previewPanels;
    private Button showAllButton;
    private ArrayList<Button> subjectButtons;

    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

        EdiManager ediManager = new EdiManager();
        ediManager.start(stage);
        ediManager.loginSucceded(true, new User(1, "First", "Last", "email", "teacher"));
        stage.toFront();
        myDashboard = (TeacherDashboard) ediManager.getDashboard();
    }

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);

        searchField = myDashboard.searchField;
        previewPanels = myDashboard.presentationPanels;
        showAllButton = myDashboard.showAllButton;
        subjectButtons = myDashboard.subjectButtons;
    }

    @Test
    public void testSearch() {
        clickOn(searchField).write("test");
    }

    @Test
    public void testFilter() {
        Boolean showAllFailed = false;
        Boolean[] subjectFails = new Boolean[subjectButtons.size()];

        for(int i=0; i<subjectFails.length; i++) {
            subjectFails[i] = false;
        }

        clickOn(showAllButton);
        for(PresentationPanel temp : previewPanels) {
            if(temp.isHidden())
                showAllFailed = true;
        }

        for(Button subjectButton : subjectButtons) {
            clickOn(subjectButton);
            for(PresentationPanel temp : previewPanels) {
                if(temp.isHidden() && temp.getPresentationSubject().equals(subjectButton.getText()))
                    subjectFails[subjectButtons.indexOf(subjectButton)] = true;
                else if(!temp.isHidden() && !temp.getPresentationSubject().equals(subjectButton.getText()))
                    subjectFails[subjectButtons.indexOf(subjectButton)] = true;
            }
        }

        assertFalse("Show All filter failed", showAllFailed);

        for(int i=0; i<subjectFails.length; i++) {
            assertFalse("Subject " + i + " (" + subjectButtons.get(i).getText() + ") filter failed", subjectFails[i]);
        }
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
