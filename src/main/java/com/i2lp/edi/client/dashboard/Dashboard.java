package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.Constants;
import com.i2lp.edi.client.editor.PresentationEditor;
import com.i2lp.edi.client.managers.*;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.utilities.ParserXML;
import com.i2lp.edi.client.utilities.PresSortKey;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
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
import org.apache.commons.lang3.StringUtils;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
    protected Scene scene;
    protected BorderPane border;
    private VBox rootBox;
    protected static Logger logger = LoggerFactory.getLogger(Dashboard.class);
    private EdiManager ediManager;
    protected PresentationManager presentationManager;
    protected Stage dashboardStage;
    protected Presentation selectedPres;
    private Classroom selectedClassroom;
    protected ArrayList<PresentationPanel> presentationPanels;
    private ArrayList<ClassroomPanel> classroomPanels;
    private ArrayList<SubjectPanel> subjectPanels;
    private FlowPane presentationPanelsFlowPane;
    private VBox subjectPanelsVBox;
    private ComboBox<PresSortKey> sortCombo;
    private Stage addToServerStage;
    private ArrayList<Presentation> availablePresentations;
    private ArrayList<Classroom> availableClassrooms;
    private boolean isWelcomeTextHidden = false;
    private DashboardState currentState;
    private Text noMatchesSubject, noMatchesPres;

    protected TextField searchField;
    protected Button showAllButton;
    protected ArrayList<Button> subjectButtons;

    @Override
    public void start(Stage dashboardStage) {
        this.dashboardStage = dashboardStage;
        //Initialise UI
        dashboardStage.setTitle("Edi");
        Image ediLogoSmall = new Image("file:projectResources/logos/ediLogo32x32.png");
        dashboardStage.getIcons().add(ediLogoSmall);
        dashboardStage.setOnCloseRequest(event -> Platform.exit());

        border = new BorderPane();
        rootBox = new VBox(addMenuBar(), border);
        scene = new Scene(rootBox, 1000, 600);
        presentationPanelsFlowPane = new FlowPane(Orientation.HORIZONTAL);
        subjectPanelsVBox = new VBox(5);  //TODO: move setup of global nodes to separate method
        subjectPanelsVBox.setAlignment(Pos.TOP_CENTER);
        noMatchesSubject = new Text("No matches found");
        noMatchesSubject.setFill(Color.GRAY);
        noMatchesSubject.getStyleClass().add("italic");
        noMatchesPres = new Text("No matches found");
        noMatchesPres.setFill(Color.GRAY);
        noMatchesPres.getStyleClass().add("italic");
        scene.getStylesheets().add("bootstrapfx.css");
        dashboardStage.setScene(scene);

        updateAvailablePresentations();

        goToState(DashboardState.MODULES);

        dashboardStage.show();
    }

    public void goToState(DashboardState state) {
        currentState = state;
        logger.info("State: " + currentState.name());

        if((state == DashboardState.CLASSROOM || state == DashboardState.MODULES) && searchField != null)
            searchField.setText("");

        displayBorderTop(state);
        displayBorderCenter(state);
        displayBorderLeft(state); //This has to be called after displayBorderCenter()
        displayBorderRight(state);
    }

    private void displayBorderTop(DashboardState state) {
        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(15, 12, 15, 12));
        topPanel.setSpacing(10);
        topPanel.setStyle("-fx-background-color: #34495e;");

        Text platformTitle = new Text("Integrated Interactive Learning Platform");
        platformTitle.getStyleClass().setAll("h3");
        platformTitle.setFill(Color.WHITESMOKE);

        switch (state) {
            case MODULES:
                topPanel.setAlignment(Pos.CENTER);
                topPanel.getChildren().add(platformTitle);
                break;

            case CLASSROOM:
            case SEARCH_CLASSROOM:
            case SEARCH_ALL:
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
                            launchPresentation(file.getPath());
                        } else logger.info("No presentation was selected");
                    });

                    MenuItem online = new MenuItem("Remotely (from HTTP)");
                    online.setOnAction(event1 -> logger.info("Not yet implemented")); //TODO: open presentation from web

                    menu.getItems().addAll(local, online);
                    menu.show(openPresButton, Side.BOTTOM, 0, 0);
                });

                Button addToServerButton = new Button("Add pres to server");
                addToServerButton.getStyleClass().setAll("btn", "btn-success");
                addToServerButton.setOnAction(event -> {
                    AtomicReference<File> xmlLocation = new AtomicReference<>(); //Store location of XML from filechooser, for upload to presentation after Thumbnail and CSS gen

                    if(addToServerStage != null) {
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

                    ComboBox<String> modulesCombo = new ComboBox<>();
                    modulesCombo.getItems().addAll("Module 1", "Module 2", "Module 3");
                    addToServerGridPane.add(modulesCombo, 0, 2);
                    GridPane.setConstraints(modulesCombo, 0, 2, 1, 1, HPos.CENTER, VPos.CENTER);

                    Button addButton = new Button("Add");
                    addButton.getStyleClass().setAll("btn", "btn-success");
                    addButton.setOnAction(event1 -> {
                        ediManager.getPresentationLibraryManager().uploadPresentation(xmlLocation.get().getAbsolutePath(), removeFileExtension(xmlLocation.get().getName()), 1); //TODO: Add proper ModuleID
                        addToServerStage.close();
                    });
                    addToServerGridPane.add(addButton, 0, 3);
                    GridPane.setConstraints(addButton, 0, 3, 1, 1, HPos.CENTER, VPos.CENTER);

                    addToServerStage.setScene(addToServerScene);
                    addToServerStage.show();
                });

                topPanel.getChildren().addAll(openPresButton, addToServerButton, platformTitle);
                break;

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
            case MODULES:
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
                break;

            case SEARCH_ALL:
                Text modulesText = new Text("Modules");
                modulesText.getStyleClass().setAll("h4");

                Text presentationsText = new Text("Presentations");
                presentationsText.getStyleClass().setAll("h4");
                VBox.setMargin(presentationsText, new Insets(10, 0, 0, 0));

//                subjectPanelsVBox.getChildren().addListener((ListChangeListener<? super Node>) observable -> {
//                    if(subjectPanelsVBox.getChildren().size() == 0 && currentState == DashboardState.SEARCH_ALL) {
//                        displayBorderCenter(currentState);
//                    }
//                });
//                presentationPanelsFlowPane.getChildren().addListener((ListChangeListener<? super Node>) observable -> {
//                    if(presentationPanelsFlowPane.getChildren().size() == 0 && currentState == DashboardState.SEARCH_ALL) {
//                        displayBorderCenter(currentState);
//                    }
//                });

                vbox.getChildren().add(modulesText);
                vbox.getChildren().add(subjectPanelsVBox);
                vbox.getChildren().add(presentationsText);
                vbox.getChildren().add(presentationPanelsFlowPane);


                ScrollPane searchScrollPane = new ScrollPane();
                searchScrollPane.setContent(vbox);
                searchScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
                searchScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
                searchScrollPane.setFitToWidth(true);
                searchScrollPane.getStyleClass().add("edge-to-edge");

                border.setCenter(searchScrollPane);
                break;

            case CLASSROOM:
            case SEARCH_CLASSROOM:
                Button backButton = new Button("Back to module selection");
                backButton.getStyleClass().setAll("btn", "btn-default");
                backButton.setOnAction(event -> goToState(DashboardState.MODULES));
                Text moduleText = new Text(selectedClassroom.getModuleName());
                moduleText.getStyleClass().setAll("h4");
                Region dummy = new Region();
                backButton.widthProperty().addListener(observable -> dummy.setPrefWidth(backButton.getWidth()));
                BorderPane borderPane = new BorderPane();
                borderPane.setPadding(new Insets(5, 0, 5, 0));
                borderPane.setLeft(backButton);
                borderPane.setRight(dummy);
                borderPane.setCenter(moduleText);

                filterPresentationPanels(selectedClassroom);

                vbox.getChildren().addAll(borderPane, presentationsScrollPane);
                border.setCenter(vbox);
                break;

            default:
                //Do nothing
        }
    }

    private void displayBorderLeft(DashboardState state) {
        VBox controlsVBox = new VBox(8);
        controlsVBox.setPadding(new Insets(10));

        switch (state) {
            case MODULES:
            case CLASSROOM:
            case SEARCH_CLASSROOM:
            case SEARCH_ALL:
                Panel searchPanel = new Panel("Search");
                controlsVBox.getChildren().add(searchPanel);
                searchPanel.getStyleClass().add("panel-primary");

                if(searchField == null) {
                    searchField = new TextField();
                    searchField.textProperty().addListener(observable -> search(searchField.getText()));
                }

                searchPanel.setBody(searchField);

                VBox subjectsVBox = new VBox();
                subjectsVBox.setPadding(new Insets(3, 0, 3, 0));
                subjectsVBox.setSpacing(3);
                subjectsVBox.setStyle("-fx-background-color: #ffffff;");

                Label filterBySubjectLabel = new Label("Filter by subject:");
                subjectsVBox.getChildren().add(filterBySubjectLabel);

                showAllButton = new Button("Show all");
                showAllButton.getStyleClass().setAll("btn", "btn-success");
                showAllButton.setOnAction(event -> filterPresentationPanels(null));
                subjectsVBox.getChildren().add(showAllButton);

                ArrayList<String> subjectArray = new ArrayList<>();
                subjectButtons = new ArrayList<>();

                for (PresentationPanel panel : presentationPanels) {
                    String subject = panel.getPresentationSubject();
                    if(subject != null && !subjectArray.contains(subject)) {
                        subjectArray.add(subject);

                        Button subjectButton = new Button(subject);
                        subjectButton.getStyleClass().setAll("btn", "btn-success");
                        subjectButton.setOnAction(event -> filterBySubject(subjectButton.getText()));
                        subjectButtons.add(subjectButton);
                        subjectsVBox.getChildren().add(subjectButton);
                    }
                }

                //Create Panel for subject filters
                Panel subjectsPanel = new Panel("My subjects");
                subjectsPanel.getStyleClass().add("panel-primary");
                subjectsPanel.setBody(subjectsVBox);
                VBox.setMargin(subjectsPanel, new Insets(0, 0, 0, 0));
                controlsVBox.getChildren().add(subjectsPanel);

                Panel sortPanel = new Panel("Sort by");
                sortPanel.getStyleClass().add("panel-primary");
                VBox sortVBox = new VBox();
                sortCombo = new ComboBox<>();
                PresSortKey.copyAllToList(sortCombo.getItems());
                sortCombo.setOnAction(event -> sortBy(sortCombo.getValue()));
                sortCombo.setValue(sortCombo.getItems().get(0));
                sortBy(sortCombo.getItems().get(0));
                sortPanel.setBody(sortCombo);
                controlsVBox.getChildren().add(sortPanel);
                break;

            default:
                //Do nothing
        }

        border.setLeft(controlsVBox);
    }

    private void displayBorderRight(DashboardState state) {
        switch (state) {
            case MODULES:
            case SEARCH_ALL:
                border.setRight(null);
                break;

            case CLASSROOM:
            case SEARCH_CLASSROOM:
                if(selectedPres != null) {
                    VBox parentBox = new VBox(2);
                    parentBox.setPadding(new Insets(5));

                    for(int i = 0; i < selectedPres.getMaxSlideNumber(); i++) {
                        VBox vbox = new VBox(3);
                        vbox.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderStroke.DEFAULT_WIDTHS)));
                        vbox.setAlignment(Pos.CENTER);
                        vbox.setPadding(new Insets(5));

                        vbox.getChildren().add(selectedPres.getSlidePreview(i, SLIDE_PREVIEW_WIDTH));
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

    private void setupClassroomPanels() {
        if(classroomPanels == null)
            classroomPanels = new ArrayList<>();
        else
            classroomPanels.clear();

        if(subjectPanels == null)
            subjectPanels = new ArrayList<>();
        else
            subjectPanels.clear();

        ArrayList<String> subjects = new ArrayList<>();

        for(Classroom classroom : availableClassrooms) {
            SubjectPanel subjectPanel = new SubjectPanel("Empty", null);
            String subject = classroom.getSubject();

            if(!subjects.contains(subject)) {
                subjects.add(subject);
                subjectPanel = new SubjectPanel(subject, subjectPanelsVBox);
                subjectPanels.add(subjectPanel);
            } else {
                for(Node node : subjectPanelsVBox.getChildren()) {
                    if(((SubjectPanel)node).getSubject().equals(subject)) {
                        subjectPanel = (SubjectPanel) node;
                        break;
                    }
                }
            }

            ClassroomPanel classroomPanel = new ClassroomPanel(subjectPanel.getSubjectPanelsHBox(), classroom);
            classroomPanel.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (classroomPanel.isSelected()) {
                        goToState(DashboardState.CLASSROOM);
                    } else {
                        selectPreviewPanel(classroomPanel);
                    }
                }
            });
            classroomPanels.add(classroomPanel);
        }
    }

    private void addUserPane() {
        //TODO: add a pane with user info to the top of the left panel
    }

    private void updateAvailablePresentations() {
        if(availablePresentations == null)
            availablePresentations = new ArrayList<>();
        else
            availablePresentations.clear();

        if(availableClassrooms == null)
            availableClassrooms = new ArrayList<>();
        else
            availableClassrooms.clear();

        ArrayList<String> modules = new ArrayList<>();

        for(int i=0; i < ediManager.getPresentationLibraryManager().getLocalPresentationList().size(); i++) {
            String moduleName = ediManager.getPresentationLibraryManager().getLocalPresentationList().get(i).getModuleName();
            String presentationDocumentID = ediManager.getPresentationLibraryManager().getLocalPresentationList().get(i).getDocumentID();
            String presentationPath = PRESENTATIONS_PATH + presentationDocumentID + File.separator + presentationDocumentID + ".xml";
            ParserXML parser = new ParserXML(presentationPath);
            Presentation presentation = parser.parsePresentation();

            if(!modules.contains(moduleName)) {
                modules.add(moduleName);
                availableClassrooms.add(new Classroom(moduleName, presentation));
            } else {
                for(Classroom classroom : availableClassrooms) {
                    if (classroom.getModuleName().equals(moduleName)) {
                        classroom.addPresentation(presentation);
                        break;
                    }
                }
            }

            availablePresentations.add(presentation);
        }

        setupClassroomPanels();
        setupPresentationPanels();
    }

    public void setupPresentationPanels() {
        if(presentationPanels == null)
            presentationPanels = new ArrayList<>();
        else
            presentationPanels.clear();

        for (Presentation presentation : availablePresentations) {
            PresentationPanel previewPanel = new PresentationPanel(presentationPanelsFlowPane, presentation);
            previewPanel.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (previewPanel.isSelected()) {
                        launchPresentation(previewPanel.getPresentationPath());
                    } else {
                        selectPreviewPanel(previewPanel);
                    }
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    selectPreviewPanel(previewPanel);

                    ContextMenu cMenu = new ContextMenu();

                    MenuItem open = new MenuItem("Open");
                    open.setOnAction(openEvent -> launchPresentation(previewPanel.getPresentationPath()));
                    cMenu.getItems().add(open);

                    if (this instanceof TeacherDashboard) {
                        MenuItem edit = new MenuItem("Edit");
                        edit.setOnAction(editEvent -> showPresentationEditor(previewPanel.getPresentationPath()));
                        cMenu.getItems().add(edit);

                        MenuItem schedule = new MenuItem("Schedule");
                        schedule.setOnAction(scheduleEvent -> showScheduler(event.getScreenX(), event.getScreenY()));
                        cMenu.getItems().add(schedule);

                        MenuItem delete = new MenuItem("Delete");
                        delete.setOnAction(deleteEvent -> deletePresentation(previewPanel));
                        cMenu.getItems().add(delete);

                        MenuItem print = new MenuItem("Print");
                        print.setOnAction(printEvent-> printPresentation(previewPanel.getPresentationPath()));
                        cMenu.getItems().add(print);

                        MenuItem report = new MenuItem("Report");
                        report.setOnAction(reportEvent -> showReport(previewPanel.getId()));
                        cMenu.getItems().add(report);

                    }
                    cMenu.show(dashboardStage, event.getScreenX(), event.getScreenY());
                }
            });
            presentationPanels.add(previewPanel);
        }

        if(sortCombo != null)
            sortBy(sortCombo.getValue());
    }

    private void selectPreviewPanel(PreviewPanel previewPanel) {

        if (previewPanel instanceof PresentationPanel) {
            for (PresentationPanel panel : presentationPanels) {
                panel.setSelected(false);
            }
            selectedPres = ((PresentationPanel) previewPanel).getPresentation();
            displayBorderRight(DashboardState.CLASSROOM);
        } else if(previewPanel instanceof ClassroomPanel) {
            for (ClassroomPanel panel : classroomPanels) {
                panel.setSelected(false);
            }
            selectedClassroom = ((ClassroomPanel) previewPanel).getClassroom();
        }

        previewPanel.setSelected(true);
    }

    private void deletePresentation(PresentationPanel previewPanel) {
        Presentation presentationToDelete = previewPanel.getPresentation();

        //Try to remove the presentation from the server
        boolean successful_removal = ediManager.getPresentationLibraryManager().removePresentation(25, 1); //TODO we needs to store this somehow in our java @Amrik

        if(successful_removal) {
            updateAvailablePresentations();
            presentationPanelsFlowPane.getChildren().remove(previewPanel);
        }
        else logger.warn("Presentation Could not be removed"); //TODO improve this
    }

    private MenuBar addMenuBar() {
        MenuBar menuBar = new MenuBar();
        //Due to travis fails, this couldn't be done in the constructor:

        Menu fileMenu = new Menu("File");

        Menu editMenu = new Menu("Edit");

        Menu viewMenu = new Menu("View");
        MenuItem showWelcomeMessage = new MenuItem("Show Welcome Message");
        showWelcomeMessage.setOnAction(event -> {
            isWelcomeTextHidden = false;
            goToState(DashboardState.MODULES);
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
     * @param path Path to presentation
     * @author Amrik Sadhra
     */
    private void launchPresentation(String path) {

        if(presentationManager != null)
            presentationManager.close();

        if (this instanceof StudentDashboard) {
            presentationManager = new PresentationManagerStudent();
        } else if (this instanceof TeacherDashboard) {
            presentationManager = new PresentationManagerTeacher();
        }
        presentationManager.openPresentation(path,false);
    }

    private void search(String text) { //TODO: show author & tags when found?
        if(text.equals("")) {
            if(currentState == DashboardState.SEARCH_CLASSROOM) {
                goToState(DashboardState.CLASSROOM);
            } else if (currentState == DashboardState.SEARCH_ALL) {
                goToState(DashboardState.MODULES);
            }
        } else {
            if(currentState == DashboardState.CLASSROOM) {
                goToState(DashboardState.SEARCH_CLASSROOM);
            } else if(currentState == DashboardState.MODULES) {
                goToState(DashboardState.SEARCH_ALL);
            }
        }

        for(PresentationPanel panel : presentationPanels) {
            panel.search(text);
        }

        for(ClassroomPanel panel : classroomPanels) {
            panel.search(text);
        }

        if(presentationPanelsFlowPane.getChildren().contains(noMatchesPres)) {
            presentationPanelsFlowPane.getChildren().remove(noMatchesPres);
        }

        if(subjectPanelsVBox.getChildren().contains(noMatchesSubject)) {
            subjectPanelsVBox.getChildren().remove(noMatchesSubject);
        }

        if(presentationPanelsFlowPane.getChildren().size() == 0) {
            presentationPanelsFlowPane.getChildren().add(noMatchesPres);
        }

        if(subjectPanelsVBox.getChildren().size() == 0) {
            subjectPanelsVBox.getChildren().add(noMatchesSubject);
        }
    }

    private void filterBySubject(String subject) {
        for (PresentationPanel panel : presentationPanels) {
            if (!subject.equals(panel.getPresentationSubject()) && !panel.isHidden())
                panel.setFiltered(true);
            else if (subject.equals(panel.getPresentationSubject()) && panel.isHidden())
                panel.setFiltered(false);
        }
    }

    private void filterPresentationPanels(Classroom classroom) {
        for (PresentationPanel panel : presentationPanels) {
            if (classroom == null) {
                panel.setFiltered(false);
            } else {
                Presentation p = panel.getPresentation();
                Classroom c = p.getClassroom();
                if(c == classroom) {
                    panel.setFiltered(false);
                } else {
                    panel.setFiltered(true);
                }
            }
        }
    }

    private void sortBy(PresSortKey sortKey) {
        presentationPanels.sort((p1, p2) -> sortKey.compare(p1.getPresentation(), p2.getPresentation()));

        presentationPanelsFlowPane.getChildren().clear();
        for (PresentationPanel panel : presentationPanels) {
            if (!panel.isHidden())
                presentationPanelsFlowPane.getChildren().add(panel);
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

    private void printPresentation(String presentationPath) {
        ThumbnailGenerationManager.generateSlideThumbnails(presentationPath,true);
    }

    private void showReport(String presentationID){ReportManager.openReportPanel(presentationID);}

    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }
}

