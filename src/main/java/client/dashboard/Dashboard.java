package client.dashboard;

import client.presentationElements.*;
import client.presentationViewer.TeacherPresentationManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import client.managers.EdiManager;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import client.utilities.*;
import client.presentationViewer.TeacherPresentationManager;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by amriksadhra on 24/01/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused", "Duplicates"})
public abstract class Dashboard extends Application {
    protected Scene scene;
    protected BorderPane border;
    protected Presentation myPresentationElement;
    protected Logger logger = LoggerFactory.getLogger(Dashboard.class);
    private EdiManager ediManager;

    @Override
    public void start(Stage primaryStage) {
        //Initialise UI
        primaryStage.setTitle("I^2LP");

        border = new BorderPane();
        scene = new Scene(border, 1000, 600);
        scene.getStylesheets().add("bootstrapfx.css");
        primaryStage.setScene(scene);


        border.setTop(addBorderTop());
        border.setLeft(addLeftPanel());
        border.setCenter(addCenterPanel());
        border.setRight(addRightPanel());

        primaryStage.show();

    }


    private ScrollPane addCenterPanel() {

        FlowPane presentationsFlowPane = new FlowPane(Orientation.HORIZONTAL);

        presentationsFlowPane.setPadding(new Insets(5, 0, 5, 0));
        presentationsFlowPane.setVgap(4);
        presentationsFlowPane.setHgap(4);
        presentationsFlowPane.setStyle("-fx-background-color: #ffffff;");

        int arraySize = 20;
        Panel[] presentationPanelList = new Panel[arraySize];

        for(int i=0; i<arraySize; i++) {
            Panel presentationPanel = new Panel(String.format("Presentation Title %d", i));

            presentationPanel.getStyleClass().add("panel-primary");
            presentationPanel.setBody(new Text("Presentation preview"));

            presentationPanelList[i] = presentationPanel;
            presentationPanelList[i].addEventHandler(MouseEvent.MOUSE_CLICKED, evt->{
                TeacherPresentationManager tpm = new TeacherPresentationManager();
                tpm.openPresentation("poop");
            });
        }

        for(int i=0; i<presentationPanelList.length; i++) presentationsFlowPane.getChildren().add(presentationPanelList[i]);

        ScrollPane centerPane = new ScrollPane();
        centerPane.setContent(presentationsFlowPane);
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
        menuBar.getMenus().addAll(new Menu("File"),
                                new Menu("Edit"),
                                new Menu("Dogs"),
                                new Menu("Spinach"));

        menuBar.setUseSystemMenuBar(true);

        return  menuBar;
    }

    private HBox addTopPanel() {
        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(15, 12, 15, 12));
        topPanel.setSpacing(10);
        topPanel.setStyle("-fx-background-color: #34495e;");

