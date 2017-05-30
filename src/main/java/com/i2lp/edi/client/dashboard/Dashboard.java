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
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.stage.Window;
import org.apache.commons.validator.UrlValidator;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.i2lp.edi.client.Constants.PRESENTATIONS_PATH;
import static com.i2lp.edi.client.Constants.SLIDE_PREVIEW_WIDTH;
import static com.i2lp.edi.client.utilities.Utilities.removeFileExtension;
import static javafx.scene.layout.BorderPane.setAlignment;


/**
 * Created by amriksadhra on 24/01/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused", "Duplicates"})
public abstract class Dashboard extends Application {
    protected static Logger logger = LoggerFactory.getLogger(Dashboard.class);
    private EdiManager ediManager;
    protected PresentationManager presentationManager;

    private static final double LEFT_PANEL_SIZE = 200;
    protected Scene scene;
    protected BorderPane border;
    private VBox rootBox;
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
    private boolean isWelcomeTextHidden = false;
    private DashboardState currentState;
    private Text noMatchesSubject, noMatchesPres;
    private Button addToServerButton;
    private VBox constantControlsVBox, removableControlsVBox, controlsContainerVBox;
    private ScrollPane controlsScroll;
    private Panel searchPanel, subjectFilterPanel, presSortPanel, moduleSortPanel, subjectSortPanel;
    private DashModule selectedModule;

    protected TextField searchField;
    protected Button selectAllButton;
    protected ArrayList<Button> subjectButtons;
    private ArrayList<CheckBox> subjectCheckboxes;
    private ArrayList<Subject> filterSubjects;
    protected Button loadPresButton;
    protected FileChooser fileChooser;
    protected MenuBar menuBar;

    @Override
    public void start(Stage dashboardStage) {
        this.dashboardStage = dashboardStage;
        //Initialise UI
        dashboardStage.setTitle("Edi");
        Image ediLogoSmall = new Image("file:projectResources/logos/ediLogo32x32.png");
        dashboardStage.getIcons().add(ediLogoSmall);
        dashboardStage.setOnCloseRequest(event -> Platform.exit());
        dashboardStage.setMinWidth(800);
        dashboardStage.setMinHeight(600);

        border = new BorderPane();
        rootBox = new VBox(addMenuBar(), border);
        scene = new Scene(rootBox, 1000, 600);
        noMatchesSubject = new Text("No matches found"); //TODO: move setup of global nodes to separate method
        noMatchesSubject.setFill(Color.GRAY);
        noMatchesSubject.getStyleClass().add("italic");
        noMatchesPres = new Text("No matches found");
        noMatchesPres.setFill(Color.GRAY);
        noMatchesPres.getStyleClass().add("italic");
        scene.getStylesheets().add("bootstrapfx.css");
        dashboardStage.setScene(scene);

        updateAvailablePresentations();

        ediManager.getPresentationLibraryManager().getUserModuleList();

        goToState(DashboardState.TOP_LEVEL);

        dashboardStage.show();
    }

    public void goToState(DashboardState state) {
        currentState = state;
        logger.info("State: " + currentState.name());

        if (searchField != null) {
            if (state == DashboardState.MODULE || state == DashboardState.TOP_LEVEL)
                searchField.setText("");
        }

        if (state == DashboardState.TOP_LEVEL) {
            setSelectedPreviewPanel(selectedPresPanel, false);
        }

        displayBorderTop(state);
        displayBorderCenter(state);
        displayBorderLeft(state); //This has to be called after displayBorderCenter()
        displayBorderRight(state);
    }

    private void displayBorderTop(DashboardState state) {
        HBox topPanel = new HBox(10);
        topPanel.setPadding(new Insets(15, 12, 15, 12));
        topPanel.setStyle("-fx-background-color: #34495e;");

        Text platformTitle = new Text("Integrated Interactive Learning Platform");
        platformTitle.getStyleClass().setAll("h3");
        platformTitle.setFill(Color.WHITESMOKE);

        HBox platformTitleHBox = new HBox(10);
        platformTitleHBox.setAlignment(Pos.CENTER);
        platformTitleHBox.getChildren().add(platformTitle);

        HBox openAddButtonsHBox = new HBox(10);

        final FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter xmlExtensionFilter =
                new FileChooser.ExtensionFilter("XML Presentations (*.XML)", "*.xml", "*.XML");
        fileChooser.getExtensionFilters().add(xmlExtensionFilter);
        fileChooser.setSelectedExtensionFilter(xmlExtensionFilter);
        fileChooser.setInitialDirectory(new File("projectResources/sampleFiles/xml"));
        //fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))); //TODO reinstate when tested
        fileChooser.setTitle("Open Presentation");

        Button openPresButton = new Button("Open Presentation", new ImageView(new Image("file:projectResources/icons/arrow-down.png", 10, 10, true, true)));
        openPresButton.getStyleClass().setAll("btn", "btn-default");
        openPresButton.setOnAction(event -> {
            ContextMenu menu = new ContextMenu();

            MenuItem local = new MenuItem("From this computer");
            local.setOnAction(event1 -> {
                Node source = (Node) event.getSource();
                Window stage = source.getScene().getWindow();


                File file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    ParserXML parserXML = new ParserXML(file.getPath());
                    launchPresentation(parserXML.parsePresentation());
                } else logger.info("No presentation was selected");
            });

            MenuItem online = new MenuItem("Remotely (from HTTP)");
            online.setOnAction(event1 -> {
                //For testing you can use :https://raw.githubusercontent.com/hermanblarsen/SWEng1York/master/projectResources/sampleFiles/xml/i2lpSampleXml.xml?token=AYLAhZswVfz-zJFrEDKoquw1Eg0XybCKks5ZNcqAwA%3D%3D
                openOnlineXMLSelectorWindow("");
            });

            menu.getItems().addAll(local, online);
            menu.show(openPresButton, Side.BOTTOM, 0, 0);
        });

        openAddButtonsHBox.getChildren().add(openPresButton);
        topPanel.getChildren().add(openAddButtonsHBox);

        if (this instanceof TeacherDashboard) {
            addToServerButton = new Button("Add pres to server");
            addToServerButton.getStyleClass().setAll("btn", "btn-success");
            addToServerButton.setOnAction(event -> {
                AtomicReference<File> xmlLocation = new AtomicReference<>(); //Store location of XML from filechooser, for upload to presentation after Thumbnail and CSS gen

                if (addToServerStage != null) {
                    addToServerStage.close();
                }

                addToServerStage = new Stage();
                GridPane addToServerGridPane = new GridPane();
                addToServerGridPane.setAlignment(Pos.CENTER);

                addToServerGridPane.setHgap(10);
                addToServerGridPane.setVgap(10);
                addToServerGridPane.setPadding(new Insets(25, 25, 25, 25));
                Scene addToServerScene = new Scene(addToServerGridPane, 300, 300);
                addToServerScene.getStylesheets().add("bootstrapfx.css");

                Button selectXML = new Button("Select XML");
                selectXML.getStyleClass().setAll("btn", "btn-primary");
                selectXML.setOnAction(event1 -> {
                    File file = fileChooser.showOpenDialog(addToServerStage);
                    xmlLocation.set(file);
                });
                addToServerGridPane.add(selectXML, 0, 0);
                GridPane.setConstraints(selectXML, 0, 0, 1, 1, HPos.CENTER, VPos.CENTER);

                Label saveInModule = new Label("Save in module:");
                addToServerGridPane.add(saveInModule, 0, 1);
                GridPane.setConstraints(saveInModule, 0, 1, 1, 1, HPos.CENTER, VPos.CENTER);

                ComboBox<DashModule> modulesCombo = new ComboBox<>();
                modulesCombo.getItems().addAll(availableModules);
                addToServerGridPane.add(modulesCombo, 0, 2);
                GridPane.setConstraints(modulesCombo, 0, 2, 1, 1, HPos.CENTER, VPos.CENTER);

                Button addButton = new Button("Add");
                addButton.getStyleClass().setAll("btn", "btn-success");
                addButton.setOnAction(event1 -> {
                    ediManager.getPresentationLibraryManager().uploadPresentation(xmlLocation.get().getAbsolutePath(), removeFileExtension(xmlLocation.get().getName()), modulesCombo.getValue().getModuleID());
                    addToServerStage.close();
                });
                addToServerGridPane.add(addButton, 0, 3);
                GridPane.setConstraints(addButton, 0, 3, 1, 1, HPos.CENTER, VPos.CENTER);

                addToServerStage.setScene(addToServerScene);
                addToServerStage.show();
            });

            openAddButtonsHBox.getChildren().add(addToServerButton);

        }

        topPanel.getChildren().add(platformTitleHBox);
        HBox.setHgrow(platformTitleHBox, Priority.ALWAYS);

        switch (state) {
            case TOP_LEVEL:
            case MODULE:
            case SEARCH_IN_MODULE:
            case SEARCH_ALL:
            default:
                //Do nothing
        }

        border.setTop(topPanel);
    }

    private void displayBorderCenter(DashboardState state) {
        VBox vbox = new VBox(5);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.TOP_CENTER);

        ScrollPane modulesScrollPane = new ScrollPane();
        modulesScrollPane.setContent(subjectPanelsVBox);
        modulesScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        modulesScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        modulesScrollPane.setFitToWidth(true);
        modulesScrollPane.getStyleClass().add("edge-to-edge");

        ScrollPane presentationsScrollPane = new ScrollPane();
        presentationsScrollPane.setContent(presentationPanelsFlowPane);
        presentationsScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        presentationsScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        presentationsScrollPane.setFitToWidth(true);
        presentationsScrollPane.getStyleClass().add("edge-to-edge");

        presentationPanelsFlowPane.setPadding(new Insets(5, 0, 5, 0));
        presentationPanelsFlowPane.setVgap(4);
        presentationPanelsFlowPane.setHgap(4);
        presentationPanelsFlowPane.getStyleClass().add("edge-to-edge");
        presentationPanelsFlowPane.setAlignment(Pos.TOP_CENTER);

        switch (state) {
            case TOP_LEVEL:
                if (!isWelcomeTextHidden) {
                    StackPane textStackPane = new StackPane();

                    VBox textVBox = new VBox();
                    textVBox.setAlignment(Pos.TOP_CENTER);
                    Panel textPanel = new Panel();
                    textPanel.getStyleClass().add("panel-primary");
                    textPanel.setBody(textVBox);

                    Button closeTextButton = new Button("X");
                    closeTextButton.getStyleClass().setAll("btn", "btn-default");
                    closeTextButton.setOnAction(event -> {
                        isWelcomeTextHidden = true;
                        vbox.getChildren().remove(textStackPane);
                    });
                    closeTextButton.setStyle("-fx-font-size: 10px; -fx-min-width: 0; -fx-padding: 6px 6px 6px 6px;");

                    StackPane.setAlignment(closeTextButton, Pos.TOP_RIGHT);
                    StackPane.setMargin(closeTextButton, new Insets(5));

                    textStackPane.getChildren().addAll(textPanel, closeTextButton);

                    Text welcomeText = new Text("Welcome to Edi");
                    welcomeText.getStyleClass().setAll("h3");
                    textVBox.getChildren().add(welcomeText);
                    VBox.setMargin(welcomeText, new Insets(5, 0, 5, 0));

                    Text updateText = new Text("Here we can put some updates and stuff");
                    updateText.getStyleClass().setAll("h6", "text-justify");
                    textVBox.getChildren().add(updateText);
                    VBox.setMargin(updateText, new Insets(5, 0, 5, 0));

                    vbox.getChildren().add(textStackPane);
                }

                vbox.getChildren().add(modulesScrollPane);
                border.setCenter(vbox);

                filterBy(filterSubjects);
                break;

            case SEARCH_ALL:
                Text modulesText = new Text("Subjects & Modules");
                modulesText.getStyleClass().setAll("h4");

                Text presentationsText = new Text("Presentations");
                presentationsText.getStyleClass().setAll("h4");
                VBox.setMargin(presentationsText, new Insets(10, 0, 0, 0));

                vbox.getChildren().addAll(modulesText, subjectPanelsVBox, presentationsText, presentationPanelsFlowPane);

                ScrollPane searchScrollPane = new ScrollPane();
                searchScrollPane.setContent(vbox);
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
                backButton.setOnAction(event -> {
                    goToState(DashboardState.TOP_LEVEL);
                });
                Text moduleText = new Text(selectedModule.getSubject().getSubjectName() + " -> " + selectedModule.getModuleName());
                moduleText.getStyleClass().setAll("h4");
                Region dummy = new Region();
                backButton.widthProperty().addListener(observable -> dummy.setPrefWidth(backButton.getWidth()));
                BorderPane borderPane = new BorderPane();
                borderPane.setPadding(new Insets(5, 0, 5, 0));
                borderPane.setLeft(backButton);
                borderPane.setRight(dummy);
                borderPane.setCenter(moduleText);

                vbox.getChildren().addAll(borderPane, presentationsScrollPane);
                border.setCenter(vbox);

                filterBy(selectedModule);
                break;

            default:
                //Do nothing
        }
    }

    private void displayBorderLeft(DashboardState state) {
        //Setup VBox for all panels
        if (controlsContainerVBox == null) {
            controlsContainerVBox = new VBox(8);
        }

        if (constantControlsVBox == null) {
            constantControlsVBox = new VBox(8);
            constantControlsVBox.setPadding(new Insets(10, 10, 0, 10));
            controlsContainerVBox.getChildren().add(constantControlsVBox);
        }

        if (removableControlsVBox == null) {
            removableControlsVBox = new VBox(8);
            removableControlsVBox.setPadding(new Insets(0, 10, 10, 10));
            controlsContainerVBox.getChildren().add(removableControlsVBox);
        }

        if (controlsScroll == null) {
            controlsScroll = new ScrollPane();
            controlsScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
            controlsScroll.setVbarPolicy(ScrollBarPolicy.NEVER);
            controlsScroll.getStyleClass().add("edge-to-edge");
            controlsScroll.setContent(controlsContainerVBox);
            controlsScroll.setFitToWidth(true);
            controlsScroll.setMaxWidth(LEFT_PANEL_SIZE);
            border.setLeft(controlsScroll);
        }

        Insets panelInsets = new Insets(5);

        //Setup search panel
        if (searchPanel == null) {
            searchPanel = new Panel();
            searchPanel.getStyleClass().add("panel-primary");
            if (searchField == null) {
                searchField = new TextField();
                searchField.textProperty().addListener(observable -> search(searchField.getText()));
            }
            searchPanel.setCenter(searchField);
            BorderPane.setMargin(searchField, panelInsets);

            constantControlsVBox.getChildren().add(searchPanel);
        }

        if (subjectFilterPanel == null) {
            subjectCheckboxes = new ArrayList<>();

            //Setup filtering panel
            VBox subjectsVBox = new VBox();
            subjectsVBox.setPadding(new Insets(3, 0, 3, 0));
            subjectsVBox.setSpacing(3);
            subjectsVBox.setStyle("-fx-background-color: #ffffff;");

            selectAllButton = new Button("Select all");
            selectAllButton.getStyleClass().setAll("btn", "btn-success");
            selectAllButton.setMaxWidth(Double.MAX_VALUE);
            selectAllButton.setAlignment(Pos.CENTER);
            selectAllButton.setOnAction(event -> {
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

                    selectAllButton.setText("Select all");
                } else {
                    for (CheckBox checkBox : subjectCheckboxes) {
                        checkBox.setSelected(true);
                    }

                    selectAllButton.setText("Deselect all");
                }

                filterBy(null);
            });
            subjectsVBox.getChildren().add(selectAllButton);

            //Sort subjects alphabetically without affecting the displayed subjects
            ArrayList<Subject> subjectsForButtons = (ArrayList<Subject>) availableSubjects.clone();
            subjectsForButtons.sort((s1, s2) -> SubjectSortKey.NAME_AZ.compare(s1, s2));

            for (Subject subject : subjectsForButtons) {
                CheckBox subjectCheckBox = new CheckBox(subject.getSubjectName());
                subjectCheckBox.selectedProperty().addListener(change -> {
                    if (filterSubjects == null) {
                        filterSubjects = new ArrayList<>();
                    }

                    if (subjectCheckBox.isSelected() && !filterSubjects.contains(subject)) {
                        filterSubjects.add(subject);
                    } else if (!subjectCheckBox.isSelected() && filterSubjects.contains(subject)) {
                        filterSubjects.remove(subject);
                    }

                    boolean allSelected = true;

                    for (CheckBox checkBox : subjectCheckboxes) {
                        if (!checkBox.isSelected()) {
                            allSelected = false;
                            break;
                        }
                    }

                    if (allSelected) {
                        selectAllButton.setText("Deselect all");
                    } else {
                        selectAllButton.setText("Select all");
                    }

                    filterBy(filterSubjects);
                });
                subjectCheckboxes.add(subjectCheckBox);
                subjectsVBox.getChildren().add(subjectCheckBox);
            }

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
                searchPanel.setText("Search");
                removableControlsVBox.getChildren().clear();
                removableControlsVBox.getChildren().addAll(subjectFilterPanel, subjectSortPanel, moduleSortPanel);
                break;
            case MODULE:
            case SEARCH_IN_MODULE:
                searchPanel.setText("Search in " + selectedModule.getModuleName());
                removableControlsVBox.getChildren().clear();
                removableControlsVBox.getChildren().add(presSortPanel);
                break;
            case SEARCH_ALL:
                searchPanel.setText("Search");
                removableControlsVBox.getChildren().clear();
                removableControlsVBox.getChildren().addAll(subjectFilterPanel, subjectSortPanel, moduleSortPanel, presSortPanel);
                break;

            default:
                //Do nothing
        }
    }

    private void displayBorderRight(DashboardState state) {
        switch (state) {
            case TOP_LEVEL:
            case SEARCH_ALL:
                border.setRight(null);
                break;

            case MODULE:
            case SEARCH_IN_MODULE:
                if (selectedPresPanel != null) {
                    VBox parentBox = new VBox(2);
                    parentBox.setPadding(new Insets(5));

                    for (int i = 0; i < selectedPresPanel.getPresentation().getMaxSlideNumber(); i++) {
                        VBox vbox = new VBox(3);
                        vbox.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderStroke.DEFAULT_WIDTHS)));
                        vbox.setAlignment(Pos.CENTER);
                        vbox.setPadding(new Insets(5));

                        vbox.getChildren().add(selectedPresPanel.getPresentation().getSlidePreview(i, SLIDE_PREVIEW_WIDTH));
                        vbox.getChildren().add(new Label(Integer.toString(i + 1)));
                        vbox.setStyle("-fx-background-color: #ffffff");
                        parentBox.getChildren().add(vbox);
                    }

                    ScrollPane scroll = new ScrollPane();
                    scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
                    scroll.setVbarPolicy(ScrollBarPolicy.NEVER);
                    scroll.getStyleClass().add("edge-to-edge");
                    scroll.setContent(parentBox);

                    border.setRight(scroll);
                } else {
                    border.setRight(null);
                }
                break;

            default:
                //Do nothing
        }
    }

    private void addUserPanel() {
        //TODO: add a pane with user info to the top of the left panel
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
            ParserXML parser = new ParserXML(PRESENTATIONS_PATH + presMeta.getDocumentID() + File.separator + presMeta.getDocumentID() + ".xml");
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

        if (subjectSortCombo != null)
            sortSubjects(subjectSortCombo.getValue());
    }

    private void setupModulePanels() {
        for (DashModule module : availableModules) {
            SubjectPanel subjectPanel = SubjectPanel.findInArray(module.getSubject().getSubjectName(), subjectPanels);

            try {
                ModulePanel modulePanel = new ModulePanel(module, subjectPanel);
                modulePanel.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    if (modulePanel.isSelected()) {
                        goToState(DashboardState.MODULE);
                    } else {
                        setSelectedPreviewPanel(modulePanel, true);
                    }
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

                        MenuItem edit = new MenuItem("Edit");
                        edit.setOnAction(editEvent -> showPresentationEditor(presentationPanel.getPresentation().getPath()));
                        cMenu.getItems().add(edit);

                        MenuItem schedule = new MenuItem("Schedule");
                        schedule.setOnAction(scheduleEvent -> showScheduler(event.getScreenX(), event.getScreenY()));
                        cMenu.getItems().add(schedule);

                        MenuItem delete = new MenuItem("Delete");
                        delete.setOnAction(deleteEvent -> deletePresentation(presentationPanel));
                        cMenu.getItems().add(delete);

                        MenuItem print = new MenuItem("Print");
                        print.setOnAction(printEvent -> printPresentation(presentationPanel.getPresentation()));
                        cMenu.getItems().add(print);

                        MenuItem report = new MenuItem("Report");
                        report.setOnAction(reportEvent -> showReport(presentationPanel.getId()));
                        cMenu.getItems().add(report);

                    }
                    cMenu.show(dashboardStage, event.getScreenX(), event.getScreenY());
                }
            });
            presentationPanels.add(presentationPanel);
        }

        if (presSortCombo != null)
            sortPresentations(presSortCombo.getValue());

    }

    private void setLive(PresentationPanel presPanel, boolean live) {
        if (!live && presPanel.isLive()) {
            presPanel.setLive(false);
            if (ediManager.getSocketClient().setPresentationLive(presPanel.getPresentation().getPresentationMetadata().getPresentationID(), false)) {
                //TODO: Stub for successful go offline
            } else {
                //TODO: Stub for unsuccessful go offline
            }
        } else if (live && !presPanel.isLive()) {
            presPanel.setLive(true);
            //Update server database to indicate presentation is Live
            if (ediManager.getSocketClient().setPresentationLive(presPanel.getPresentation().getPresentationMetadata().getPresentationID(), true)) {
                //TODO: Stub for successful go live
            } else {
                //TODO: Stub for unsuccessful go live
            }
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
        boolean successful_removal = ediManager.getPresentationLibraryManager().removePresentation(presentationToDelete.getPresentationMetadata().getPresentationID(), presentationToDelete.getPresentationMetadata().getModule_id());

        if (successful_removal) {
            updateAvailablePresentations();
            presentationPanelsFlowPane.getChildren().remove(previewPanel);
        } else logger.warn("Presentation Could not be removed"); //TODO improve this
    }

    private MenuBar addMenuBar() {
        menuBar = new MenuBar();
        //Due to travis fails, this couldn't be done in the constructor:

        Menu fileMenu = new Menu("File");

        Menu editMenu = new Menu("Edit");

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

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);

        return menuBar;
    }

    private void showAboutWindow() {
        Popup aboutPopup = new Popup();
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
                if (((ArrayList)filter).size() != 0) {
                    if (((ArrayList)filter).get(0) instanceof Subject) {
                        setAllFiltering(true);

                        ArrayList<Subject> filterSubjects = (ArrayList<Subject>) filter;

                        for (Subject subject : filterSubjects) {
                            for (PresentationPanel panel : presentationPanels) {
                                if (subject.getSubjectName().equals(panel.getPresentation().getSubject().getSubjectName()))
                                    panel.setFiltered(false);
                            }

                            for (SubjectPanel subjectPanel : subjectPanels) {
                                for (ModulePanel modulePanel : subjectPanel.getModulePanels()) {
                                    if (subject.getSubjectName().equals(modulePanel.getModule().getSubject().getSubjectName()))
                                        modulePanel.setFiltered(false);
                                }

                                if (subject.getSubjectName().equals(subjectPanel.getSubject().getSubjectName()))
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

    private void showScheduler(double x, double y) {
        Popup schedulerPopup = new Popup();
        VBox popupVBox = new VBox();
        VBox popupVBoxTop = new VBox(5);
        popupVBoxTop.setPadding(new Insets(0, 0, 5, 0));
        popupVBoxTop.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), null)));
        popupVBoxTop.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderStroke.DEFAULT_WIDTHS)));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        popupVBoxTop.getChildren().add(datePicker);

        TimePicker timePicker = new TimePicker();
        popupVBoxTop.getChildren().add(timePicker);

        Button scheduleButton = new Button("Schedule");
        scheduleButton.getStyleClass().setAll("btn", "btn-default");
        scheduleButton.setOnAction(event -> {
            LocalDate date = datePicker.getValue();
            LocalTime time = timePicker.getValue();
            logger.info("Selected Date: " + date);
            logger.info("Selected Time: " + time);
            schedulerPopup.hide();
        });
        popupVBoxTop.setAlignment(Pos.CENTER);
        popupVBox.getChildren().add(popupVBoxTop);
        popupVBox.getChildren().add(scheduleButton);
        popupVBox.setAlignment(Pos.CENTER);

        schedulerPopup.setAutoHide(true);
        schedulerPopup.getContent().add(popupVBox);
        schedulerPopup.show(dashboardStage, x, y);
    }

    private void showPresentationEditor(String presentationPath) {
        new PresentationEditor(presentationPath);
    }

    private void printPresentation(Presentation presentation) {
        ThumbnailGenerationManager.generateSlideThumbnails(presentation, true);
    }

    private void showReport(String presentationID) {
        ReportManager rm = new ReportManager();
        rm.openReportPanel(presentationID);
    }

    private void openOnlineXMLSelectorWindow(String defaultURLField) {
        TextField urlInput = new TextField(defaultURLField);
        urlInput.setPrefWidth(250);

        Button okButton = new Button("OK");

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
            if(urlValidator.isValid(urlInput.getText())) {
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

                } catch (MalformedURLException murle){
                    logger.error("Malformed URL given for remote presentation.");
                } catch (IOException ioe){
                    logger.warn("IOException when trying to get remote http presentation.");
                    new Alert(Alert.AlertType.ERROR, "Couldn't get presentation from this URL.");
                    openOnlineXMLSelectorWindow(presentationURL);//Reopen the url entry window so that the user can try again.
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

    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }
}

