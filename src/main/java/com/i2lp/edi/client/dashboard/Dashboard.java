package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.Constants;
import com.i2lp.edi.client.editor.PresentationEditor;
import com.i2lp.edi.client.managers.*;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.utilities.PresSortKey;
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
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.i2lp.edi.client.Constants.PRESENTATIONS_PATH;
import static com.i2lp.edi.client.utilities.Utilities.removeFileExtension;
import static javafx.scene.layout.BorderPane.setAlignment;


/**
 * Created by amriksadhra on 24/01/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused", "Duplicates"})
public abstract class Dashboard extends Application {
    protected Scene scene;
    protected BorderPane border;
    protected static Logger logger = LoggerFactory.getLogger(Dashboard.class);
    private EdiManager ediManager;
    protected PresentationManager presentationManager;
    protected Stage dashboardStage;
    protected Presentation selectedPres;
    protected ArrayList<PresentationPreviewPanel> previewPanels;
    private FlowPane presentationPreviewsFlowPane;
    private ComboBox<PresSortKey> sortCombo;
    private Stage addToServerStage;

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
        scene = new Scene(border, 1000, 600);
        scene.getStylesheets().add("bootstrapfx.css");
        dashboardStage.setScene(scene);

        border.setTop(addBorderTop());
        border.setCenter(addBorderCenter());
        updatePresentationPreviews(); //This has to be placed between addBorderCenter() and addBorderLeft()
        showAllPreviewPanels();
        border.setLeft(addBorderLeft());

        dashboardStage.show();
    }

    public void updatePresentationPreviews() {
        if(previewPanels == null)
            previewPanels = new ArrayList<>();
        else
            previewPanels.clear();

        presentationPreviewsFlowPane.getChildren().clear();

        int numOfPresentations = ediManager.getPresentationLibraryManager().getLocalPresentationList().size();

        for (int i = 0; i < numOfPresentations; i++) {
            String presentationDocumentID = ediManager.getPresentationLibraryManager().getLocalPresentationList().get(i).getDocumentID();

            PresentationPreviewPanel previewPanel = new PresentationPreviewPanel(presentationPreviewsFlowPane, PRESENTATIONS_PATH + presentationDocumentID + File.separator + presentationDocumentID + ".xml");
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

                    }
                    cMenu.show(dashboardStage, event.getScreenX(), event.getScreenY());
                }
            });
            previewPanels.add(previewPanel);
        }

        if(sortCombo != null)
            sortBy(sortCombo.getValue());
    }

    private void selectPreviewPanel(PresentationPreviewPanel previewPanel) {
        for (int j = 0; j < previewPanels.size(); j++)
            previewPanels.get(j).setSelected(false);
        previewPanel.setSelected(true);
        selectedPres = previewPanel.getPresentation();
        border.setRight(addBorderRight());
    }

    private void deletePresentation(PresentationPreviewPanel previewPanel) {
        Presentation presentationToDelete = previewPanel.getPresentation();

        //TODO we possibly need to have module NAME and differentiate between presentation ID and presentationTITLE @Amrik
        //Try to remove the presentation from the server
        boolean successful_removal = ediManager.getPresentationLibraryManager().removePresentation(25, 1); //TODO we needs to store this somehow in our java @Amrik

        if(successful_removal) {
            previewPanels.remove(previewPanel);
            presentationPreviewsFlowPane.getChildren().remove(previewPanel);
        }
        else logger.warn("Presentation Could not be removed"); //TODO improve this
    }

    private ScrollPane addBorderCenter() {
        ScrollPane scrollPane = new ScrollPane();

        presentationPreviewsFlowPane = new FlowPane(Orientation.HORIZONTAL);
        presentationPreviewsFlowPane.setPadding(new Insets(5, 0, 5, 0));
        presentationPreviewsFlowPane.setVgap(4);
        presentationPreviewsFlowPane.setHgap(4);
        presentationPreviewsFlowPane.setStyle("-fx-background-color: #ffffff;");
        presentationPreviewsFlowPane.setAlignment(Pos.TOP_CENTER);

        scrollPane.setContent(presentationPreviewsFlowPane);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);

        return scrollPane;
    }

    private BorderPane addBorderTop() {
        BorderPane toolBarAndTopPanel = new BorderPane();
        toolBarAndTopPanel.setTop(addMenuBar());
        toolBarAndTopPanel.setBottom(addTopPanel());

        return toolBarAndTopPanel;
    }

    private MenuBar addMenuBar() {
        MenuBar menuBar = new MenuBar();
        //Due to travis fails, this couldn't be done in the constructor:

        Menu fileMenu = new Menu("File");
        Menu editMenu = new Menu("Edit");
        Menu helpMenu = new Menu("Help");
        MenuItem aboutMenuItem = new MenuItem("About");
        aboutMenuItem.setOnAction(event -> showAboutWindow());
        helpMenu.getItems().add(aboutMenuItem);
        menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);

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

    private HBox addTopPanel() {
        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(15, 12, 15, 12));
        topPanel.setSpacing(10);
        topPanel.setStyle("-fx-background-color: #34495e;");

        final FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter xmlExtensionFilter =
                new FileChooser.ExtensionFilter("XML Presentations (*.XML)", "*.xml", "*.XML");
        fileChooser.getExtensionFilters().add(xmlExtensionFilter);
        fileChooser.setSelectedExtensionFilter(xmlExtensionFilter);
        fileChooser.setInitialDirectory(new File("projectResources/sampleFiles/xml"));
//        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))); //TODO reinstate when tested
        fileChooser.setTitle("Add Presentation");

        Button loadPresButton = new Button("Add Presentation");
        loadPresButton.getStyleClass().setAll("btn", "btn-default");
        loadPresButton.setOnAction(event -> {
            Node source = (Node) event.getSource();
            Window stage = source.getScene().getWindow();


            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                launchPresentation(file.getPath());
            } else logger.info("No presentation was selected");
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

        Text platformTitle = new Text("     Integrated Interactive Learning Platform");
        platformTitle.getStyleClass().setAll("h3");
        platformTitle.setFill(Color.WHITESMOKE);

        topPanel.getChildren().addAll(loadPresButton, addToServerButton, platformTitle);

        return topPanel;
    }

    private VBox addBorderLeft() {
        VBox controlsVBox = new VBox(8);
        controlsVBox.setPadding(new Insets(10));

        Panel searchPanel = new Panel("Search");
        controlsVBox.getChildren().add(searchPanel);
        searchPanel.getStyleClass().add("panel-primary");
        searchField = new TextField();
        searchField.setOnAction(event -> search(searchField.getText()));
        searchField.textProperty().addListener(observable -> search(searchField.getText()));
        searchPanel.setBody(searchField);

        VBox subjectsVBox = new VBox();
        subjectsVBox.setPadding(new Insets(3, 0, 3, 0));
        subjectsVBox.setSpacing(3);
        subjectsVBox.setStyle("-fx-background-color: #ffffff;");

        Label filterBySubjectLabel = new Label("Filter by subject:");
        subjectsVBox.getChildren().add(filterBySubjectLabel);

        showAllButton = new Button("Show all");
        showAllButton.getStyleClass().setAll("btn", "btn-success");
        showAllButton.setOnAction(event -> showAllPreviewPanels());
        subjectsVBox.getChildren().add(showAllButton);

        ArrayList<String> subjectArray = new ArrayList<>();
        subjectButtons = new ArrayList<>();

        for (PresentationPreviewPanel panel : previewPanels) {
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

        return controlsVBox;
    }

    private void search(String text) {
        logger.info("Searching for " + text);

        for (PresentationPreviewPanel panel : previewPanels) {
            if (!panel.getPresentation().getDocumentID().contains(text) &&
                    !panel.getPresentation().getTags().contains(text) &&
                    !panel.getPresentation().getAuthor().contains(text) &&
                    !panel.isHidden()) {
                panel.setHidden(true);
            } else if ((panel.getPresentation().getDocumentID().contains(text) ||
                    panel.getPresentation().getTags().contains(text) ||
                    panel.getPresentation().getAuthor().contains(text)) && panel.isHidden()) {
                panel.setHidden(false);
            }
        }
    }

    private ScrollPane addBorderRight() {
        FlowPane flow = new FlowPane();
        flow.setPadding(new Insets(5, 0, 5, 0));
        flow.setVgap(4);
        flow.setHgap(4);
        flow.setPrefWrapLength(170); // preferred width allows for two columns
        flow.setStyle("-fx-background-color: #ffffff;");

        for(int i = 0; i < selectedPres.getMaxSlideNumber(); i++) {
            VBox vbox = new VBox(3);
            vbox.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderStroke.DEFAULT_WIDTHS)));
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(5));

            vbox.getChildren().add(selectedPres.getSlidePreview(i, 170)); //TODO: Add constant for width
            vbox.getChildren().add(new Label(Integer.toString(i + 1)));
            flow.getChildren().add(vbox);
            FlowPane.setMargin(vbox, new Insets(0, 20, 0, 5));
        }

        ScrollPane scroll = new ScrollPane();
        scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setContent(flow);

        return scroll;
    }

    private void showAllPreviewPanels() {
        presentationPreviewsFlowPane.getChildren().clear();

        for (PresentationPreviewPanel panel : previewPanels)
            panel.setHidden(false);
    }

    private void filterBySubject(String subject) {
        for (PresentationPreviewPanel panel : previewPanels) {
            if (!subject.equals(panel.getPresentationSubject()) && !panel.isHidden())
                panel.setHidden(true);
            else if (subject.equals(panel.getPresentationSubject()) && panel.isHidden())
                panel.setHidden(false);
        }
    }

    private void sortBy(PresSortKey sortKey) {
        previewPanels.sort((p1, p2) -> sortKey.compare(p1.getPresentation(), p2.getPresentation()));

        presentationPreviewsFlowPane.getChildren().clear();
        for (PresentationPreviewPanel panel : previewPanels) {
            if (!panel.isHidden())
                presentationPreviewsFlowPane.getChildren().add(panel);
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

    private void printPresentation(String presentationPath){
        ThumbnailGenerationManager.generateSlideThumbnails(presentationPath,true);
    }

    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }
}

