package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.Constants;
import com.i2lp.edi.client.editor.PresentationEditor;
import com.i2lp.edi.client.managers.*;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.utilities.DashModuleSortKey;
import com.i2lp.edi.client.utilities.ParserXML;
import com.i2lp.edi.client.utilities.PresSortKey;
import com.i2lp.edi.client.utilities.SubjectSortKey;
import com.i2lp.edi.server.packets.Module;
import com.i2lp.edi.server.packets.PresentationMetadata;
import com.sun.javafx.scene.control.skin.DatePickerSkin;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.validator.UrlValidator;
import org.controlsfx.control.textfield.CustomTextField;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

import static com.i2lp.edi.client.Constants.PRESENTATIONS_PATH;
import static javafx.scene.layout.BorderPane.setAlignment;


/**
 * Created by amriksadhra on 24/01/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused", "Duplicates"})
public abstract class Dashboard extends Application {
    protected static final double RIGHT_PANEL_WIDTH = 210;
    protected static final double LEFT_PANEL_WIDTH = 200;
    private static final String OPEN_LOCAL_PRES_CAPTION = "Local (from this computer)";
    private static final String OPEN_REMOTE_PRES_CAPTION = "Remote (from this http)";
    protected static Logger logger = LoggerFactory.getLogger(Dashboard.class);
    private EdiManager ediManager;
    protected PresentationManager presentationManager;
    protected Scene scene;
    protected BorderPane border;
    protected Stage dashboardStage;
    private PresentationPanel selectedPresPanel;
    private ModulePanel selectedModulePanel;
    protected ArrayList<PresentationPanel> presentationPanels;
    protected ArrayList<SubjectPanel> subjectPanels;
    private FlowPane presentationPanelsFlowPane;
    private VBox subjectPanelsVBox;
    protected ComboBox<PresSortKey> presSortCombo;
    protected ComboBox<DashModuleSortKey> moduleSortCombo;
    protected ComboBox<SubjectSortKey> subjectSortCombo;
    private Stage addToServerStage;
    private ArrayList<Presentation> availablePresentations;
    private ArrayList<DashModule> availableModules;
    private ArrayList<Subject> availableSubjects;
    protected boolean isWelcomeTextHidden = false;
    private DashboardState currentState;
    private Text noMatchesSubject, noMatchesPres;
    private VBox controlsVBox;
    private ScrollPane leftPanelScroll;
    private Panel subjectFilterPanel, presSortPanel, moduleSortPanel, subjectSortPanel;
    private DashModule selectedModule;
    private VBox rightPanelVBox;
    private ScrollPane rightPanelScroll;
    private LocalDate selectedDate;
    private ScrollPane presentationsScrollPane;
    private ScrollPane subjectsScrollPane;
    private VBox centerVBox;
    private VBox subjectCheckboxesVBox;

    protected CustomTextField searchField;
    protected Button showAllButton;
    protected ArrayList<CheckBox> subjectCheckboxes;
    protected ArrayList<String> filterSubjects;
    private ArrayList<PresSchedulePanel> schedulePanels;
    private VBox scheduleVBox;
    protected Button openPresButton;
    protected FileChooser fileChooser;
    protected MenuBar menuBar;
    protected Popup aboutPopup;
    protected Node calendarNode;
    protected DatePicker calendar;
    protected DateTimePicker dateTimePicker;
    protected StackPane welcomePane;
    protected Button closeWelcomeButton;

    @Override
    public void start(Stage dashboardStage) {
        this.dashboardStage = dashboardStage;
        //Initialise UI
        dashboardStage.setTitle("Edi");
        Image ediLogoSmall = new Image("file:projectResources/logos/ediLogo32x32.png");
        dashboardStage.getIcons().add(ediLogoSmall);
        dashboardStage.setOnCloseRequest(event -> ediManager.stop());
        dashboardStage.setMinWidth(800);
        dashboardStage.setMinHeight(600);

        border = new BorderPane();
        VBox rootBox = new VBox(addMenuBar(), border);
        scene = new Scene(rootBox, 1000, 600);
        noMatchesSubject = new Text("No matches found"); //TODO: move setup of global nodes to separate method
        noMatchesSubject.setFill(Color.GRAY);
        noMatchesSubject.getStyleClass().add("italic");
        noMatchesPres = new Text("No matches found");
        noMatchesPres.setFill(Color.GRAY);
        noMatchesPres.getStyleClass().add("italic");
        selectedDate = LocalDate.now();

        scene.getStylesheets().add("bootstrapfx.css");
        dashboardStage.setScene(scene);

        updateAvailablePresentations();

        ediManager.getPresentationLibraryManager().getUserModuleList();

        goToState(DashboardState.TOP_LEVEL);
        setupBorderTop();

        dashboardStage.show();
    }

    public void goToState(DashboardState state) {
        currentState = state;
        logger.info("State: " + currentState.name());

//        if (searchField != null) {
//            if (state == DashboardState.MODULE || state == DashboardState.TOP_LEVEL)
//                searchField.setText("");
//        }

        if (state == DashboardState.TOP_LEVEL) {
            setSelectedPreviewPanel(selectedPresPanel, false);
        }

        displayBorderCenter(state);
        displayBorderLeft(state); //This has to be called after displayBorderCenter()
        displayBorderRight(state);
    }

    private void setupBorderTop() {
        HBox topPanel = new HBox(10);
        topPanel.setPadding(new Insets(0, 12, 0, 12));
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setStyle("-fx-background-color: #34495e;");
        topPanel.setPrefHeight(20);
        topPanel.setMaxHeight(HBox.USE_PREF_SIZE);
        topPanel.setMaxHeight(HBox.USE_PREF_SIZE);

        ImageView uoyLogo = new ImageView(new Image("file:projectResources/icons/UOY-Logo-Long-White.png", 300, Double.MAX_VALUE, true, true));

        HBox logoHBox = new HBox();
        logoHBox.setAlignment(Pos.CENTER);
        logoHBox.getChildren().add(uoyLogo);

        fileChooser = new FileChooser();
        FileChooser.ExtensionFilter xmlExtensionFilter =
                new FileChooser.ExtensionFilter("XML Presentations (*.XML)", "*.xml", "*.XML");
        fileChooser.getExtensionFilters().add(xmlExtensionFilter);
        fileChooser.setSelectedExtensionFilter(xmlExtensionFilter);
        fileChooser.setInitialDirectory(new File("projectResources/sampleFiles/xml"));
        //fileChooser.setInitialDirectory(new File(BASE_PATH));
        fileChooser.setTitle("Open Presentation");

        openPresButton = new Button("Open Presentation", new ImageView(new Image("file:projectResources/icons/arrow-down.png", 10, 10, true, true)));
        openPresButton.getStyleClass().setAll("btn", "btn-default");
        openPresButton.setOnAction(event -> {
            ContextMenu menu = new ContextMenu();

            MenuItem local = new MenuItem(OPEN_LOCAL_PRES_CAPTION);
            local.setOnAction(event1 -> showOpenLocalPres());

            MenuItem online = new MenuItem(OPEN_REMOTE_PRES_CAPTION);
            online.setOnAction(event1 -> {
                //For testing you can use :https://raw.githubusercontent.com/hermanblarsen/SWEng1York/master/projectResources/sampleFiles/xml/i2lpSampleXml.xml?token=AYLAhZswVfz-zJFrEDKoquw1Eg0XybCKks5ZNcqAwA%3D%3D
                showOpenRemotePres("");
            });

            menu.getItems().addAll(local, online);
            menu.show(openPresButton, Side.BOTTOM, 0, 0);
        });
        topPanel.getChildren().add(openPresButton);

        topPanel.getChildren().add(logoHBox);
        HBox.setHgrow(logoHBox, Priority.ALWAYS);

        searchField = new CustomTextField();
        searchField.setRight(new ImageView("file:projectResources/icons/searchIcon.png"));
        searchField.textProperty().addListener(observable -> search(searchField.getText()));
        topPanel.getChildren().add(searchField);

        border.setTop(topPanel);
    }

    private void displayBorderCenter(DashboardState state) {
        if (centerVBox == null) {
            centerVBox = new VBox(5);
            centerVBox.setPadding(new Insets(10));
            centerVBox.setAlignment(Pos.TOP_CENTER);
            centerVBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                switch (currentState) {
                    case TOP_LEVEL:
                        setSelectedPreviewPanel(selectedModulePanel, false);
                        break;
                    case MODULE:
                    case SEARCH_IN_MODULE:
                        setSelectedPreviewPanel(selectedPresPanel, false);
                        break;
                    case SEARCH_ALL:
                        setSelectedPreviewPanel(selectedPresPanel, false);
                        setSelectedPreviewPanel(selectedModulePanel, false);
                        break;
                    default:
                        //do nothing
                }
            });
        } else {
            centerVBox.getChildren().clear();
        }

        subjectsScrollPane = new ScrollPane();
        subjectsScrollPane.setContent(subjectPanelsVBox);
        subjectsScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        subjectsScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        subjectsScrollPane.setFitToWidth(true);
        subjectsScrollPane.getStyleClass().add("edge-to-edge");

        if (presentationsScrollPane == null) {
            presentationsScrollPane = new ScrollPane();
            presentationsScrollPane.setContent(presentationPanelsFlowPane);
            presentationsScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
            presentationsScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
            presentationsScrollPane.setFitToWidth(true);
            presentationsScrollPane.getStyleClass().add("edge-to-edge");
        }

        switch (state) {
            case TOP_LEVEL:
                if (!isWelcomeTextHidden) {
                    welcomePane = new StackPane();

                    VBox textVBox = new VBox();
                    textVBox.setAlignment(Pos.TOP_CENTER);
                    Panel textPanel = new Panel();
                    textPanel.getStyleClass().add("panel-primary");
                    textPanel.setBody(textVBox);

                    closeWelcomeButton = new Button("X");
                    closeWelcomeButton.getStyleClass().setAll("btn", "btn-default");
                    closeWelcomeButton.setOnAction(event -> {
                        isWelcomeTextHidden = true;
                        centerVBox.getChildren().remove(welcomePane);
                    });
                    closeWelcomeButton.setStyle("-fx-font-size: 10px; -fx-min-width: 0; -fx-padding: 6px 6px 6px 6px;");

                    StackPane.setAlignment(closeWelcomeButton, Pos.TOP_RIGHT);
                    StackPane.setMargin(closeWelcomeButton, new Insets(5));

                    welcomePane.getChildren().addAll(textPanel, closeWelcomeButton);

                    Text welcomeHeader = new Text("Welcome to Edi");
                    welcomeHeader.getStyleClass().setAll("h3");
                    textVBox.getChildren().add(welcomeHeader);
                    VBox.setMargin(welcomeHeader, new Insets(5, 0, 5, 0));

                    Text welcomeText = new Text("Hello, " + ediManager.getUserData().getFirstName() +
                            "! You have " + getNumOfScheduledPresOnDate(LocalDate.now()) +
                            " presentations on schedule today.");
                    textVBox.getChildren().add(welcomeText);
                    VBox.setMargin(welcomeText, new Insets(5, 0, 5, 0));

                    centerVBox.getChildren().add(welcomePane);
                }

                centerVBox.getChildren().add(subjectsScrollPane);
                border.setCenter(centerVBox);

                filterBy(filterSubjects);
                break;

            case SEARCH_ALL:
                Text modulesText = new Text("Subjects & Modules");
                modulesText.getStyleClass().setAll("h4");

                Text presentationsText = new Text("Presentations");
                presentationsText.getStyleClass().setAll("h4");
                VBox.setMargin(presentationsText, new Insets(10, 0, 0, 0));

                centerVBox.getChildren().addAll(modulesText, subjectPanelsVBox, presentationsText, presentationPanelsFlowPane);

                ScrollPane searchScrollPane = new ScrollPane();
                searchScrollPane.setContent(centerVBox);
                searchScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
                searchScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
                searchScrollPane.setFitToWidth(true);
                searchScrollPane.getStyleClass().add("edge-to-edge");

                border.setCenter(searchScrollPane);

                filterBy(filterSubjects);
                break;

            case MODULE:
            case SEARCH_IN_MODULE:
                Button backButton = new Button("Back");
                backButton.getStyleClass().setAll("btn", "btn-default");
                backButton.setAlignment(Pos.CENTER);
                backButton.setOnAction(event -> goToState(DashboardState.TOP_LEVEL));

                Region spacer1 = new Region();
                HBox.setHgrow(spacer1, Priority.ALWAYS);

                Text subjectText = new Text(selectedModule.getSubject().getSubjectName());
                subjectText.getStyleClass().setAll("h4");

                ImageView arrow = new ImageView("file:projectResources/icons/dashboard-arrow-right.png");
                HBox.setMargin(arrow, new Insets(0, 5, 0, 5));

                Text moduleText = new Text(selectedModule.getModuleName());
                moduleText.getStyleClass().setAll("h4");

                Region spacer2 = new Region();
                HBox.setHgrow(spacer2, Priority.ALWAYS);

                Region dummy = new Region();
                dummy.setPrefWidth(80);
                //Platform.runLater(() -> dummy.setPrefWidth(backButton.getWidth()));

                HBox moduleNameHBox = new HBox(5);
                moduleNameHBox.setAlignment(Pos.CENTER);
                moduleNameHBox.setPadding(new Insets(5, 0, 5, 0));
                moduleNameHBox.getChildren().addAll(backButton, spacer1, subjectText, arrow, moduleText, spacer2, dummy);

                centerVBox.getChildren().addAll(moduleNameHBox, presentationsScrollPane);
                border.setCenter(centerVBox);

                filterBy(selectedModule);
                break;

            default:
                //Do nothing
        }
    }

    private void displayBorderLeft(DashboardState state) {
        if (controlsVBox == null) {
            controlsVBox = new VBox(8);
            controlsVBox.setPadding(new Insets(10));
        }

        if (leftPanelScroll == null) {
            leftPanelScroll = new ScrollPane();
            leftPanelScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
            leftPanelScroll.setVbarPolicy(ScrollBarPolicy.NEVER);
            leftPanelScroll.getStyleClass().add("edge-to-edge");
            leftPanelScroll.setContent(controlsVBox);
            leftPanelScroll.setFitToWidth(true);
            leftPanelScroll.setMaxWidth(LEFT_PANEL_WIDTH);
            border.setLeft(leftPanelScroll);
        }

        Insets panelInsets = new Insets(5);

        if (subjectFilterPanel == null) {
            //Setup filtering panel

            VBox subjectsVBox = new VBox(3);
            subjectsVBox.setStyle("-fx-background-color: #ffffff;");

            showAllButton = new Button("Select all");
            showAllButton.getStyleClass().setAll("btn", "btn-success");
            showAllButton.setMaxWidth(Double.MAX_VALUE);
            showAllButton.setAlignment(Pos.CENTER);
            showAllButton.setOnAction(event -> {
                boolean allSelected = true;

                for (CheckBox checkBox : subjectCheckboxes) {
                    if (!checkBox.isSelected()) {
                        allSelected = false;
                        break;
                    }
                }

                if (allSelected) {
                    for (CheckBox checkBox : subjectCheckboxes) {
                        checkBox.setSelected(false);
                    }

                    showAllButton.setText("Select all");
                } else {
                    for (CheckBox checkBox : subjectCheckboxes) {
                        checkBox.setSelected(true);
                    }

                    showAllButton.setText("Deselect all");
                }

                filterBy(null);
            });
            subjectsVBox.getChildren().add(showAllButton);

            updateSubjectCheckBoxes();

            subjectsVBox.getChildren().add(subjectCheckboxesVBox);

            subjectFilterPanel = new Panel("Filter by subject:");
            subjectFilterPanel.getStyleClass().add("panel-primary");
            subjectFilterPanel.setCenter(subjectsVBox);
            BorderPane.setMargin(subjectsVBox, panelInsets);
            VBox.setMargin(subjectFilterPanel, new Insets(0, 0, 0, 0));
        }

        if (presSortPanel == null) {
            //Setup panel for presentation sorting
            presSortPanel = new Panel("Sort presentations by");
            presSortPanel.getStyleClass().add("panel-primary");
            presSortCombo = new ComboBox<>();
            presSortCombo.setMaxWidth(Double.MAX_VALUE);
            PresSortKey.copyAllToList(presSortCombo.getItems());
            presSortCombo.setOnAction(event -> sortPresentations(presSortCombo.getValue()));
            presSortCombo.setValue(presSortCombo.getItems().get(0));
            sortPresentations(presSortCombo.getValue());
            presSortPanel.setCenter(presSortCombo);
            BorderPane.setMargin(presSortCombo, panelInsets);
        }

        if (moduleSortPanel == null) {
            //Setup panel for module sorting
            moduleSortPanel = new Panel("Sort modules by");
            moduleSortPanel.getStyleClass().add("panel-primary");
            moduleSortCombo = new ComboBox<>();
            moduleSortCombo.setMaxWidth(Double.MAX_VALUE);
            DashModuleSortKey.copyAllToList(moduleSortCombo.getItems());
            moduleSortCombo.setOnAction(event -> sortModules(moduleSortCombo.getValue()));
            moduleSortCombo.setValue(moduleSortCombo.getItems().get(0));
            sortModules(moduleSortCombo.getValue());
            moduleSortPanel.setCenter(moduleSortCombo);
            BorderPane.setMargin(moduleSortCombo, panelInsets);
        }

        if (subjectSortPanel == null) {
            //Setup panel for subject sorting
            subjectSortPanel = new Panel("Sort subjects by");
            subjectSortPanel.getStyleClass().add("panel-primary");
            subjectSortCombo = new ComboBox<>();
            subjectSortCombo.setMaxWidth(Double.MAX_VALUE);
            SubjectSortKey.copyAllToList(subjectSortCombo.getItems());
            subjectSortCombo.setOnAction(event -> sortSubjects(subjectSortCombo.getValue()));
            subjectSortCombo.setValue(subjectSortCombo.getItems().get(0));
            sortSubjects(subjectSortCombo.getValue());
            subjectSortPanel.setCenter(subjectSortCombo);
            BorderPane.setMargin(subjectSortCombo, panelInsets);
        }

        switch (state) {
            case TOP_LEVEL:
                controlsVBox.getChildren().clear();
                controlsVBox.getChildren().addAll(subjectFilterPanel, subjectSortPanel, moduleSortPanel);
                break;
            case MODULE:
            case SEARCH_IN_MODULE:
                controlsVBox.getChildren().clear();
                controlsVBox.getChildren().add(presSortPanel);
                break;
            case SEARCH_ALL:
                controlsVBox.getChildren().clear();
                controlsVBox.getChildren().addAll(subjectFilterPanel, subjectSortPanel, moduleSortPanel, presSortPanel);
                break;

            default:
                //Do nothing
        }
    }

    private void displayBorderRight(DashboardState state) {

        if (rightPanelVBox == null) {
            rightPanelVBox = new VBox(2);
            rightPanelVBox.setAlignment(Pos.TOP_CENTER);
            rightPanelVBox.setMaxWidth(RIGHT_PANEL_WIDTH + 20);
        }

        if (rightPanelScroll == null) {
            rightPanelScroll = new ScrollPane();
            rightPanelScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
            rightPanelScroll.setVbarPolicy(ScrollBarPolicy.NEVER);
            rightPanelScroll.getStyleClass().add("edge-to-edge");
            rightPanelScroll.setFitToWidth(true);
            rightPanelScroll.setPrefWidth(RIGHT_PANEL_WIDTH + 20);
            rightPanelScroll.setMaxWidth(ScrollPane.USE_PREF_SIZE);
            rightPanelScroll.setContent(rightPanelVBox);
            border.setRight(rightPanelScroll);
        }

        Panel schedulePanel = new Panel("Schedule");
        schedulePanel.getStyleClass().add("panel-primary");

        calendar = new DatePicker(selectedDate);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("E, dd.MM.YYYY");
        schedulePanel.setText("Schedule for " + selectedDate.format(dtf));
        calendar.setShowWeekNumbers(false);
        calendar.setOnAction(event -> {
            selectedDate = calendar.getValue();
            updateSchedulePanels();
            schedulePanel.setText("Schedule for " + selectedDate.format(dtf));
        });
        final Callback<DatePicker, DateCell> dayCellFactory =
                new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);

                                setPadding(new Insets(0));

                                for (Presentation presentation : availablePresentations) {
                                    try {
                                        if (item.isEqual(presentation.getGoLiveDateTime().toLocalDate())) {
                                            setStyle("-fx-background-color: #80e980;");
                                        }
                                    } catch (NullPointerException e) {
                                        //Exception thrown when goLiveTimeDate has not been specified for a presentation. Do nothing
                                    }
                                }
                            }
                        };
                    }
                };
        calendar.setDayCellFactory(dayCellFactory);
        DatePickerSkin calendarSkin = new DatePickerSkin(calendar);
        calendarNode = calendarSkin.getPopupContent();
        calendarNode.setStyle("-fx-font-size: 10px;");
        calendarNode.setEffect(null);

        switch (state) {
            case TOP_LEVEL:
            case SEARCH_ALL:
                rightPanelVBox.setPadding(new Insets(10));
                rightPanelVBox.getChildren().clear();
                rightPanelVBox.getChildren().addAll(schedulePanel, calendarNode, scheduleVBox);
                break;

            case MODULE:
            case SEARCH_IN_MODULE:
                if (selectedPresPanel != null) {
                    rightPanelVBox.setPadding(new Insets(10, 9, 10, 10));
                    rightPanelVBox.getChildren().clear();
                    for (int i = 0; i < selectedPresPanel.getPresentation().getMaxSlideNumber(); i++) {
                        VBox vbox = new VBox(3);
                        vbox.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderStroke.DEFAULT_WIDTHS)));
                        vbox.setAlignment(Pos.CENTER);
                        vbox.setPadding(new Insets(5));

                        StackPane previewPane = new StackPane(selectedPresPanel.getPresentation().getSlidePreview(i, RIGHT_PANEL_WIDTH - 10));
                        vbox.getChildren().add(previewPane);
                        vbox.getChildren().add(new Label(Integer.toString(i + 1)));
                        vbox.setStyle("-fx-background-color: #ffffff");
                        rightPanelVBox.getChildren().add(vbox);
                    }
                } else {
                    rightPanelVBox.setPadding(new Insets(10));
                    rightPanelVBox.getChildren().clear();
                    rightPanelVBox.getChildren().addAll(schedulePanel, calendarNode, scheduleVBox);
                }
                break;

            default:
                //Do nothing
        }
    }

    public void updateAvailablePresentations() {
        if (availablePresentations == null)
            availablePresentations = new ArrayList<>();
        else
            availablePresentations.clear();

        if (availableModules == null)
            availableModules = new ArrayList<>();
        else
            availableModules.clear();

        if (availableSubjects == null)
            availableSubjects = new ArrayList<>();
        else
            availableSubjects.clear();

        //Create a list of available modules based on module list from server
        for (Module module : ediManager.getPresentationLibraryManager().getUserModuleList()) {
            Subject subject = Subject.findInArray(module.getSubjectName(), availableSubjects);

            if (subject == null) {
                subject = new Subject(module.getSubjectName());
                availableSubjects.add(subject);
            }

            availableModules.add(new DashModule(module, subject));
        }

        //Create a list of available presentations based on metadata from server
        for (PresentationMetadata presMeta : ediManager.getPresentationLibraryManager().getLocalPresentationList()) {
            ParserXML parser = null;
            try {
                parser = new ParserXML(PRESENTATIONS_PATH + File.separator + presMeta.getModuleName() + File.separator + presMeta.getDocumentID() + File.separator + presMeta.getDocumentID() + ".xml");
            } catch (FileNotFoundException e) {
                logger.error("XML file not found: " + presMeta.getXml_url());
            }
            Presentation presentation = parser.parsePresentation();
            presentation.setPresentationMetadata(presMeta);

            DashModule module = DashModule.findInArray(presMeta.getModule_id(), availableModules);

            try {
                module.addPresentation(presentation);
                availablePresentations.add(presentation);
            } catch (NullPointerException e) {
                logger.info("Module " + presMeta.getModuleName() + " (module_ID: " + presMeta.getModule_id() + ") was not found in the dashboard for presentation " + presentation.getDocumentID());
            }
        }

        setupSubjectPanels();
        setupModulePanels();
        setupPresentationPanels();
        setupSchedulePanels();
        updateSubjectCheckBoxes();

        if (currentState != null) {
            if (currentState == DashboardState.SEARCH_ALL || currentState == DashboardState.TOP_LEVEL) {
                filterBy(filterSubjects);
            } else {
                filterBy(selectedModule);
            }
        }

        if (selectedPresPanel != null) {
            setSelectedPreviewPanel(PresentationPanel.findInArray(selectedPresPanel.getPresentation().getPresentationMetadata().getPresentationID(), presentationPanels), true);
        }
        if (selectedModulePanel != null) {
            for (SubjectPanel panel : subjectPanels) {
                setSelectedPreviewPanel(ModulePanel.findInArray(selectedModulePanel.getModule().getModuleID(), panel.getModulePanels()), true);
            }
        }
        if (searchField != null) {
            search(searchField.getText());
        }
    }

    private void setupSubjectPanels() {
        if (subjectPanels == null)
            subjectPanels = new ArrayList<>();
        else
            subjectPanels.clear();

        if (subjectPanelsVBox == null) {
            subjectPanelsVBox = new VBox(5);
            subjectPanelsVBox.setAlignment(Pos.TOP_CENTER);
        } else {
            subjectPanelsVBox.getChildren().clear();
        }

        for (Subject subject : availableSubjects) {
            subjectPanels.add(new SubjectPanel(subject, subjectPanelsVBox));
        }

        if (subjectSortCombo != null) {
            sortSubjects(subjectSortCombo.getValue());
        }
    }

    private void setupModulePanels() {
        for (DashModule module : availableModules) {
            SubjectPanel subjectPanel = SubjectPanel.findInArray(module.getSubject().getSubjectName(), subjectPanels);

            try {
                ModulePanel modulePanel = new ModulePanel(module, subjectPanel);
                modulePanel.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    if (event.getButton().equals(MouseButton.PRIMARY)) {
                        if (modulePanel.isSelected()) {
                            goToState(DashboardState.MODULE);
                        } else {
                            setSelectedPreviewPanel(modulePanel, true);
                        }
                    } else if (event.getButton().equals(MouseButton.SECONDARY)) {
                        setSelectedPreviewPanel(modulePanel, true);

                        ContextMenu menu = new ContextMenu();
                        MenuItem open = new MenuItem("Open");
                        open.setOnAction(event1 -> goToState(DashboardState.MODULE));
                        menu.getItems().add(open);
                        MenuItem details = new MenuItem("Details");
                        details.setOnAction(event1 -> showDetailsWindow(modulePanel.getModule()));
                        menu.getItems().add(details);

                        menu.show(dashboardStage, event.getScreenX(), event.getScreenY());
                    }
                    event.consume();
                });
            } catch (NullPointerException e) {
                logger.info("Couldn't find subject panel for module" + module.getModuleName());
            }
        }

        if (moduleSortCombo != null)
            sortModules(moduleSortCombo.getValue());
    }

    private void setupPresentationPanels() {
        if (presentationPanels == null)
            presentationPanels = new ArrayList<>();
        else
            presentationPanels.clear();

        if (presentationPanelsFlowPane == null) {
            presentationPanelsFlowPane = new FlowPane(Orientation.HORIZONTAL);
            presentationPanelsFlowPane.setPadding(new Insets(5, 0, 5, 0));
            presentationPanelsFlowPane.setVgap(4);
            presentationPanelsFlowPane.setHgap(4);
            presentationPanelsFlowPane.getStyleClass().add("edge-to-edge");
            presentationPanelsFlowPane.setAlignment(Pos.TOP_CENTER);
        } else {
            presentationPanelsFlowPane.getChildren().clear();
        }

        for (Presentation presentation : availablePresentations) {
            PresentationPanel presentationPanel = new PresentationPanel(presentation, presentationPanelsFlowPane);
            presentationPanel.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (presentationPanel.isSelected()) {
                        launchPresentation(presentationPanel.getPresentation());
                    } else {
                        setSelectedPreviewPanel(presentationPanel, true);
                    }
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    setSelectedPreviewPanel(presentationPanel, true);

                    ContextMenu cMenu = new ContextMenu();

                    MenuItem open = new MenuItem("Open");
                    open.setOnAction(openEvent -> launchPresentation(presentationPanel.getPresentation()));
                    cMenu.getItems().add(open);

                    if (this instanceof TeacherDashboard) {
                        MenuItem openAndGoLive = new MenuItem("Open & Go Live");
                        openAndGoLive.setOnAction(goLiveEvent -> {
                            setLive(presentationPanel, true);
                            launchPresentation(presentationPanel.getPresentation());
                        });
                        cMenu.getItems().add(openAndGoLive);

                        MenuItem schedule = new MenuItem("Schedule");
                        schedule.setOnAction(scheduleEvent -> showScheduler(presentationPanel, event.getScreenX(), event.getScreenY()));
                        cMenu.getItems().add(schedule);

                        MenuItem unschedule = new MenuItem("Unschedule");
                        unschedule.setOnAction(unscheduleEvent -> {
                            presentationPanel.getPresentation().setGoLiveDate(null);
                            ediManager.getSocketClient().setPresentationGoLive(presentationPanel.getPresentation().getPresentationMetadata().getPresentationID(), "0");
                        });
                        cMenu.getItems().add(unschedule);

                        MenuItem print = new MenuItem("Generate PDF");
                        print.setOnAction(printEvent -> printPresentation(presentationPanel.getPresentation()));
                        cMenu.getItems().add(print);

                        MenuItem report = new MenuItem("Report");
                        report.setOnAction(reportEvent -> showReport(presentationPanel.getPresentation()));
                        cMenu.getItems().add(report);

                        MenuItem details = new MenuItem("Details");
                        details.setOnAction(detailsEvent -> showDetailsWindow(presentationPanel.getPresentation()));
                        cMenu.getItems().add(details);

                        MenuItem edit = new MenuItem("Edit");
                        edit.setOnAction(editEvent -> showPresentationEditor(presentationPanel.getPresentation().getPath()));
                        cMenu.getItems().add(edit);

                        MenuItem resetInteractions = new MenuItem("Reset interaction data");
                        resetInteractions.setOnAction(resetEvent -> {
                            ediManager.getSocketClient().resetInteractionsForPresentation(this.selectedPresPanel.
                                    getPresentation().getPresentationMetadata().getPresentationID());
                        });
                        cMenu.getItems().add(resetInteractions);

                        MenuItem delete = new MenuItem("Delete");
                        delete.setOnAction(deleteEvent -> deletePresentation(presentationPanel));
                        cMenu.getItems().add(delete);
                    }
                    cMenu.show(dashboardStage, event.getScreenX(), event.getScreenY());
                }
                event.consume();
            });
            presentationPanels.add(presentationPanel);
        }

        if (presSortCombo != null) {
            sortPresentations(presSortCombo.getValue());
        }

    }

    private void setupSchedulePanels() {
        if (schedulePanels == null) {
            schedulePanels = new ArrayList<>();
        } else {
            for (PresSchedulePanel panel : schedulePanels) {
                panel.setParentPane(null);
            }

            schedulePanels.clear();
        }

        if (scheduleVBox == null) {
            scheduleVBox = new VBox();
        } else {
            scheduleVBox.getChildren().clear();
        }

        for (PresentationPanel presPanel : presentationPanels) {
            if (presPanel.getPresentation().getGoLiveDateTime() != null) {
                PresSchedulePanel schedulePanel = new PresSchedulePanel(scheduleVBox, presPanel);
                schedulePanels.add(schedulePanel);
                schedulePanel.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    setSelectedPreviewPanel(presPanel, true);
                    selectedModule = presPanel.getPresentation().getModule();
                    goToState(DashboardState.MODULE);
                    double panelY = presPanel.getLayoutY();
                    double flowpaneHeight = presentationPanelsFlowPane.getHeight();
                    presentationsScrollPane.setVvalue(panelY/flowpaneHeight);
                });
            }
        }

        schedulePanels.sort(Comparator.comparing(PresSchedulePanel::getGoLiveDateTime));

        updateSchedulePanels();
    }

    private void updateSchedulePanels() {
        for (PresSchedulePanel schedulePanel : schedulePanels) {
            schedulePanel.setFiltered(true);

            if (selectedDate.isEqual(schedulePanel.getGoLiveDateTime().toLocalDate())) {
                schedulePanel.setFiltered(false);
            }
        }
    }

    private void updateSubjectCheckBoxes() {
        if (subjectCheckboxesVBox == null) {
            subjectCheckboxesVBox = new VBox(3);
            subjectCheckboxesVBox.setPadding(new Insets(0, 0, 3, 0));
            subjectCheckboxesVBox.setStyle("-fx-background-color: #ffffff;");
        } else {
            subjectCheckboxesVBox.getChildren().clear();
        }

        //Sort subjects alphabetically without affecting the displayed subjects
        ArrayList<Subject> subjectsForButtons = (ArrayList<Subject>) availableSubjects.clone();
        subjectsForButtons.sort((s1, s2) -> SubjectSortKey.NAME_AZ.compare(s1, s2));

        subjectCheckboxes = new ArrayList<>();

        if (filterSubjects == null) {
            filterSubjects = new ArrayList<>();
        }

        for (Subject subject : subjectsForButtons) {
            CheckBox subjectCheckBox = new CheckBox(subject.getSubjectName());
            subjectCheckBox.selectedProperty().addListener(change -> {
                if (subjectCheckBox.isSelected() && !filterSubjects.contains(subject.getSubjectName())) {
                    filterSubjects.add(subject.getSubjectName());
                } else if (!subjectCheckBox.isSelected() && filterSubjects.contains(subject.getSubjectName())) {
                    filterSubjects.remove(subject.getSubjectName());
                }

                boolean allSelected = true;

                for (CheckBox checkBox : subjectCheckboxes) {
                    if (!checkBox.isSelected()) {
                        allSelected = false;
                        break;
                    }
                }

                if (allSelected) {
                    showAllButton.setText("Deselect all");
                } else {
                    showAllButton.setText("Select all");
                }

                filterBy(filterSubjects);
            });
            subjectCheckBox.setPadding(new Insets(1));
            subjectCheckboxes.add(subjectCheckBox);
            subjectCheckboxesVBox.getChildren().add(subjectCheckBox);
            if (filterSubjects.contains(subject.getSubjectName())) {
                subjectCheckBox.setSelected(true);
            }
        }
    }

    private int getNumOfScheduledPresOnDate(LocalDate date) {
        int count = 0;

        for (Presentation presentation : availablePresentations) {
            try {
                if (presentation.getGoLiveDateTime().toLocalDate().isEqual(date)) {
                    count++;
                }
            } catch (NullPointerException e) {
                //Presentation has no set goLive date. Do nothing
            }
        }

        return count;
    }

    private void setLive(PresentationPanel presPanel, boolean live) {
        if (!live && presPanel.isLive()) {
            presPanel.setLive(false);
        } else if (live && !presPanel.isLive()) {
            presPanel.setLive(true);
        }
    }

    private void setSelectedPreviewPanel(PreviewPanel previewPanel, boolean setSelected) {
        if (previewPanel instanceof PresentationPanel) {
            if (setSelected) {
                previewPanel.setSelected(true);
                if (selectedPresPanel != null && selectedPresPanel != previewPanel) {
                    selectedPresPanel.setSelected(false);
                }
                selectedPresPanel = (PresentationPanel) previewPanel;
                if (currentState == DashboardState.SEARCH_ALL) {
                    setSelectedPreviewPanel(selectedModulePanel, false);
                }
            } else {
                previewPanel.setSelected(false);
                selectedPresPanel = null;
            }

            displayBorderRight(DashboardState.MODULE);
        } else if (previewPanel instanceof ModulePanel) {
            if (setSelected) {
                previewPanel.setSelected(true);
                if (selectedModulePanel != null && selectedModulePanel != previewPanel) {
                    selectedModulePanel.setSelected(false);
                }
                selectedModulePanel = (ModulePanel) previewPanel;
                selectedModule = selectedModulePanel.getModule();
                if (currentState == DashboardState.SEARCH_ALL) {
                    setSelectedPreviewPanel(selectedPresPanel, false);
                }
            } else {
                previewPanel.setSelected(false);
                selectedModulePanel = null;
            }
        }
    }

    private void deletePresentation(PresentationPanel previewPanel) {
        Presentation presentationToDelete = previewPanel.getPresentation();
        //Try to remove the presentation from the server
        boolean successful_removal = ediManager.getPresentationLibraryManager().removePresentation(presentationToDelete.getPresentationMetadata().getPresentationID());

        if (successful_removal) {
            updateAvailablePresentations();
        } else logger.warn("Presentation Could not be removed");

        setSelectedPreviewPanel(previewPanel, false);
    }

    private MenuBar addMenuBar() {
        menuBar = new MenuBar();
        //Due to travis fails, this couldn't be done in the constructor:

        Menu fileMenu = new Menu("File");
        Menu openPresMenu = new Menu("Open presentation...");
        fileMenu.getItems().add(openPresMenu);
        MenuItem openLocal = new MenuItem(OPEN_LOCAL_PRES_CAPTION);
        openLocal.setOnAction(event -> showOpenLocalPres());
        MenuItem openRemote = new MenuItem(OPEN_REMOTE_PRES_CAPTION);
        openRemote.setOnAction(event -> showOpenRemotePres(""));
        openPresMenu.getItems().addAll(openLocal, openRemote);

        if (this instanceof TeacherDashboard) {
            MenuItem upload = new MenuItem("Upload presentation to server");
            upload.setOnAction(event -> showAddPresToServer());
            fileMenu.getItems().add(upload);
        }

        Menu viewMenu = new Menu("View");
        MenuItem showWelcomeMessage = new MenuItem("Show Welcome Message");
        showWelcomeMessage.setOnAction(event -> {
            isWelcomeTextHidden = false;
            goToState(DashboardState.TOP_LEVEL);
        });
        viewMenu.getItems().add(showWelcomeMessage);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutMenuItem = new MenuItem("About");
        aboutMenuItem.setOnAction(event -> showAboutWindow());
        helpMenu.getItems().add(aboutMenuItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);

        return menuBar;
    }

    private void showAboutWindow() {
        aboutPopup = new Popup();
        StackPane aboutStackPane = new StackPane();
        aboutStackPane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> aboutPopup.hide());
        aboutPopup.getContent().add(aboutStackPane);
        Region backgroundRegion = new Region();
        backgroundRegion.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        DropShadow shadow = new DropShadow();
        backgroundRegion.setEffect(shadow);
        aboutStackPane.getChildren().add(backgroundRegion);
        BorderPane aboutBorder = new BorderPane();
        aboutStackPane.getChildren().add(aboutBorder);
        ImageView ediLogoImageView = new ImageView(new Image("file:projectResources/logos/ediLogo400x400.png"));
        ediLogoImageView.setPreserveRatio(true);
        ediLogoImageView.setSmooth(true);
        ediLogoImageView.setFitWidth(300);
        ediLogoImageView.setCache(true);
        Label aboutLabel = new Label("Edi by I2LP, " + Constants.BUILD_STRING);
        aboutBorder.setBottom(aboutLabel);
        setAlignment(aboutLabel, Pos.CENTER);
        aboutBorder.setCenter(ediLogoImageView);

        aboutPopup.setAutoHide(true);
        aboutPopup.show(dashboardStage);
    }

    /**
     * Helper method to launch correct Presentation manager dependent upon current object type
     *
     * @param presentation Presentation to open
     * @author Amrik Sadhra
     */
    private void launchPresentation(Presentation presentation) {
        if (presentationManager != null)
            presentationManager.close();

        if (this instanceof StudentDashboard) {
            presentationManager = new PresentationManagerStudent(ediManager);
        } else if (this instanceof TeacherDashboard) {
            presentationManager = new PresentationManagerTeacher(ediManager);
        }
        ediManager.setPresentationManager(presentationManager);
        presentationManager.openPresentation(presentation, false);
    }

    private void search(String text) { //TODO: show author & tags when found?
        if (text.equals("")) {
            if (currentState == DashboardState.SEARCH_IN_MODULE) {
                goToState(DashboardState.MODULE);
            } else if (currentState == DashboardState.SEARCH_ALL) {
                goToState(DashboardState.TOP_LEVEL);
            }
        } else {
            if (currentState == DashboardState.MODULE) {
                goToState(DashboardState.SEARCH_IN_MODULE);
            } else if (currentState == DashboardState.TOP_LEVEL) {
                goToState(DashboardState.SEARCH_ALL);
            }
        }

        for (PresentationPanel panel : presentationPanels) {
            panel.search(text);
            if (panel == selectedPresPanel && panel.isHidden()) {
                setSelectedPreviewPanel(panel, false);
            }
        }

        for (SubjectPanel subjectPanel : subjectPanels) {
            for (ModulePanel panel : subjectPanel.getModulePanels()) {
                panel.search(text);
                if (panel == selectedModulePanel && panel.isHidden()) {
                    setSelectedPreviewPanel(panel, false);
                }
            }
        }

        sortSubjects(subjectSortCombo.getValue());
        sortModules(moduleSortCombo.getValue());
        sortPresentations(presSortCombo.getValue());


        //Displaying "No matches" has to be the last thing in this method
        if (presentationPanelsFlowPane.getChildren().contains(noMatchesPres)) {
            presentationPanelsFlowPane.getChildren().remove(noMatchesPres);
        }

        if (subjectPanelsVBox.getChildren().contains(noMatchesSubject)) {
            subjectPanelsVBox.getChildren().remove(noMatchesSubject);
        }

        if (presentationPanelsFlowPane.getChildren().size() == 0) {
            presentationPanelsFlowPane.getChildren().add(noMatchesPres);
        }

        if (subjectPanelsVBox.getChildren().size() == 0) {
            subjectPanelsVBox.getChildren().add(noMatchesSubject);
        }
    }

    private void filterBy(Object filter) {
        if (filter != null) {
            if (filter instanceof ArrayList) {
                if (((ArrayList) filter).size() != 0) {
                    if (((ArrayList) filter).get(0) instanceof String) {
                        setAllFiltering(true);

                        ArrayList<String> filterSubjects = (ArrayList<String>) filter;

                        for (String subjectName : filterSubjects) {
                            for (PresentationPanel panel : presentationPanels) {
                                if (subjectName.equals(panel.getPresentation().getSubject().getSubjectName()))
                                    panel.setFiltered(false);
                            }

                            for (SubjectPanel subjectPanel : subjectPanels) {
                                for (ModulePanel modulePanel : subjectPanel.getModulePanels()) {
                                    if (subjectName.equals(modulePanel.getModule().getSubject().getSubjectName()))
                                        modulePanel.setFiltered(false);
                                }

                                if (subjectName.equals(subjectPanel.getSubject().getSubjectName()))
                                    subjectPanel.setFiltered(false);
                            }
                        }
                    }
                } else {
                    setAllFiltering(false);
                }
            } else if (filter instanceof Subject) {
                Subject subject = (Subject) filter;

                for (PresentationPanel panel : presentationPanels) {
                    if (subject.getSubjectName().equals(panel.getPresentation().getSubject().getSubjectName()))
                        panel.setFiltered(false);
                    else
                        panel.setFiltered(true);
                }

                for (SubjectPanel subjectPanel : subjectPanels) {
                    for (ModulePanel modulePanel : subjectPanel.getModulePanels()) {
                        if (subject.getSubjectName().equals(modulePanel.getModule().getSubject().getSubjectName()))
                            modulePanel.setFiltered(false);
                        else
                            modulePanel.setFiltered(true);
                    }

                    if (subject.getSubjectName().equals(subjectPanel.getSubject().getSubjectName()))
                        subjectPanel.setFiltered(false);
                    else
                        subjectPanel.setFiltered(true);
                }
            } else if (filter instanceof DashModule) {
                DashModule module = (DashModule) filter;

                for (PresentationPanel panel : presentationPanels) {
                    if (module.getModuleName().equals(panel.getPresentation().getModule().getModuleName()))
                        panel.setFiltered(false);
                    else
                        panel.setFiltered(true);
                }

                for (SubjectPanel subjectPanel : subjectPanels) {
                    for (ModulePanel panel : subjectPanel.getModulePanels()) {
                        if (module.getModuleName().equals(panel.getModule().getModuleName()))
                            panel.setFiltered(false);
                        else
                            panel.setFiltered(true);
                    }
                }
            }
        } else {
            setAllFiltering(false);
        }

        if (subjectSortCombo != null) {
            sortSubjects(subjectSortCombo.getValue());
        }
        if (moduleSortCombo != null) {
            sortModules(moduleSortCombo.getValue());
        }
        if (presSortCombo != null) {
            sortPresentations(presSortCombo.getValue());
        }
    }

    private void setAllFiltering(boolean isFiltered) {
        if (presentationPanels != null) {
            for (PresentationPanel panel : presentationPanels) {
                panel.setFiltered(isFiltered);
            }
        }

        if (subjectPanels != null) {
            for (SubjectPanel subjectPanel : subjectPanels) {
                for (ModulePanel modulePanel : subjectPanel.getModulePanels()) {
                    modulePanel.setFiltered(isFiltered);
                }

                subjectPanel.setFiltered(isFiltered);
            }
        }
    }

    private void sortPresentations(PresSortKey sortKey) {
        presentationPanels.sort((p1, p2) -> sortKey.compare(p1.getPresentation(), p2.getPresentation()));

        presentationPanelsFlowPane.getChildren().clear();
        for (PresentationPanel panel : presentationPanels) {
            panel.updateVisibility();
        }
    }

    private void sortModules(DashModuleSortKey sortKey) {
        for (SubjectPanel subjectPanel : subjectPanels) {
            subjectPanel.getModulePanels().sort((m1, m2) -> sortKey.compare(m1.getModule(), m2.getModule()));

            subjectPanel.getModulePanelsHBox().getChildren().clear();

            for (ModulePanel modulePanel : subjectPanel.getModulePanels()) {
                modulePanel.updateVisibility();
            }
        }
    }

    private void sortSubjects(SubjectSortKey sortKey) {
        subjectPanels.sort((s1, s2) -> sortKey.compare(s1.getSubject(), s2.getSubject()));

        subjectPanelsVBox.getChildren().clear();
        for (SubjectPanel panel : subjectPanels) {
            panel.updateVisibility();
        }
    }

    private void showScheduler(PresentationPanel panel, double x, double y) {
        Popup schedulerPopup = new Popup();

        dateTimePicker = new DateTimePicker();

        dateTimePicker.getScheduleButton().setOnAction(event -> {
            logger.info("Scheduled to: " + dateTimePicker.getDateTime().toString());
            panel.getPresentation().setGoLiveDate(dateTimePicker.getDateTime());
            ediManager.getSocketClient().setPresentationGoLive(
                    panel.getPresentation().getPresentationMetadata().getPresentationID(),
                    Timestamp.valueOf(panel.getPresentation().getGoLiveDateTime()).toString());
            schedulerPopup.hide();
            setupSchedulePanels();
        });

        schedulerPopup.setAutoHide(true);
        schedulerPopup.getContent().add(dateTimePicker);
        schedulerPopup.show(dashboardStage, x, y);
    }

    private void showPresentationEditor(String presentationPath) {
        new PresentationEditor(presentationPath);
    }

    private void showDetailsWindow(Presentation presentation) {
        Popup detailsPopup = new Popup();

        GridPane detailsPane = new GridPane();
        detailsPane.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), null)));
        detailsPane.setPadding(new Insets(10));
        detailsPane.setEffect(new DropShadow());
        detailsPane.setAlignment(Pos.CENTER);
        detailsPane.getColumnConstraints().add(new ColumnConstraints(100));
        detailsPane.getColumnConstraints().add(new ColumnConstraints(200));
        detailsPopup.getContent().add(detailsPane);

        Label name = new Label(presentation.getDocumentTitle());
        name.getStyleClass().add("h4");
        name.setPadding(new Insets(5));
        GridPane.setHalignment(name, HPos.CENTER);
        detailsPane.add(name, 0, 0, 2, 1);

        Label authorLabel = new Label("Author: ");
        authorLabel.setPadding(new Insets(5));
        GridPane.setValignment(authorLabel, VPos.TOP);
        detailsPane.add(authorLabel, 0, 1);

        Label author = new Label(presentation.getAuthor());
        author.setPadding(new Insets(5));
        detailsPane.add(author, 1, 1);

        Label moduleLabel = new Label("Module: ");
        moduleLabel.setPadding(new Insets(5));
        GridPane.setValignment(moduleLabel, VPos.TOP);
        detailsPane.add(moduleLabel, 0, 2);

        Label module = new Label(presentation.getModule().getModuleName());
        module.setPadding(new Insets(5));
        detailsPane.add(module, 1, 2);

        Label subjectLabel = new Label("Subject: ");
        subjectLabel.setPadding(new Insets(5));
        GridPane.setValignment(subjectLabel, VPos.TOP);
        detailsPane.add(subjectLabel, 0, 3);

        Label subject = new Label(presentation.getSubject().getSubjectName());
        subject.setPadding(new Insets(5));
        detailsPane.add(subject, 1, 3);

        Label descriptionLabel = new Label("Description: ");
        descriptionLabel.setPadding(new Insets(5));
        GridPane.setValignment(descriptionLabel, VPos.TOP);
        detailsPane.add(descriptionLabel, 0, 4);

        Text description = new Text(presentation.getDescription());
        description.setFill(descriptionLabel.getTextFill());
        StackPane descriptionPane = new StackPane(description);
        descriptionPane.setPadding(new Insets(5));
        descriptionPane.setAlignment(Pos.TOP_LEFT);
        description.setWrappingWidth(200);
        detailsPane.add(descriptionPane, 1, 4);

        Label tagsLabel = new Label("Tags: ");
        tagsLabel.setPadding(new Insets(5));
        GridPane.setValignment(tagsLabel, VPos.TOP);
        detailsPane.add(tagsLabel, 0, 5);

        Label tags = new Label(presentation.getTags());
        tags.setPadding(new Insets(5));
        detailsPane.add(tags, 1, 5);

        detailsPopup.setAutoHide(true);
        detailsPopup.show(dashboardStage);
    }

    private void showDetailsWindow(DashModule module) {
        Popup detailsPopup = new Popup();

        GridPane detailsPane = new GridPane();
        detailsPane.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), null)));
        detailsPane.setPadding(new Insets(10));
        detailsPane.setEffect(new DropShadow());
        detailsPane.setAlignment(Pos.CENTER);
        detailsPane.getColumnConstraints().add(new ColumnConstraints(100));
        detailsPane.getColumnConstraints().add(new ColumnConstraints(200));
        detailsPopup.getContent().add(detailsPane);

        Label name = new Label(module.getModuleName());
        name.getStyleClass().add("h4");
        name.setPadding(new Insets(5));
        GridPane.setHalignment(name, HPos.CENTER);
        detailsPane.add(name, 0, 0, 2, 1);

        Label subjectLabel = new Label("Subject: ");
        subjectLabel.setPadding(new Insets(5));
        GridPane.setValignment(subjectLabel, VPos.TOP);
        detailsPane.add(subjectLabel, 0, 1);

        Label subject = new Label(module.getSubject().getSubjectName());
        subject.setPadding(new Insets(5));
        detailsPane.add(subject, 1, 1);

        Label descriptionLabel = new Label("Description: ");
        descriptionLabel.setPadding(new Insets(5));
        GridPane.setValignment(descriptionLabel, VPos.TOP);
        detailsPane.add(descriptionLabel, 0, 2);

        Text description = new Text(module.getModuleDescription());
        description.setFill(descriptionLabel.getTextFill());
        StackPane descriptionPane = new StackPane(description);
        descriptionPane.setPadding(new Insets(5));
        descriptionPane.setAlignment(Pos.TOP_LEFT);
        description.setWrappingWidth(200);
        detailsPane.add(descriptionPane, 1, 2);

        detailsPopup.setAutoHide(true);
        detailsPopup.show(dashboardStage);
    }

    private void showReport(Presentation presentation) {
        ReportManager rm = new ReportManager();
        rm.openReportPanel(presentation, ediManager);
    }

    private void showOpenLocalPres() {
        File file = fileChooser.showOpenDialog(dashboardStage);
        if (file != null) {
            ParserXML parserXML = null;
            try {
                parserXML = new ParserXML(file.getPath());
            } catch (FileNotFoundException e) {
                logger.error("XML file not found: " + file.getPath());
            }
            launchPresentation(parserXML.parsePresentation());
        } else logger.info("No presentation was selected");
    }

    private void showOpenRemotePres(String defaultURLField) {
        TextField urlInput = new TextField(defaultURLField);
        urlInput.setPrefWidth(450);

        Button okButton = new Button("OK");
        okButton.setDefaultButton(true);

        HBox root = new HBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(5);
        root.setPadding(new Insets(5, 12, 5, 12));
        root.setHgrow(urlInput, Priority.ALWAYS);

        root.getChildren().add(urlInput);
        root.getChildren().add(okButton);

        Scene scene = new Scene(root, 400, 100);

        final Stage onlineChooser = new Stage();
        onlineChooser.setScene(scene);
        onlineChooser.setTitle("Open Remote Presentation");
        onlineChooser.getIcons().add(new Image("file:projectResources/logos/ediLogo32x32.png"));

        okButton.setOnMouseClicked((e) -> {
            UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
            if (urlValidator.isValid(urlInput.getText())) {
                //Valid URL
                String presentationURL = urlInput.getText();
                onlineChooser.close();

                try {
                    URL url = new URL(presentationURL);//Example source: https://raw.githubusercontent.com/hermanblarsen/SWEng1York/master/projectResources/sampleFiles/xml/i2lpSampleXml.xml?token=AYLAhZslFnsvKaAljeMwbdPuGpYn7Rnbks5ZNciAwA%3D%3D
                    URLConnection connection = url.openConnection();
                    InputSource presentationInputSource = new InputSource(new BufferedInputStream(connection.getInputStream()));

                    ParserXML parser = new ParserXML(presentationInputSource, presentationURL);
                    Presentation presentation = parser.parsePresentation();
                    launchPresentation(presentation);

                } catch (MalformedURLException murle) {
                    logger.error("Malformed URL given for remote presentation.");
                } catch (IOException ioe) {
                    logger.warn("IOException when trying to get remote http presentation.");
                    new Alert(Alert.AlertType.ERROR, "Couldn't get presentation from this URL.");
                    showOpenRemotePres(presentationURL);//Reopen the url entry window so that the user can try again.
                    return;
                }

            } else {
                //Not Valid URL.
                logger.info("Invalid url given for remote presentation.");
                new Alert(Alert.AlertType.ERROR, "Invalid URL.").showAndWait();
            }

        });

        onlineChooser.show();
    }

    private void showAddPresToServer() {
        AtomicReference<File> xmlLocation = new AtomicReference<>(); //Store location of XML from filechooser, for upload to presentation after Thumbnail and CSS gen

        if (addToServerStage != null) {
            addToServerStage.close();
        }

        addToServerStage = new Stage();
        addToServerStage.resizableProperty().setValue(Boolean.FALSE);
        GridPane addToServerGridPane = new GridPane();
        addToServerGridPane.setAlignment(Pos.CENTER);

        BorderPane rootPane = new BorderPane();
        rootPane.setCenter(addToServerGridPane);

        addToServerGridPane.setHgap(10);
        addToServerGridPane.setVgap(5);
        addToServerGridPane.setPadding(new Insets(10));
        Scene addToServerScene = new Scene(rootPane, 250, 200);
        addToServerScene.getStylesheets().add("bootstrapfx.css");

        Label filename = new Label("No file selected");

        Button selectXML = new Button("Select XML");
        selectXML.getStyleClass().setAll("btn", "btn-primary");
        selectXML.setOnAction(event1 -> {
            File file = fileChooser.showOpenDialog(addToServerStage);
            if (file != null) {
                xmlLocation.set(file);
                filename.setText(file.getName());
            } else {
                filename.setText("No file selected");
            }
        });
        addToServerGridPane.add(selectXML, 0, 0);
        GridPane.setConstraints(selectXML, 0, 0, 1, 1, HPos.CENTER, VPos.CENTER);

        addToServerGridPane.add(filename, 0, 1);
        GridPane.setConstraints(filename, 0, 1, 1, 1, HPos.CENTER, VPos.CENTER);

        Label saveInModule = new Label("Save in module:");
        saveInModule.setPadding(new Insets(10, 0, 0, 0));
        addToServerGridPane.add(saveInModule, 0, 2);
        GridPane.setConstraints(saveInModule, 0, 2, 1, 1, HPos.CENTER, VPos.CENTER);

        ComboBox<DashModule> modulesCombo = new ComboBox<>();
        ArrayList<DashModule> modulesSorted = (ArrayList<DashModule>) availableModules.clone();
        modulesSorted.sort((m1, m2) -> DashModuleSortKey.NAME_AZ.compare(m1, m2));
        modulesCombo.getItems().addAll(modulesSorted);
        addToServerGridPane.add(modulesCombo, 0, 3);
        GridPane.setConstraints(modulesCombo, 0, 3, 1, 1, HPos.CENTER, VPos.CENTER);

        Button addButton = new Button("Add");
        addButton.setAlignment(Pos.CENTER);
        addButton.getStyleClass().setAll("btn", "btn-success");
        addButton.setOnAction(event1 -> {
            try {
                ediManager.getPresentationLibraryManager().uploadPresentation(xmlLocation.get().getAbsolutePath(), modulesCombo.getValue().getModuleID());
                addToServerStage.close();
            } catch (NullPointerException e) {
                //Will be thrown if the user has not selected a file
                //Do nothing
            }
        });
        addToServerGridPane.add(addButton, 0, 7);
        GridPane.setConstraints(addButton, 0, 7, 1, 1, HPos.CENTER, VPos.CENTER);

        addToServerStage.setScene(addToServerScene);
        addToServerStage.show();
    }

    private void printPresentation(Presentation presentation) {
        ThumbnailGenerationManager.generateSlideThumbnails(presentation, true);
    }

    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }
}

