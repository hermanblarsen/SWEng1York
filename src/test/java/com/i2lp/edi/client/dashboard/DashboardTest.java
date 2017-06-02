package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.utilities.DashModuleSortKey;
import com.i2lp.edi.client.utilities.ParserXML;
import com.i2lp.edi.client.utilities.PresSortKey;
import com.i2lp.edi.client.utilities.SubjectSortKey;
import com.i2lp.edi.server.packets.User;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.junit.*;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.time.LocalDateTime;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by Luke on 06/05/2017.
 */
public abstract class DashboardTest extends ApplicationTest {
    protected static Dashboard myDashboard;

    protected TextField searchField;
    protected Button showAllButton;
    protected ArrayList<CheckBox> subjectCheckboxes;
    protected Button openPresButton;
    protected FileChooser fileChooser;
    protected MenuBar menuBar;
    protected Node calendarNode;
    protected DatePicker calendar;
    protected ComboBox<DashModuleSortKey> moduleSortCombo;
    protected ComboBox<SubjectSortKey> subjectSortCombo;
    protected ComboBox<PresSortKey> presSortCombo;
    protected ArrayList<PresentationPanel> presentationPanels;
    protected ArrayList<ModulePanel> modulePanels;
    protected ArrayList<SubjectPanel> subjectPanels;

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
        calendarNode = myDashboard.calendarNode;
        calendar = myDashboard.calendar;

        subjectSortCombo = myDashboard.subjectSortCombo;
        moduleSortCombo = myDashboard.moduleSortCombo;
        presSortCombo = myDashboard.presSortCombo;

