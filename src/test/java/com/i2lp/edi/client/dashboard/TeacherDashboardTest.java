package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.utilities.DashModuleSortKey;
import com.i2lp.edi.client.utilities.PresSortKey;
import com.i2lp.edi.client.utilities.SubjectSortKey;
import com.i2lp.edi.server.packets.User;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.junit.*;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
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
    private Button showAllButton;
    private ArrayList<Button> subjectButtons;
    private Button loadPresButton;
    private FileChooser fileChooser;
    private MenuBar menuBar;
    private ComboBox<DashModuleSortKey> moduleSortCombo;
    private ComboBox<SubjectSortKey> subjectSortCombo;
    private ComboBox<PresSortKey> presSortCombo;
    private ArrayList<PresentationPanel> presentationPanels;
    private ArrayList<ModulePanel> modulePanels;
    private ArrayList<SubjectPanel> subjectPanels;

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
        myDashboard = (TeacherDashboard) ediManager.getDashboard();
    }

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);

        searchField = myDashboard.searchField;
        showAllButton = myDashboard.selectAllButton;
        subjectButtons = myDashboard.subjectButtons;
        loadPresButton = myDashboard.loadPresButton;
        fileChooser = myDashboard.fileChooser;
        menuBar = myDashboard.menuBar;

        subjectSortCombo = myDashboard.subjectSortCombo;
        moduleSortCombo = myDashboard.moduleSortCombo;
        presSortCombo = myDashboard.presSortCombo;

        subjectPanels = myDashboard.subjectPanels;
        presentationPanels = myDashboard.presentationPanels;
    }

    @Test
    public void testSearch() {
        Boolean presSearchFailed = false;
        Boolean moduleSearchFailed = false;
        String searchText = "Subject 1";

        clickOn(searchField).write("Subject 1");
        for(PresentationPanel temp : presentationPanels) {
            if(temp.isHidden()) {
                for (String term : temp.getSearchableTerms()) {
                    if(term == null) term = "";
                    if (term.contains(searchText))
                        presSearchFailed = true;
                }
            }
            else if(!temp.isHidden()) {
                presSearchFailed = true;
                for (String term : temp.getSearchableTerms()) {
                    if(term == null) term = "";
                    if (term.contains(searchText))
                        presSearchFailed = false;
                }
            }
        }

        for(ModulePanel temp : modulePanels) {
            if(temp.isHidden()) {
                for (String term : temp.getSearchableTerms()) {
                    if(term == null) term = "";
                    if (term.contains(searchText))
                        presSearchFailed = true;
                }
            }
            else if(!temp.isHidden()) {
                presSearchFailed = true;
                for (String term : temp.getSearchableTerms()) {
                    if(term == null) term = "";
                    if (term.contains(searchText))
                        presSearchFailed = false;
                }
            }
        }

        assertFalse("Presentation search for " + searchText + " failed", presSearchFailed);
        assertFalse("DashModule search for " + searchText + " failed", moduleSearchFailed);
    }

    @Test
    public void testFilter() {
        Boolean showAllFailed = false;
        Boolean[] subjectFails = new Boolean[subjectButtons.size()];

        for(int i=0; i<subjectFails.length; i++) {
            subjectFails[i] = false;
        }

        clickOn(showAllButton);
        for(SubjectPanel temp : subjectPanels) {
            if(temp.isHidden())
                showAllFailed = true;
        }

        for(Button subjectButton : subjectButtons) {
            clickOn(subjectButton);
            for(SubjectPanel temp : subjectPanels) {
                if(temp.isHidden() && temp.getSubject().getSubjectName().equals(subjectButton.getText()))
                    subjectFails[subjectButtons.indexOf(subjectButton)] = true;
                else if(!temp.isHidden() && !temp.getSubject().getSubjectName().equals(subjectButton.getText()))
                    subjectFails[subjectButtons.indexOf(subjectButton)] = true;
            }
        }

        assertFalse("Show All filter failed", showAllFailed);

        for(int i=0; i<subjectFails.length; i++) {
            assertFalse("Subject " + i + " (" + subjectButtons.get(i).getText() + ") filter failed", subjectFails[i]);
        }
    }

    @Test
    public void testSortSubjects() {
        String previousName = null, currentName;
        int previousModuleNum = 0, currentModuleNum = 0;
        int previousPresNum = 0, currentPresNum = 0;

        Boolean azNameFailed = false, zaNameFailed = false;
        Boolean moduleNumAscFailed = false, moduleNumDescFailed = false;
        Boolean presNumAscFailed = false, presNumDescFailed = false;
        Boolean ignore = true;

        clickOn(subjectSortCombo);
        for(int i = 0; i < 6; i++) {
            for (SubjectPanel temp : subjectPanels) {
                currentModuleNum = temp.getSubject().getModules().size();
                currentPresNum = temp.getSubject().getNumberOfPresentations();
                currentName = temp.getSubject().getSubjectName();
                if (currentName == null) currentName = "";
                if (previousName == null) previousName = "";

                switch(i) {
                    case 0:
                        if(currentName.compareTo(previousName) < 0 && !ignore)
                            azNameFailed = true;
                        break;
                    case 1:
                        if (currentName.compareTo(previousName) > 0 && !ignore)
                            zaNameFailed = true;
                        break;
                    case 2:
                        if (currentModuleNum < previousModuleNum && !ignore)
                            moduleNumAscFailed = true;
                        break;
                    case 3:
                        if (currentModuleNum > previousModuleNum && !ignore)
                            moduleNumDescFailed = true;
                        break;
                    case 4:
                        if (currentPresNum < previousPresNum && !ignore)
                            presNumAscFailed = true;
                        break;
                    case 6:
                        if (currentPresNum > previousPresNum && !ignore)
                            presNumDescFailed = true;
                        break;
                    default:
                        break;
                }
                previousName = currentName;
                previousModuleNum = currentModuleNum;
                previousPresNum = currentPresNum;
                ignore = false;
            }
            push(KeyCode.DOWN);
            ignore = true;
        }
        assertFalse("Name A-Z sorting failed", azNameFailed);
        assertFalse("Name Z-A sorting failed", zaNameFailed);
        assertFalse("No of modules ascending sorting failed", moduleNumAscFailed);
        assertFalse("No of modules ascending sorting failed", moduleNumDescFailed);
        assertFalse("No of presentations ascending sorting failed", presNumAscFailed);
        assertFalse("No of presentations ascending sorting failed", presNumDescFailed);
    }

    @Test
    public void testSortModules() {
        String previousName = null, currentName;
        String previousSubject = null, currentSubject;
        int previousPresNum = 0, currentPresNum = 0;

        Boolean azNameFailed = false, zaNameFailed = false;
        Boolean azSubjectFailed = false, zaSubjectFailed = false;
        Boolean presNumAscFailed = false, presNumDescFailed = false;
        Boolean ignore = true;

        clickOn(moduleSortCombo);
        for(int i = 0; i < 6; i++) {
            for (ModulePanel temp : modulePanels) {
                currentPresNum = temp.getModule().getPresentations().size();
                currentName = temp.getModuleName();
                if (currentName == null) currentName = "";
                if (previousName == null) previousName = "";
                currentSubject = temp.getModule().getSubject().getSubjectName();
                if (currentSubject == null) currentSubject = "";
                if (previousSubject == null) previousSubject = "";

                switch(i) {
                    case 0:
                        if(currentName.compareTo(previousName) < 0 && !ignore)
                            azNameFailed = true;
                        break;
                    case 1:
                        if (currentName.compareTo(previousName) > 0 && !ignore)
                            zaNameFailed = true;
                        break;
                    case 2:
                        if (currentSubject.compareTo(previousSubject) < 0 && !ignore)
                            azSubjectFailed = true;
                        break;
                    case 3:
                        if (currentSubject.compareTo(previousSubject) > 0 && !ignore)
                            zaSubjectFailed = true;
                        break;
                    case 4:
                        if (currentPresNum < previousPresNum && !ignore)
                            presNumAscFailed = true;
                        break;
                    case 6:
                        if (currentPresNum > previousPresNum && !ignore)
                            presNumDescFailed = true;
                        break;
                    default:
                        break;
                }
                previousName = currentName;
                previousSubject = currentSubject;
                previousPresNum = currentPresNum;
                ignore = false;
            }
            push(KeyCode.DOWN);
            ignore = true;
        }
        assertFalse("Name A-Z sorting failed", azNameFailed);
        assertFalse("Name Z-A sorting failed", zaNameFailed);
        assertFalse("Subject A-Z sorting failed", azSubjectFailed);
        assertFalse("Subject Z-A sorting failed", zaSubjectFailed);
        assertFalse("No of presentations ascending sorting failed", presNumAscFailed);
        assertFalse("No of presentations ascending sorting failed", presNumDescFailed);
    }

    @Test
    public void testSortPresentations() {
        String previousName = null, currentName;
        String previousSubject = null, currentSubject;
        String previousAuthor = null, currentAuthor;

        Boolean azNameFailed = false, zaNameFailed = false;
        Boolean azSubjectFailed = false, zaSubjectFailed = false;
        Boolean azAuthorFailed = false, zaAuthorFailed = false;
        Boolean ignore = true;

        doubleClickOn(modulePanels.get(0));
        clickOn(presSortCombo);
        for(int i = 0; i < 6; i++) {
            for (PresentationPanel temp : presentationPanels) {
                currentName = temp.getPresentation().getDocumentTitle();
                if (currentName == null) currentName = "";
                if (previousName == null) previousName = "";
                currentSubject = temp.getPresentation().getSubject().getSubjectName();
                if (currentSubject == null) currentSubject = "";
                if (previousSubject == null) previousSubject = "";
                currentAuthor = temp.getPresentation().getAuthor();
                if (currentAuthor == null) currentAuthor = "";
                if (previousAuthor == null) previousAuthor = "";

                switch(i) {
                    case 0:
                        if(currentName.compareTo(previousName) < 0 && !ignore)
                            azNameFailed = true;
                        break;
                    case 1:
                        if (currentName.compareTo(previousName) > 0 && !ignore)
                            zaNameFailed = true;
                        break;
                    case 2:
                        if (currentSubject.compareTo(previousSubject) < 0 && !ignore)
                            azSubjectFailed = true;
                        break;
                    case 3:
                        if (currentSubject.compareTo(previousSubject) > 0 && !ignore)
                            zaSubjectFailed = true;
                        break;
                    case 4:
                        if (currentAuthor.compareTo(previousAuthor) < 0 && !ignore)
                            azAuthorFailed = true;
                        break;
                    case 6:
                        if (currentAuthor.compareTo(previousAuthor) > 0 && !ignore)
                            zaAuthorFailed = true;
                        break;
                    default:
                        break;
                }
                previousName = currentName;
                previousSubject = currentSubject;
                previousAuthor = currentAuthor;
                ignore = false;
            }
            push(KeyCode.DOWN);
            ignore = true;
        }
        assertFalse("Name A-Z sorting failed", azNameFailed);
        assertFalse("Name Z-A sorting failed", zaNameFailed);
        assertFalse("Subject A-Z sorting failed", azSubjectFailed);
        assertFalse("Subject Z-A sorting failed", zaSubjectFailed);
        assertFalse("Author A-Z sorting failed", azAuthorFailed);
        assertFalse("Author Z-A sorting failed", zaAuthorFailed);
    }

    @Ignore //TODO @Luke Redo
    @Test
    public void testAddPresentation() {
        clickOn(loadPresButton);
        push(KeyCode.I);
        push(KeyCode.DOWN);
        push(KeyCode.ENTER);

        File file = new File("projectResources/sampleFiles/xml/i2lpSampleXml.xml");

        //System.out.print(myDashboard.presentationManager.getXmlPath());
        //assertEquals(file.getAbsolutePath(), myDashboard.presentationManager.getXmlPath());
    }

    @Ignore
    @Test
    public void testMenuBar() {
        clickOn(menuBar);
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