        Button createPresButton = new Button("Create Presentation");
        createPresButton.getStyleClass().setAll("btn", "btn-success");

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        Button loadPresButton = new Button("Load Presentation");
        loadPresButton.getStyleClass().setAll("btn", "btn-default");
        loadPresButton.setOnAction(event -> {
            Node source = (Node) event.getSource();
            Window stage = source.getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                //loadPresentation(border, file.getPath());
            }
        });

        Text platformTitle = new Text("     Integrated Interactive Learning Platform");
        platformTitle.getStyleClass().setAll("h3");
        platformTitle.setFill(Color.WHITESMOKE);

        topPanel.getChildren().addAll(createPresButton, loadPresButton, platformTitle);

        return topPanel;
    }

    private VBox addLeftPanel() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);

        /*//Generate flexible flowplane to store shape buttons
        FlowPane controlsPane = new FlowPane();
        controlsPane.setPadding(new Insets(5, 0, 5, 0));
        controlsPane.setVgap(4);
        controlsPane.setHgap(4);
        controlsPane.setPrefWrapLength(180); // preferred width allows for two columns
        controlsPane.setStyle("-fx-background-color: #ffffff;");

        //Buttons for shapePane
        Button backButton = new Button("Back");
        backButton.getStyleClass().setAll("btn", "btn-success");
        backButton.setOnAction(event ->{
            //TODO: Lock these so they only function once presentation loaded
            controlPresentation(Slide.SLIDE_BACKWARD);
        });
        controlsPane.getChildren().add(backButton);
        Button forwardsButton = new Button("Forwards");
        forwardsButton.getStyleClass().setAll("btn", "btn-warning");
        forwardsButton.setOnAction(event ->{
            //TODO: Lock these so they only function once presentation loaded
            controlPresentation(Slide.SLIDE_FORWARD);
        });
        controlsPane.getChildren().add(forwardsButton);
        Button fillButton1 = new Button("Filler");
        fillButton1.getStyleClass().setAll("btn", "btn-info");
        controlsPane.getChildren().add(fillButton1);
        Button fillButton2 = new Button("Filler");
        fillButton2.getStyleClass().setAll("btn", "btn-danger");
        controlsPane.getChildren().add(fillButton2);*/

        VBox controlsPane = new VBox();
        controlsPane.setPadding(new Insets(3, 0, 3, 0));
        controlsPane.setSpacing(3);
        controlsPane.setStyle("-fx-background-color: #ffffff;");

        Button subjectButton1 = new Button("Subject 1");
        subjectButton1.getStyleClass().setAll("btn", "btn-success");
        controlsPane.getChildren().add(subjectButton1);

        Button subjectButton2 = new Button("Subject 3");
        subjectButton2.getStyleClass().setAll("btn", "btn-success");
        controlsPane.getChildren().add(subjectButton2);

        Button subjectButton3 = new Button("Subject 3");
        subjectButton3.getStyleClass().setAll("btn", "btn-success");
        controlsPane.getChildren().add(subjectButton3);

        //Create Panel for shapes
        Panel slideControls = new Panel();
        slideControls.setText("My subjects");
        slideControls.getStyleClass().add("panel-primary");
        slideControls.setBody(controlsPane);
        VBox.setMargin(slideControls, new Insets(0, 0, 0, 0));
        vbox.getChildren().add(slideControls);

        return vbox;
    }

    private ScrollPane addRightPanel() {
        ScrollPane scroll = new ScrollPane();

        FlowPane flow = new FlowPane();
        flow.setPadding(new Insets(5, 0, 5, 0));
        flow.setVgap(4);
        flow.setHgap(4);
        flow.setPrefWrapLength(170); // preferred width allows for two columns
        flow.setStyle("-fx-background-color: #ffffff;");

        int numSlides = 20;

        Panel[] slides = new Panel[numSlides];

        for (int i = 0; i < numSlides; i++) {
            slides[i] = new Panel();
            slides[i].getStyleClass().add("panel-primary");
            slides[i].setBody(new Text("Slide panel preview here."));
            slides[i].setPrefWidth(170);//Dynamic resizing of panel width possible?
            flow.getChildren().add(slides[i]);
            flow.setMargin(slides[i], new Insets(0, 20, 0, 5));
        }

        scroll.setContent(flow);

        return scroll;
    }

    private HBox addStatBar(Slide presentationElement) {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(0, 12, 0, 12));
        hbox.setSpacing(2);
        hbox.setStyle("-fx-background-color: #34495e;");

        BorderPane border = new BorderPane();

        Text versionText = new Text("Version 0.0.1 Alpha");
        versionText.setFont(Font.font("San Francisco", FontWeight.NORMAL, 12));
        versionText.setFill(Color.WHITE);

        border.setLeft(versionText);

        border.setCenter(new Text("                                                     "));

        Text coordTextBar = new Text("Mouse data not available!");
        coordTextBar.setFont(Font.font("San Francisco", FontWeight.NORMAL, 12));
        coordTextBar.setFill(Color.WHITE);

        border.setRight(coordTextBar);

        //TODO: This stops working when WebView enters Slide
        presentationElement.setOnMouseMoved(event -> coordTextBar.setText(
                "Slide Number: " + presentationElement.getSlideID() + " (x: " + event.getX() + ", y: " + event.getY() + ") -- " +
                        "(sceneX: " + event.getSceneX() + ", sceneY: " + event.getSceneY() + ") -- " +
                        "(screenX: " + event.getScreenX() + ", screenY: " + event.getScreenY() + ")"));

        hbox.getChildren().addAll(border);

        return hbox;
    }


    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }
}


