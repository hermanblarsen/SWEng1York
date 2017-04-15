package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.Constants;
import com.i2lp.edi.client.editor.PresentationEditor;
import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.managers.PresentationManager;
import com.i2lp.edi.client.managers.ThumbnailGenerationManager;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.presentationViewer.StudentPresentationManager;
import com.i2lp.edi.client.presentationViewer.TeacherPresentationManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
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
import java.util.ArrayList;


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
    protected PresentationManager presentationManager;
    protected Stage primaryStage;
    protected String selectedPresID;
    private ArrayList<PresentationPreviewPanel> previewPanels;
    private FlowPane presentationPreviewsFlowPane;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        //Initialise UI
        primaryStage.setTitle("I2LP");

        border = new BorderPane();
        scene = new Scene(border, 1000, 600);
        scene.getStylesheets().add("bootstrapfx.css");
        primaryStage.setScene(scene);

        previewPanels = new ArrayList<>();

        border.setTop(addBorderTop());
        border.setCenter(addBorderCenter());

        //The following code has to be placed between addBorderCenter() and addBorderLeft()
        int numOfPresentations = 20; //TODO: numOfPresentations to be read from database
        for (int i=0; i<numOfPresentations; i++) {
            PresentationPreviewPanel previewPanel = new PresentationPreviewPanel(presentationPreviewsFlowPane);
            previewPanel.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if(event.getButton() == MouseButton.PRIMARY) {
                    if (previewPanel.isSelected()) {
                        launchPresentation(previewPanel.getPresentationPath());
                    } else {
                        for (int j = 0; j < numOfPresentations; j++)
                            previewPanels.get(j).setSelected(false);

                        previewPanel.setSelected(true);
                        selectedPresID = previewPanel.getPresentationID();
                        border.setRight(addBorderRight());
                    }
                } else if (event.getButton() == MouseButton.SECONDARY && this instanceof TeacherDashboard) {
                    ContextMenu cMenu = new ContextMenu();

                    MenuItem edit = new MenuItem("Edit");
                    edit.setOnAction(editEvent -> showPresentationEditor(previewPanel.getPresentationPath()));
                    cMenu.getItems().add(edit);

                    MenuItem schedule = new MenuItem("Schedule");
                    schedule.setOnAction(ScheduleEvent -> showScheduler(event.getScreenX(), event.getScreenY()));
                    cMenu.getItems().add(schedule);

                    cMenu.show(primaryStage, event.getScreenX(), event.getScreenY());
                }

            });
            previewPanels.add(previewPanel);
        }

        border.setRight(addBorderRight());
        border.setLeft(addBorderLeft());

        primaryStage.show();
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
        aboutPopup.getContent().add(aboutStackPane);
        Region backgroundRegion = new Region();
        backgroundRegion.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        aboutStackPane.getChildren().add(backgroundRegion);
        BorderPane aboutBorder = new BorderPane();
        aboutStackPane.getChildren().add(aboutBorder);
        ImageView ediLogoImageView = new ImageView(new Image("file:projectResources/edi.png"));
        ediLogoImageView.setPreserveRatio(true);
        ediLogoImageView.setSmooth(true);
        ediLogoImageView.setFitWidth(300);
        ediLogoImageView.setCache(true);
        Label aboutLabel = new Label("Edi by I2LP, " + Constants.BUILD_STRING);
        aboutBorder.setBottom(aboutLabel);
        aboutBorder.setAlignment(aboutLabel, Pos.CENTER);
        aboutBorder.setCenter(ediLogoImageView);

        aboutPopup.setAutoHide(true);
        aboutPopup.show(primaryStage);
    }

    /**
     * s
     * Helper method to launch correct Presentation manager dependent upon current object type
     *
     * @param path Path to presentation
     * @author Amrik Sadhra
     */
    private void launchPresentation(String path) {
        if (this instanceof StudentDashboard) {
            presentationManager = new StudentPresentationManager();
        } else if (this instanceof TeacherDashboard) {
            presentationManager = new TeacherPresentationManager();
        }
        presentationManager.openPresentation(path);
    }

    private HBox addTopPanel() {
        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(15, 12, 15, 12));
        topPanel.setSpacing(10);
        topPanel.setStyle("-fx-background-color: #34495e;");

        Button createPresButton = new Button("TEST: Generate Thumbnails");
        createPresButton.getStyleClass().setAll("btn", "btn-success");
        createPresButton.setOnAction(event -> ThumbnailGenerationManager.generateSlideThumbnails("file:projectResources/sampleXMLsimple.xml"));

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        Button loadPresButton = new Button("Load Presentation");
        loadPresButton.getStyleClass().setAll("btn", "btn-default");
        loadPresButton.setOnAction(event -> {
            Node source = (Node) event.getSource();
            Window stage = source.getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                launchPresentation(file.getPath());
            }
        });

        Text platformTitle = new Text("     Integrated Interactive Learning Platform");
        platformTitle.getStyleClass().setAll("h3");
        platformTitle.setFill(Color.WHITESMOKE);

        topPanel.getChildren().addAll(createPresButton, loadPresButton, platformTitle);

        return topPanel;
    }

    private VBox addBorderLeft() {
        VBox controlsVBox = new VBox();
        controlsVBox.setPadding(new Insets(10));
        controlsVBox.setSpacing(8);

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

    private ScrollPane addBorderRight() {
        ScrollPane scroll = new ScrollPane();

        FlowPane flow = new FlowPane();
        flow.setPadding(new Insets(5, 0, 5, 0));
        flow.setVgap(4);
        flow.setHgap(4);
        flow.setPrefWrapLength(170); // preferred width allows for two columns
        flow.setStyle("-fx-background-color: #ffffff;");

        int numSlides = 20; //TODO: Obtain number of slides from XML

        Panel[] slides = new Panel[numSlides];

        for (int i = 0; i < numSlides; i++) {
            slides[i] = new Panel("Slide " + i);
            slides[i].getStyleClass().add("panel-primary");

            ImageView preview;
            try {
                preview = new ImageView("file:" + System.getProperty("java.io.tmpdir") + "Edi/Thumbnails/" + selectedPresID + "_slide" + i + "_thumbnail.png");
            } catch (NullPointerException | IllegalArgumentException e) {
                logger.info("Slide thumbnail not found");
                preview = new ImageView("file:projectResources/emptyThumbnail.png");
            }

            preview.setFitWidth(150);
            preview.setPreserveRatio(true);
            preview.setSmooth(true);
            preview.setCache(true);

            slides[i].setBody(preview);
            slides[i].setPrefWidth(170);//Dynamic resizing of panel width possible?
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
            if(sortKey.equals("Name A-Z"))
                return p1.getPresentationID().compareTo(p2.getPresentationID());
            else if(sortKey.equals("Name Z-A"))
                return -p1.getPresentationID().compareTo(p2.getPresentationID());
            else if(sortKey.equals("Subject A-Z"))
                return p1.getPresentationSubject().compareTo(p2.getPresentationSubject());
            else if(sortKey.equals("Subject Z-A"))
                return -p1.getPresentationSubject().compareTo(p2.getPresentationSubject());
            else return 0;
        });

        presentationPreviewsFlowPane.getChildren().clear();
        for(PresentationPreviewPanel panel : previewPanels) {
            if(!panel.isHidden())
                presentationPreviewsFlowPane.getChildren().add(panel);
        }
    }

    private void showScheduler(double x, double y) {
        Popup schedulerPopup = new Popup();
        BorderPane popupBorder = new BorderPane();

        DatePicker datePicker = new DatePicker(LocalDate.now());
        popupBorder.setCenter(datePicker);

        Button scheduleButton = new Button("Schedule");
        scheduleButton.getStyleClass().setAll("btn", "btn-default");
        scheduleButton.setOnAction(event -> {
            LocalDate date = datePicker.getValue();
            logger.info("Selected Date: " + date);
            schedulerPopup.hide();
        });
        popupBorder.setBottom(scheduleButton);
        popupBorder.setAlignment(scheduleButton, Pos.CENTER);

        //TODO: JavaFX has no native time picker, we need to find one made by someone or implement one ourselves

        schedulerPopup.getContent().add(popupBorder);
        schedulerPopup.show(primaryStage, x, y);
    }

    private void showPresentationEditor(String presentationPath) {
        new PresentationEditor(presentationPath);
    }

    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }
}

