package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.dashboard.TeacherDashboard;
import com.i2lp.edi.client.login.Login;
import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.managers.PresentationManager;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.server.packets.User;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import static org.junit.Assert.*;

/**
 * Created by Luke on 06/05/2017.
 */
public class TeacherDashboardTest extends ApplicationTest {
    private static TeacherDashboard myDashboard;

    private TextField searchField;
    private ArrayList<PresentationPreviewPanel> previewPanels;
    private Button showAllButton;
    private Button subjectButton0;
    private Button subjectButton1;
    private Button subjectButton2;

    @Override
    public void start(Stage stage) throws Exception {
        EdiManager ediManager = new EdiManager();
        ediManager.start(stage);
        ediManager.loginSucceded(true, new User(1, "First", "Last", "email", "teacher"));
        stage.toFront();
        myDashboard = (TeacherDashboard) ediManager.getDashboard();
    }

    @Before
    public void setUp() {
        searchField = myDashboard.searchField;
        previewPanels = myDashboard.previewPanels;
        showAllButton = myDashboard.showAllButton;
        subjectButton0 = myDashboard.subjectButton0;
        subjectButton1 = myDashboard.subjectButton1;
        subjectButton2 = myDashboard.subjectButton2;
    }

    @Test
    public void testSearch() {
        clickOn(searchField).write("test");
    }

    @Test
    public void testFilter() {
        Boolean showAllFailed = false, subject0Failed = false, subject1Failed = false , subject2Failed = false;

        clickOn(showAllButton);
        for(PresentationPreviewPanel temp : previewPanels) {
            if(temp.isHidden())
                showAllFailed = true;
        }

        clickOn(subjectButton0);
        for(PresentationPreviewPanel temp : previewPanels) {
            if(temp.isHidden() && temp.getPresentationSubject().equals(subjectButton0.getText()))
                    subject0Failed = true;
            else if(!temp.isHidden() && !temp.getPresentationSubject().equals(subjectButton0.getText()))
                    subject0Failed = true;
        }

        clickOn(subjectButton1);
        for(PresentationPreviewPanel temp : previewPanels) {
            if(temp.isHidden() && temp.getPresentationSubject().equals(subjectButton1.getText()))
                subject1Failed = true;
            else if(!temp.isHidden() && !temp.getPresentationSubject().equals(subjectButton1.getText()))
                subject1Failed = true;
        }

        clickOn(subjectButton2);
        for(PresentationPreviewPanel temp : previewPanels) {
            if(temp.isHidden() && temp.getPresentationSubject().equals(subjectButton2.getText()))
                subject2Failed = true;
            else if(!temp.isHidden() && !temp.getPresentationSubject().equals(subjectButton2.getText()))
                subject2Failed = true;
        }

        assertFalse("Show All filter failed", showAllFailed);
        assertFalse("Subject 0 filter failed", subject0Failed);
        assertFalse("Subject 1 filter failed", subject1Failed);
        assertFalse("Subject 2 filter failed", subject2Failed);
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