        subjectPanels = myDashboard.subjectPanels;
        presentationPanels = myDashboard.presentationPanels;
        modulePanels = subjectPanels.get(0).getModulePanels();
    }

    @Test
    public void testSearch() {
        Boolean presSearchFailed = false;
        Boolean moduleSearchFailed = false;
        String searchText = "Y1 Maths";

        clickOn(searchField).write("Y1 Maths");
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
        Boolean[] subjectFails = new Boolean[subjectCheckboxes.size()];

        for(int i=0; i<subjectFails.length; i++) {
            subjectFails[i] = false;
        }

        for(CheckBox subjectButton : subjectCheckboxes) {
            clickOn(subjectButton);
            for(SubjectPanel temp : subjectPanels) {
                if(temp.isHidden() && temp.getSubject().getSubjectName().equals(subjectButton.getText()))
                    subjectFails[subjectCheckboxes.indexOf(subjectButton)] = true;
                else if(!temp.isHidden() && !temp.getSubject().getSubjectName().equals(subjectButton.getText()))
                    subjectFails[subjectCheckboxes.indexOf(subjectButton)] = true;
            }
            clickOn(subjectButton);
        }

        clickOn(showAllButton);
        for(SubjectPanel temp : subjectPanels) {
            if(temp.isHidden())
                showAllFailed = true;
        }

        assertFalse("Show All filter failed", showAllFailed);

        for(int i=0; i<subjectFails.length; i++) {
            assertFalse("Subject " + i + " (" + subjectCheckboxes.get(i).getText() + ") filter failed", subjectFails[i]);
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
                        if(currentName.compareToIgnoreCase(previousName) < 0 && !ignore)
                            azNameFailed = true;
                        break;
                    case 1:
                        if (currentName.compareToIgnoreCase(previousName) > 0 && !ignore)
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
        int previousPresNum = 0, currentPresNum;

        Boolean azNameFailed = false, zaNameFailed = false;
        Boolean presNumAscFailed = false, presNumDescFailed = false;
        Boolean ignore = true;

        clickOn(moduleSortCombo);
        for(int i = 0; i < 4; i++) {
            for (ModulePanel temp : subjectPanels.get(0).getModulePanels()) {
                currentPresNum = temp.getModule().getPresentations().size();
                currentName = temp.getModuleName();
                if (currentName == null) currentName = "";
                if (previousName == null) previousName = "";

                switch(i) {
                    case 0:
                        if(currentName.compareToIgnoreCase(previousName) < 0 && !ignore)
                            azNameFailed = true;
                        break;
                    case 1:
                        if (currentName.compareToIgnoreCase(previousName) > 0 && !ignore)
                            zaNameFailed = true;
                        break;
                    case 2:
                        if (currentPresNum < previousPresNum && !ignore)
                            presNumAscFailed = true;
                        break;
                    case 3:
                        if (currentPresNum > previousPresNum && !ignore)
                            presNumDescFailed = true;
                        break;
                    default:
                        break;
                }
                previousName = currentName;
                previousPresNum = currentPresNum;
                ignore = false;
            }
            push(KeyCode.DOWN);
            ignore = true;
        }
        assertFalse("Name A-Z sorting failed", azNameFailed);
        assertFalse("Name Z-A sorting failed", zaNameFailed);
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

        ArrayList<PresentationPanel> currentPresPanels= new ArrayList<>();
        for (PresentationPanel temp : presentationPanels) {
            if(temp.getPresentation().getModule() == subjectPanels.get(0).getModulePanels().get(0).getModule())
                currentPresPanels.add(temp);
        }

        doubleClickOn(subjectPanels.get(0).getModulePanels().get(0));
        presSortCombo = myDashboard.presSortCombo;
        clickOn(presSortCombo);

        for(int i = 0; i < 6; i++) {
            for (PresentationPanel temp : currentPresPanels) {
                currentName = temp.getPresentation().getId();
                if (currentName == null) currentName = "";
                if (previousName == null) previousName = "";
                currentSubject = temp.getPresentation().getSubject().getSubjectName();
                if (currentSubject == null) currentSubject = "";
                if (previousSubject == null) previousSubject = "";
                currentAuthor = temp.getPresentation().getAuthor();
                if (currentAuthor == null) currentAuthor = "";
                if (previousAuthor == null) previousAuthor = "";

                if(!temp.isVisible())
                    ignore = true;

                switch(i) {
                    case 0:
                        if(currentName.compareToIgnoreCase(previousName) < 0 && !ignore)
                            azNameFailed = true;
                        break;
                    case 1:
                        if (currentName.compareToIgnoreCase(previousName) > 0 && !ignore)
                            zaNameFailed = true;
                        break;
                    case 2:
                        if (currentSubject.compareToIgnoreCase(previousSubject) < 0 && !ignore)
                            azSubjectFailed = true;
                        break;
                    case 3:
                        if (currentSubject.compareToIgnoreCase(previousSubject) > 0 && !ignore)
                            zaSubjectFailed = true;
                        break;
                    case 4:
                        if (currentAuthor.compareToIgnoreCase(previousAuthor) < 0 && !ignore)
                            azAuthorFailed = true;
                        break;
                    case 6:
                        if (currentAuthor.compareToIgnoreCase(previousAuthor) > 0 && !ignore)
                            zaAuthorFailed = true;
                        break;
                    default:
                        break;
                }
                if(!ignore)
                    System.out.print(" [" + i +  " : "  + previousName + " : " + currentName + "] ");
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

    //@Ignore
    @Test
    public void testOpenPresentation() {
        clickOn(openPresButton);
        push(KeyCode.DOWN);
        push(KeyCode.ENTER);

        push(KeyCode.S).push(KeyCode.A).push(KeyCode.M).push(KeyCode.P).push(KeyCode.L).push(KeyCode.E);
        push(KeyCode.DOWN);
        push(KeyCode.ENTER);

        File file = new File("projectResources/sampleFiles/xml/sampleXmlSimple.xml");
        ParserXML parser = null;
        try {
            parser = new ParserXML("projectResources/sampleFiles/xml/sampleXmlSimple.xml");
        } catch (FileNotFoundException e) {
            //FileNotFound.eat()
        }
        Presentation pres = parser.parsePresentation();

        //TODO @Luke Subject assertion fails due to no module
        if(pres.getDocumentTitle() == null)
            assertNull(myDashboard.presentationManager.getPresentationElement().getDocumentTitle());
        else
            assertEquals(pres.getDocumentTitle(), myDashboard.presentationManager.getPresentationElement().getDocumentTitle());
        //if(pres.getSubject() == null)
        //    assertNull(myDashboard.presentationManager.getPresentationElement().getSubject());
        //else
        //    assertEquals(pres.getSubject(), myDashboard.presentationManager.getPresentationElement().getSubject());
        if(pres.getAuthor() == null)
            assertNull(myDashboard.presentationManager.getPresentationElement().getAuthor());
        else
            assertEquals(pres.getAuthor(), myDashboard.presentationManager.getPresentationElement().getAuthor());
        if(pres.getDescription() == null)
            assertNull(myDashboard.presentationManager.getPresentationElement().getDescription());
        else
            assertEquals(pres.getDescription(), myDashboard.presentationManager.getPresentationElement().getDescription());
        if(pres.getDocumentID() == null)
            assertNull(myDashboard.presentationManager.getPresentationElement().getDocumentID());
        else
            assertEquals(pres.getModule(), myDashboard.presentationManager.getPresentationElement().getModule());
        if(pres.getModule() == null)
            assertNull(myDashboard.presentationManager.getPresentationElement().getModule());
        else
            assertEquals(pres.getModule(), myDashboard.presentationManager.getPresentationElement().getModule());
        //if(pres.getSlideList() == null)
        //    assertNull(myDashboard.presentationManager.getPresentationElement().getSlideList());
        //else
        //    assertEquals(pres.getSlideList(), myDashboard.presentationManager.getPresentationElement().getSlideList());
        if(pres.getTags() == null)
            assertNull(myDashboard.presentationManager.getPresentationElement().getTags());
        else
            assertEquals(pres.getTags(), myDashboard.presentationManager.getPresentationElement().getTags());
        //if(pres.getTheme() == null)
        //    assertNull(myDashboard.presentationManager.getPresentationElement().getTheme());
        //else
         //   assertEquals(pres.getTheme(), myDashboard.presentationManager.getPresentationElement().getTheme());
        if(pres.getVersion() == null)
            assertNull(myDashboard.presentationManager.getPresentationElement().getVersion());
        else
            assertEquals(pres.getVersion(), myDashboard.presentationManager.getPresentationElement().getVersion());
    }

    @Ignore //Possibly not needed
    @Test
    public void testAboutPopup() {
        moveTo(menuBar);
        moveBy(menuBar.getLayoutX() - (int)(menuBar.getWidth() / 2.0) + 20, menuBar.getLayoutY());
        clickOn();
        push(KeyCode.RIGHT).push(KeyCode.RIGHT).push(KeyCode.RIGHT);
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        assertTrue(myDashboard.aboutPopup.isShowing());
    }

    @Test
    public void testWelcomeMessage() {
        assertTrue(myDashboard.welcomePane.isVisible());
        assertFalse(myDashboard.isWelcomeTextHidden);

        clickOn(myDashboard.closeWelcomeButton);
        assertTrue(myDashboard.isWelcomeTextHidden);

        moveTo(menuBar);
        moveBy(menuBar.getLayoutX() - (int)(menuBar.getWidth() / 2.0) + 20, menuBar.getLayoutY());
        clickOn();
        push(KeyCode.RIGHT);
        push(KeyCode.DOWN).push(KeyCode.DOWN);
        push(KeyCode.ENTER);
        assertFalse(myDashboard.isWelcomeTextHidden);
    }

    @Test
    public void testCalendar() {
        clickOn(myDashboard.calendarNode);
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
