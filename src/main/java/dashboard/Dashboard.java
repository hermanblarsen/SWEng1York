package dashboard;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.*;

import java.util.ArrayList;

/**
 * Created by amriksadhra on 24/01/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Dashboard extends Application {
    Scene scene;
    BorderPane border;
    Logger logger = LoggerFactory.getLogger(Dashboard.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //Initialise UI
        primaryStage.setTitle("I^2LP");

        border = new BorderPane();
        scene = new Scene(border, 1000, 600);
        scene.getStylesheets().add("bootstrapfx.css");
        primaryStage.setScene(scene);

        border.setTop(addHBox(primaryStage));
        border.setLeft(addVBox());


        border.setCenter(new StackPane());
        border.setRight(addFlowPane());

        primaryStage.show();

        //TEST
        loadPresentation(border, "shit");
    }

    private void loadPresentation(BorderPane mainUI, String path) {
        //ParserXML readPresentationParser = new ParserXML(path);

        //TEST
        Presentation myPresentationElement = generateTestPresentation();
        mainUI.setCenter(myPresentationElement.getCurrentSlide());

        //Presentation myPresentationElement = readPresentationParser.parsePresentation();

        //Keyboard listener for moving through presentation
        scene.setOnKeyPressed(ke -> {
            int presentationStatus = 0;
            if (ke.getCode().equals(KeyCode.RIGHT)) {
                presentationStatus = myPresentationElement.advance(Slide.SLIDE_FORWARD);
            } else if (ke.getCode().equals(KeyCode.LEFT)) {
                presentationStatus = myPresentationElement.advance(Slide.SLIDE_BACKWARD);
            }
            //If Presentation handler told us that slide is changing, update the Slide present on Main screen
            //Can do specific things when presentation reached end, or start.
            if ((presentationStatus == Presentation.SLIDE_CHANGE) || (presentationStatus == Presentation.PRESENTATION_FINISH) || (presentationStatus == Presentation.PRESENTATION_START)) {
                logger.info("Changing Slides");
                mainUI.setCenter(myPresentationElement.getCurrentSlide());
            }
        });

        mainUI.setBottom(addStatBar(myPresentationElement));
    }

    public HBox addHBox(Stage primaryStage) {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #34495e;");

        Button createPresButton = new Button("Create Presentation");
        createPresButton.getStyleClass().setAll("btn", "btn-success");


        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        Button loadPresButton = new Button("Load Presentation");
        loadPresButton.getStyleClass().setAll("btn", "btn-default");
        loadPresButton.setOnAction(event -> {
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                loadPresentation(border, file.getPath());
            }
        });

        Text platformTitle = new Text("     Integrated Interactive Learning Platform");
        platformTitle.getStyleClass().setAll("h3");
        platformTitle.setFill(Color.WHITESMOKE);

        hbox.getChildren().addAll(createPresButton, loadPresButton, platformTitle);

        return hbox;
    }

    public VBox addVBox() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);

        //Generate flexible flowplane to store shape buttons
        FlowPane shapesPane = new FlowPane();
        shapesPane.setPadding(new Insets(5, 0, 5, 0));
        shapesPane.setVgap(4);
        shapesPane.setHgap(4);
        shapesPane.setPrefWrapLength(180); // preferred width allows for two columns
        shapesPane.setStyle("-fx-background-color: #ffffff;");

        //Buttons for shapePane
        Button triButton = new Button("Triangle");
        triButton.getStyleClass().setAll("btn", "btn-success");
        shapesPane.getChildren().add(triButton);
        Button squareButton = new Button("Square");
        squareButton.getStyleClass().setAll("btn", "btn-warning");
        shapesPane.getChildren().add(squareButton);
        Button circButton = new Button("Circle");
        circButton.getStyleClass().setAll("btn", "btn-info");
        shapesPane.getChildren().add(circButton);
        Button starButton = new Button("Star");
        starButton.getStyleClass().setAll("btn", "btn-danger");
        shapesPane.getChildren().add(starButton);

        //Create Panel for shapes
        Panel shapes = new Panel();
        shapes.setText("Shapes");
        shapes.getStyleClass().add("panel-primary");
        shapes.setBody(shapesPane);
        VBox.setMargin(shapes, new Insets(0, 0, 0, 0));
        vbox.getChildren().add(shapes);

        //Generate flexible flowplane to store text buttons
        FlowPane textPane = new FlowPane();
        textPane.setPadding(new Insets(5, 0, 5, 0));
        textPane.setVgap(4);
        textPane.setHgap(4);
        textPane.setPrefWrapLength(180); // preferred width allows for two columns
        textPane.setStyle("-fx-background-color: #ffffff;");

        //Buttons for textPane
        Button boldButton = new Button("Bold");
        boldButton.getStyleClass().setAll("btn", "btn-default");
        textPane.getChildren().add(boldButton);
        Button italButton = new Button("Itallic");
        italButton.getStyleClass().setAll("btn", "btn-default");
        textPane.getChildren().add(italButton);
        Button underButton = new Button("Underline");
        underButton.getStyleClass().setAll("btn", "btn-default");
        textPane.getChildren().add(underButton);

        //Create panel for text controls
        Panel text = new Panel();
        text.setText("Text");
        text.getStyleClass().add("panel-default");
        text.setBody(textPane);
        VBox.setMargin(text, new Insets(0, 0, 0, 0));
        vbox.getChildren().add(text);

        return vbox;
    }

    public Presentation generateTestPresentation() {
        ArrayList<Slide> slides = new ArrayList<>();

        Slide slide1 = new Slide();
        slide1.setSlideID(1);
        slides.add(slide1);

        //Create some test Slide Elements
        ArrayList<SlideElement> slideElementsSlide1 = new ArrayList<>();

        //Create a test Text element, add some text and pop it onto our stack pane. This code will all be driven from XML parser
        TextElement myTextElement = new TextElement();
        myTextElement.setLayer(0);
        myTextElement.setStartSequence(1);
        myTextElement.setEndSequence(2);
        myTextElement.setTextContent("<h1 style='background : rgba(0,0,0,0);'><b><font color=\"red\">IILP </font><font color=\"blue\">HTML</font> <font color=\"green\">Support Test</font></b></h1>");
        myTextElement.setSlideCanvas(slide1);
        slideElementsSlide1.add(myTextElement);

        GraphicElement myGraphicElement = new GraphicElement();
        myGraphicElement.setLayer(1);
        myGraphicElement.setStartSequence(3);
        myGraphicElement.setEndSequence(4);
        myGraphicElement.setSlideCanvas(slide1);
        slideElementsSlide1.add(myGraphicElement);

        VideoElement myVideoElement = new VideoElement();
        myVideoElement.setMediaPath("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv");
        myVideoElement.setAutoPlay(true);
        myVideoElement.setMediaControl(true);
        myVideoElement.setLoop(false);
        myVideoElement.setVideoStartTime(Duration.seconds(5));
        myVideoElement.setVideoEndTime(Duration.seconds(7));
        myVideoElement.setAspectRatioLock(true);
        myVideoElement.setxPosition(200);
        myVideoElement.setyPosition(200);
        myVideoElement.setxSize(2000);
        myVideoElement.setySize(2000);
        myVideoElement.setSlideCanvas(slide1);
        slideElementsSlide1.add(myVideoElement);


        //Create a test Text element, add some text and pop it onto our stack pane. This code will all be driven from XML parser
        TextElement myTextElement1 = new TextElement();
        myTextElement1.setLayer(3);
        myTextElement1.setStartSequence(7);
        myTextElement1.setEndSequence(8);
        myTextElement1.setTextContent("<b>Poop</b>");
        myTextElement1.setSlideCanvas(slide1);
        slideElementsSlide1.add(myTextElement1);

        slide1.setSlideElementList(slideElementsSlide1);

        Slide slide2 = new Slide();
        slide1.setSlideID(2);
        slides.add(slide2);

        //Create some test Slide Elements
        ArrayList<SlideElement> slideElementsSlide2 = new ArrayList<>();

        //Create a test Text element, add some text and pop it onto our stack pane. This code will all be driven from XML parser
        TextElement myTextElementNewSlide = new TextElement();
        myTextElementNewSlide.setLayer(1);
        myTextElementNewSlide.setStartSequence(1);
        myTextElementNewSlide.setEndSequence(2);
        myTextElementNewSlide.setTextContent("<b>Slide2</b>");
        myTextElementNewSlide.setSlideCanvas(slide2);
        slideElementsSlide2.add(myTextElementNewSlide);

        slide2.setSlideElementList(slideElementsSlide2);

        Presentation myPresentation = new Presentation();
        myPresentation.setSlideList(slides);

        return myPresentation;
    }


    public ScrollPane addFlowPane() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

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

    public HBox addStatBar(Pane presentationElement) {
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

        presentationElement.setOnMouseMoved(event -> coordTextBar.setText(
                "(x: " + event.getX() + ", y: " + event.getY() + ") -- " +
                        "(sceneX: " + event.getSceneX() + ", sceneY: " + event.getSceneY() + ") -- " +
                        "(screenX: " + event.getScreenX() + ", screenY: " + event.getScreenY() + ")"));

        presentationElement.setOnMouseExited(event -> coordTextBar.setText("Mouse Exited"));

        hbox.getChildren().addAll(border);

        return hbox;
    }
}


