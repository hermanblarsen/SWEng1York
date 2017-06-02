package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.server.packets.User;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDateTime;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by Luke on 30/05/2017.
 */
public class TeacherDashboardTest extends DashboardTest{

    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

        EdiManager ediManager = new EdiManager();
        ediManager.start(stage);
        ediManager.loginSucceeded(true, new User(1, "First", "Last", "email", "teacher"));
        stage.toFront();
        myDashboard = ediManager.getDashboard();
    }

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);

        searchField = myDashboard.searchField;
        showAllButton = myDashboard.selectAllButton;
        subjectCheckboxes = myDashboard.subjectCheckboxes;
        openPresButton = myDashboard.openPresButton;
        fileChooser = myDashboard.fileChooser;
        menuBar = myDashboard.menuBar;

        subjectSortCombo = myDashboard.subjectSortCombo;
        moduleSortCombo = myDashboard.moduleSortCombo;
        presSortCombo = myDashboard.presSortCombo;

        subjectPanels = myDashboard.subjectPanels;
        presentationPanels = myDashboard.presentationPanels;
        modulePanels = subjectPanels.get(0).getModulePanels();
    }

    //@Ignore //TODO @Luke Dependent on first module containing at least one presentation
    @Test
    public void testSchedulePresentation() {
        doubleClickOn(subjectPanels.get(0).getModulePanels().get(0));

        PresentationPanel presPanel = presentationPanels.get(0);
        for(PresentationPanel temp : presentationPanels) {
            if(!temp.isHidden())
                presPanel = temp;
        }

        //TODO @Luke Fix so this is not needed
        Assume.assumeFalse(presPanel.isHidden());

        rightClickOn(presPanel);
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN);
        push(KeyCode.ENTER);

        calendar = myDashboard.calendar;
        assertTrue(calendar.isVisible());

        push(KeyCode.CONTROL, KeyCode.A);
        write("01/01/2018");
        push(KeyCode.TAB);
        type(KeyCode.UP, 24);
        push(KeyCode.TAB);
        type(KeyCode.UP, 12);
        push(KeyCode.TAB);
        clickOn(myDashboard.dateTimePicker.scheduleButton);

        assertEquals(LocalDateTime.of(2018,1,1,0,0), presPanel.getPresentation().getGoLiveDateTime());
    }
}
