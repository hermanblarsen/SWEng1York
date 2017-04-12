package client.dashboard;

import client.managers.EdiManager;
import client.managers.PresentationManager;
import client.presentationElements.Presentation;
import client.presentationElements.Slide;
import client.presentationElements.TextElement;
import client.presentationViewer.StudentPresentationManager;
import client.presentationViewer.TeacherPresentationManager;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
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
import java.util.concurrent.atomic.AtomicReference;


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

        Button createPresButton = new Button("TEST: Generate Thumbnails");
        createPresButton.getStyleClass().setAll("btn", "btn-success");
        createPresButton.setOnAction(event -> {
            Dashboard.generateSlideThumbnails("file:externalResources/sampleXMLsimple.xml");
        });

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
                preview = new ImageView("file:" + System.getProperty("java.io.tmpdir") + "Edi/Thumbnails/" + selectedPresID + "_slide" + i + "_thumbnail.png");
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

    private static void generateSlideThumbnails(String presentationPath) {
        PresentationManager slideGenManager = new TeacherPresentationManager();
        slideGenManager.openPresentation(presentationPath);
        generateSlideThumbNail(slideGenManager);
    }

    private static void generateSlideThumbNail(PresentationManager slideGenManager){
        Presentation presentation = slideGenManager.myPresentationElement;

        //TODO: This method works for the first slide. To make it work for all of them, change  while (slideGenManager.slideAdvance(presentation, Slide.SLIDE_FORWARD) != Presentation.SLIDE_CHANGE) ;
        //to: while (slideGenManager.slideAdvance(presentation, Slide.SLIDE_FORWARD) != Presentation.PRESENTATION_FINISH){, wrapping the entire rest of the method.
        //This will fail, as we will advance to the next slide, so the asynchronous screenshot will now screenshot the next slide instead of the first
        //We therefore need to spinlock/wait on the screenshot to be taken before continuing the while loop, but we cannot do this using a sempahore w/ while loop
        //As this will stall the main JavaFx thread, causing the snapshot to not be taken. We cannot thread this entire method to allow the while loop wait to be successful,
        //as it will then exist on a thread that is not the javaFx worker thread, and therefore the snapshot will fail (cant snapshot outside main javafx thread)

        //Move to end of current slide so all elements are visible in snapshot
        while (slideGenManager.slideAdvance(presentation, Slide.SLIDE_FORWARD) != Presentation.SLIDE_CHANGE) ;

        //If we're in last element of slide, take snapshot
        if (presentation.getSlide(slideGenManager.currentSlideNumber - 1).getCurrentSequenceNumber() == presentation.getSlide(slideGenManager.currentSlideNumber - 1).getMaxSequenceNumber()) {
            File thumbnailFile = new File(System.getProperty("java.io.tmpdir") + "Edi/Thumbnails/" + presentation.getDocumentID() + "_slide" + (slideGenManager.currentSlideNumber - 1) + "_thumbnail.png");
            if (!thumbnailFile.exists()) {
                thumbnailFile.getParentFile().mkdirs(); //Create directory structure if not present yet
            } else {
                return;
            }

            //Set number of workers to 0, if no workers, then webviewRenderChecker will skip, and we will snapshot
            AtomicReference<Integer> numWorkers = new AtomicReference<>(0);

            //WebViews don't render immediately, so text doesn't show in snapshots.
            if (!presentation.getSlide(slideGenManager.currentSlideNumber - 1).getTextElementList().isEmpty()) {
                //Set number of workers to the number that there are. This will be decremented on worker completion
                numWorkers.set(presentation.getSlide(slideGenManager.currentSlideNumber - 1).getTextElementList().size());

                for (TextElement toAddWorkerListener : presentation.getSlide(slideGenManager.currentSlideNumber - 1).getTextElementList()) {
                    toAddWorkerListener.webEngine.getLoadWorker().stateProperty().addListener((arg0, oldState, newState) -> {
                        if (newState == Worker.State.SUCCEEDED) {
                            //Decrement number of workers still working, as this one is finished
                            numWorkers.set(numWorkers.get() - 1);
                        }
                    });
                }
            }

            //This task will succeed when the webviews have all rendered
            Task webviewRenderChecker = new Task() {
                @Override
                protected Object call() throws Exception {
                    //If no workers, skip the render delay
                    if (numWorkers.get() == 0) return null;
                    else {
                        //Wait for number of workers to be equal to 0 (All workers rendered)
                        while (numWorkers.get() != 0) ;
                        logger.info("All webviews on TextElements in slide " + (slideGenManager.currentSlideNumber - 1) + " have completed rendering.");
                        //TODO: Even though the webview has told us its done rendering, there is some overhead before it is visible on StackPane. Account for this with minor delay. I cant find any state variable that we can check to avoid waiting. Maybe you can Kacper
                        //This value may need to be upped on slower systems to ensure successful screenshot
                        Thread.sleep(50);
                        return null;
                    }
                }
            };

            //Begin to check for webview render finish
            Thread webviewRenderCheckThread = new Thread(webviewRenderChecker);
            webviewRenderCheckThread.start();

            //When webviews rendered, can take snapshot
            webviewRenderChecker.setOnSucceeded(event -> {
                logger.info("Generating thumbnail file for " + presentation.getDocumentID() + " Slide " + (slideGenManager.currentSlideNumber - 1) + " at " + thumbnailFile.getAbsolutePath());
                WritableImage thumbnail = presentation.getSlide(slideGenManager.currentSlideNumber - 1).snapshot(new SnapshotParameters(), null);
                try {
                    //Write the snapshot to the chosen file
                    ImageIO.write(SwingFXUtils.fromFXImage(thumbnail, null), "png", thumbnailFile);
                    logger.info("Done");
                    //Advance to next slide, and generate next Slide Thumbnail
                    if(slideGenManager.slideAdvance(presentation, Slide.SLIDE_FORWARD) != Presentation.PRESENTATION_FINISH){
                        logger.info("Done generating thumbnails for presentation " + presentation.getDocumentID());
                        return;
                    } else {
                        generateSlideThumbNail(slideGenManager);
                    }
                } catch (IOException ex) {
                    logger.error("Generating presentation thumbnail for " + presentation.getDocumentID() + " at " + thumbnailFile.getAbsolutePath() + " failed");
                }
            });
        }
    }

    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }
}

