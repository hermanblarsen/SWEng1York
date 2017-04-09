package client.dashboard;

import client.managers.EdiManager;
import client.managers.PresentationManager;
import client.presentationElements.Presentation;
import client.presentationViewer.StudentPresentationManager;
import client.presentationViewer.TeacherPresentationManager;
import client.utilities.ParserXML;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;


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
    protected PresentationManager presentationManager;
    protected Stage primaryStage;
    protected String selectedPresID;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
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
        PresentationPreviewPanel[] presentationPanelList = new PresentationPreviewPanel[arraySize];

        for (int i = 0; i < arraySize; i++) {
            final int finalI = i;

            presentationPanelList[i] = new PresentationPreviewPanel();
            //generateSlideThumbnails(presentationPanelList[i].getPresentationPath());
            presentationPanelList[i].addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (presentationPanelList[finalI].isSelected()) {
                    launchPresentation(presentationPanelList[finalI].getPresentationPath());
                } else {
                    for (int j = 0; j < arraySize; j++)
                        presentationPanelList[j].setSelected(false);

                    presentationPanelList[finalI].setSelected(true);
                    selectedPresID = presentationPanelList[finalI].getPresentationID();
                    border.setRight(addRightPanel());
                }
            });
        }

        for (Panel aPresentationPanelList : presentationPanelList)
            presentationsFlowPane.getChildren().add(aPresentationPanelList);

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

        return menuBar;
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
                launchPresentation(file.getPath());
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

        //Create Panel for subject filters
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
            slides[i] = new Panel("Slide " + i);
            slides[i].getStyleClass().add("panel-primary");

            ImageView preview;
            try {
                preview = new ImageView("file:externalResources/" + selectedPresID + "_slide" + i + "_thumbnail.png");
            } catch (NullPointerException | IllegalArgumentException e) {
                logger.info("Slide thumbnail not found");
                preview = new ImageView("file:externalResources/emptyThumbnail.png");
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

    private void generateSlideThumbnails(String presentationPath) {
        ParserXML parser = new ParserXML(presentationPath);
        Presentation presentation = parser.parsePresentation();
        for (int i = 0; i < presentation.getMaxSlideNumber(); i++) {
            WritableImage thumbnail = presentation.getSlide(i).snapshot(new SnapshotParameters(), null);
            File thumbnailFile = new File("externalResources/" + presentation.getDocumentID() + "_slide" + i + "_thumbnail.png");
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(thumbnail, null), "png", thumbnailFile);
            } catch (IOException e) {
                logger.error("Generating presentation thumbnail failed");
            }
        }

    }

    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }
}


