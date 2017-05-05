package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.Constants;
import com.i2lp.edi.client.editor.PresentationEditor;
import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.managers.PresentationController;
import com.i2lp.edi.client.managers.ThumbnailGenerationController;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.presentationViewer.StudentPresentationController;
import com.i2lp.edi.client.presentationViewer.TeacherPresentationController;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
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

import static com.i2lp.edi.client.Constants.PRESENTATIONS_PATH;
import static javafx.scene.layout.BorderPane.setAlignment;


/**
 * Created by amriksadhra on 24/01/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused", "Duplicates"})
public abstract class Dashboard extends Application {
    protected Scene scene;
    protected BorderPane border;
    protected Presentation myPresentationElement;
    protected static Logger logger = LoggerFactory.getLogger(Dashboard.class);
    private EdiManager ediManager;
    protected PresentationController presentationController;
    protected Stage dashboardStage;
    protected String selectedPresID;
    private ArrayList<PresentationPreviewPanel> previewPanels;
    private FlowPane presentationPreviewsFlowPane;

    @Override
    public void start(Stage dashboardStage) {
        this.dashboardStage = dashboardStage;
        //Initialise UI
        dashboardStage.setTitle("Edi");
        Image ediLogoSmall = new Image("file:projectResources/logos/ediLogo32x32.png");
        dashboardStage.getIcons().add(ediLogoSmall);

        border = new BorderPane();
        scene = new Scene(border, 1000, 600);
        scene.getStylesheets().add("bootstrapfx.css");
        dashboardStage.setScene(scene);

        previewPanels = new ArrayList<>();

        border.setTop(addBorderTop());
        border.setCenter(addBorderCenter());

        //The following code has to be placed between addBorderCenter() and addBorderLeft()
        int numOfPresentations = ediManager.getPresentationManager().getLocalPresentationList().size();

        for (int i = 0;i < numOfPresentations; i++) {
            String presentationDocumentID = ediManager.getPresentationManager().getLocalPresentationList().get(i).getDocumentID();

            PresentationPreviewPanel previewPanel = new PresentationPreviewPanel(presentationPreviewsFlowPane, PRESENTATIONS_PATH + presentationDocumentID + File.separator + presentationDocumentID + ".xml");
            previewPanel.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if(event.getButton() == MouseButton.PRIMARY) {
                    if (previewPanel.isSelected()) {
                        launchPresentation(previewPanel.getPresentationPath());
                    } else {
                        for (int j = 0; j < numOfPresentations; j++)
                            previewPanels.get(j).setSelected(false);

                        previewPanel.setSelected(true);
                        selectedPresID = previewPanel.getPresentation().getDocumentID();
                        border.setRight(addBorderRight());
                    }
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    ContextMenu cMenu = new ContextMenu();

                    MenuItem open = new MenuItem("Open");
                    open.setOnAction(openEvent -> launchPresentation(previewPanel.getPresentationPath()));
                    cMenu.getItems().add(open);

                    if(this instanceof  TeacherDashboard) {
                        MenuItem edit = new MenuItem("Edit");
                        edit.setOnAction(editEvent -> showPresentationEditor(previewPanel.getPresentationPath()));
                        cMenu.getItems().add(edit);

                        MenuItem schedule = new MenuItem("Schedule");
                        schedule.setOnAction(scheduleEvent -> showScheduler(event.getScreenX(), event.getScreenY()));
                        cMenu.getItems().add(schedule);
                    }
                    cMenu.show(dashboardStage, event.getScreenX(), event.getScreenY());
                }

            });
            previewPanels.add(previewPanel);
        }

        border.setRight(addBorderRight());
        border.setLeft(addBorderLeft());

        dashboardStage.show();
    }

    private ScrollPane addBorderCenter() {
        presentationPreviewsFlowPane = new FlowPane(Orientation.HORIZONTAL);
        presentationPreviewsFlowPane.setPadding(new Insets(5, 0, 5, 0));
        presentationPreviewsFlowPane.setVgap(4);
        presentationPreviewsFlowPane.setHgap(4);
        presentationPreviewsFlowPane.setStyle("-fx-background-color: #ffffff;");

        showAllPreviewPanels();

        ScrollPane centerPane = new ScrollPane();
        centerPane.setContent(presentationPreviewsFlowPane);
        centerPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        centerPane.setFitToWidth(true);

        return centerPane;
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
        aboutStackPane.getChildren().add(backgroundRegion);
        BorderPane aboutBorder = new BorderPane();
        aboutStackPane.getChildren().add(aboutBorder);
        ImageView ediLogoImageView = new ImageView(new Image("file:projectResources/logos/ediLogo64x64.png"));
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
        if (this instanceof StudentDashboard) {
            presentationController = new StudentPresentationController();
        } else if (this instanceof TeacherDashboard) {
            presentationController = new TeacherPresentationController();
        }
        presentationController.openPresentation(path);
    }

    private HBox addTopPanel() {
        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(15, 12, 15, 12));
        topPanel.setSpacing(10);
        topPanel.setStyle("-fx-background-color: #34495e;");

        Button createPresButton = new Button("TEST: Generate Thumbnails"); //TODO remove
        createPresButton.getStyleClass().setAll("btn", "btn-success");
        createPresButton.setOnAction(event -> ThumbnailGenerationController.generateSlideThumbnails("file:projectResources/sampleFiles/sampleXmlSimple.xml"));

        final FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter xmlExtensionFilter =
                new FileChooser.ExtensionFilter("XML Presentations (*.XML)", "*.xml", "*.XML");
        fileChooser.getExtensionFilters().add(xmlExtensionFilter);
        fileChooser.setSelectedExtensionFilter(xmlExtensionFilter);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle("Load Presentation");

        Button loadPresButton = new Button("Load Presentation");
        loadPresButton.getStyleClass().setAll("btn", "btn-default");
        loadPresButton.setOnAction(event -> {
            Node source = (Node) event.getSource();
            Window stage = source.getScene().getWindow();


            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                launchPresentation(file.getPath());
                //TODO add to library somehow?
            } else logger.info("No presentation was selected");
        });

        Button addToServerButton = new Button("Add presentation to server");
        addToServerButton.getStyleClass().setAll("btn", "btn-success");
        addToServerButton.setOnAction(event -> {
            Stage addToServerStage = new Stage();
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
                File file = fileChooser.showOpenDialog(dashboardStage);
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

        topPanel.getChildren().addAll(createPresButton, loadPresButton, addToServerButton, platformTitle);

        return topPanel;
    }

    private VBox addBorderLeft() {
        VBox controlsVBox = new VBox(8);
        controlsVBox.setPadding(new Insets(10));

        Panel searchPanel = new Panel("Search");
        controlsVBox.getChildren().add(searchPanel);
        searchPanel.getStyleClass().add("panel-primary");
        TextField searchField = new TextField();
        searchField.setOnAction(event -> search(searchField.getText()));
        searchField.textProperty().addListener(observable -> search(searchField.getText()));
        searchPanel.setBody(searchField);

        VBox subjectsVBox = new VBox();
        subjectsVBox.setPadding(new Insets(3, 0, 3, 0));
        subjectsVBox.setSpacing(3);
        subjectsVBox.setStyle("-fx-background-color: #ffffff;");

        Label filterBySubjectLabel = new Label("Filter by subject:");
        subjectsVBox.getChildren().add(filterBySubjectLabel);

        Button showAllButton = new Button("Show all");
        showAllButton.getStyleClass().setAll("btn", "btn-success");
        showAllButton.setOnAction(event -> showAllPreviewPanels());
        subjectsVBox.getChildren().add(showAllButton);

        Button subjectButton1 = new Button("Subject 0");
        subjectButton1.getStyleClass().setAll("btn", "btn-success");
        subjectButton1.setOnAction(event -> filterBySubject(subjectButton1.getText()));
        subjectsVBox.getChildren().add(subjectButton1);

        Button subjectButton2 = new Button("Subject 1");
        subjectButton2.getStyleClass().setAll("btn", "btn-success");
        subjectButton2.setOnAction(event -> filterBySubject(subjectButton2.getText()));
        subjectsVBox.getChildren().add(subjectButton2);

        Button subjectButton3 = new Button("Subject 2");
        subjectButton3.getStyleClass().setAll("btn", "btn-success");
        subjectButton3.setOnAction(event -> filterBySubject(subjectButton3.getText()));
        subjectsVBox.getChildren().add(subjectButton3);

        //Create Panel for subject filters
        Panel subjectsPanel = new Panel("My subjects");
        subjectsPanel.getStyleClass().add("panel-primary");
        subjectsPanel.setBody(subjectsVBox);
        VBox.setMargin(subjectsPanel, new Insets(0, 0, 0, 0));
        controlsVBox.getChildren().add(subjectsPanel);

        Panel sortPanel = new Panel("Sort by");
        sortPanel.getStyleClass().add("panel-primary");
        VBox sortVBox = new VBox();
        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Name A-Z", "Name Z-A", "Subject A-Z", "Subject Z-A");
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
            if(!panel.getPresentation().getDocumentID().contains(text) &&
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
        ScrollPane scroll = new ScrollPane();

        FlowPane flow = new FlowPane();
        flow.setPadding(new Insets(5, 0, 5, 0));
        flow.setVgap(4);
        flow.setHgap(4);
        flow.setPrefWrapLength(170); // preferred width allows for two columns
        flow.setStyle("-fx-background-color: #ffffff;");

        int numSlides = 20; //TODO: Obtain number of slides from XML

        VBox[] slides = new VBox[numSlides];

        for (int i = 0; i < numSlides; i++) {
            slides[i] = new VBox(3);
            slides[i].setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderStroke.DEFAULT_WIDTHS)));
            slides[i].setAlignment(Pos.CENTER);
            slides[i].setPadding(new Insets(5));

            ImageView preview;
            File thumbnailFile = new File(PRESENTATIONS_PATH + "Thumbnails/" + selectedPresID + "_slide" + i + "_thumbnail.png");

            if(thumbnailFile.exists()) {
                try {
                    preview = new ImageView("file:" + PRESENTATIONS_PATH + "Thumbnails/" + selectedPresID + "_slide" + i + "_thumbnail.png");
                } catch (NullPointerException | IllegalArgumentException e) {
                    logger.debug("Couldn't open thumbnail" + thumbnailFile.toString());
                    preview = new ImageView("file:projectResources/icons/emptyThumbnail.png");
                }
            } else {
                preview = new ImageView("file:projectResources/icons/emptyThumbnail.png");
            }

            preview.setFitWidth(170);
            preview.setPreserveRatio(true);
            preview.setSmooth(true);
            preview.setCache(true);

            slides[i].getChildren().add(preview);
            slides[i].getChildren().add(new Label(Integer.toString(i)));
            flow.getChildren().add(slides[i]);
            FlowPane.setMargin(slides[i], new Insets(0, 20, 0, 5));
        }

        scroll.setContent(flow);

        return scroll;
    }

    private void showAllPreviewPanels() {
        presentationPreviewsFlowPane.getChildren().clear();

        for (PresentationPreviewPanel panel : previewPanels)
            panel.setHidden(false);
    }

    //TODO: Will this maintain sorting order?
    private void filterBySubject(String subject) {
        for(PresentationPreviewPanel panel : previewPanels) {
            if(!subject.equals(panel.getPresentationSubject()) && !panel.isHidden())
                panel.setHidden(true);
            else if(subject.equals(panel.getPresentationSubject()) && panel.isHidden())
                panel.setHidden(false);
        }
    }

    //The sorting method is pretty ghetto for now, will have to refactor depending on what sorting key we'll allow
    private void sortBy(String sortKey) {
        previewPanels.sort((p1, p2) -> {
            switch (sortKey) {
                case "Name A-Z":
                    return p1.getPresentation().getDocumentID().compareTo(p2.getPresentation().getDocumentID());
                case "Name Z-A":
                    return -p1.getPresentation().getDocumentID().compareTo(p2.getPresentation().getDocumentID());
                case "Subject A-Z":
                    return p1.getPresentationSubject().compareTo(p2.getPresentationSubject());
                case "Subject Z-A":
                    return -p1.getPresentationSubject().compareTo(p2.getPresentationSubject());
                default:
                    return 0;
            }
        });

        presentationPreviewsFlowPane.getChildren().clear();
        for(PresentationPreviewPanel panel : previewPanels) {
            if(!panel.isHidden())
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

    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }
}

