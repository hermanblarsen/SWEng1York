package dashboard;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.StageStyle;
import javafx.util.Duration;


import org.kordamp.bootstrapfx.scene.layout.Panel;

import javax.swing.text.Document;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by amriksadhra on 24/01/2017.
 */
public class Dashboard extends Application {
    ArrayList<Node> nodeList = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("I^2LP");

        BorderPane border = new BorderPane();
        Scene scene = new Scene(border, 1000, 600);
        scene.getStylesheets().add("bootstrapfx.css");
        primaryStage.setScene(scene);

        border.setTop(addHBox(primaryStage));
        border.setLeft(addVBox());

        Pane myPresentationElement = addPresentationElement();
        border.setCenter(myPresentationElement);
        border.setRight(addFlowPane());
        border.setBottom(addStatBar(myPresentationElement));


        primaryStage.show();
    }

    public HBox addHBox(Stage primaryStage) {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #34495e;");

        Button createPresButton = new Button("Create Presentation");
        createPresButton.getStyleClass().setAll("btn", "btn-success");

        Button loadPresButton = new Button("Load Presentation");
        loadPresButton.getStyleClass().setAll("btn", "btn-default");
        loadPresButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            fileChooser.showOpenDialog(primaryStage);
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

    public StackPane addPresentationElement() {
        StackPane stack = new StackPane();

        //addMediaPlayerElement(stack);
        addHtmlElement(stack, "<div>Lol</div>");
        addHtmlElement(stack, "<h1><b><font color=\"red\">IILP </font><font color=\"blue\">HTML</font> <font color=\"green\">Support Test</font></b></h1>");
        addCanvas(stack);

        //Test a simple animation
        animationTest(nodeList.get(1));


        return stack;
    }

    private void animationTest(Node toAnimate) {
        //Create new timer and schedule increment of first nodes Y position: Test animation
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(250),
                ae -> toAnimate.setTranslateY(toAnimate.getTranslateY() + 1)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }


    private void addCanvas(Pane stackPane) {
        // Create a wrapper Pane first
        Pane canvasPane = new Pane();

        Canvas canvas = new Canvas(canvasPane.getWidth(), canvasPane.getHeight());
        canvasPane.getChildren().add(canvas);
        // Bind the width/height property to the wrapper Pane
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());
        // redraw when resized
        canvas.widthProperty().addListener(event -> draw(canvas));
        canvas.heightProperty().addListener(event -> draw(canvas));
        draw(canvas);

        trackNode(canvas);

        stackPane.getChildren().add(canvasPane);
    }

    private void trackNode(Node toTrack) {
        nodeList.add(toTrack);
    }

    private void addHtmlElement(Pane stackPane, String htmlString) {
        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();

        trackNode(browser);

        stackPane.getChildren().add(browser);
        webEngine.loadContent(htmlString);
        //TODO: CSS styling support so we can change fonts, etc.
        webEngine.setUserStyleSheetLocation(getClass().getResource("../style.css").toString());
    }

    private void draw(Canvas canvas) {
        //Draw Polygons in here
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.BLACK);
        gc.setLineWidth(5);

        gc.setStroke(Color.BLUEVIOLET);
        gc.strokeOval(30, 60, 30, 30);
        gc.setStroke(Color.BLUE);
        gc.strokeOval(50, 60, 30, 30);
        gc.setStroke(Color.INDIANRED);
        gc.strokeOval(70, 60, 30, 30);
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

    private void addMediaPlayerElement(Pane stackPane) {
        String MEDIA_URL =
                "http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv";

        Media media = new Media(MEDIA_URL);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);

        MediaView mv = new MediaView(mediaPlayer);
        DoubleProperty mvw = mv.fitWidthProperty();
        DoubleProperty mvh = mv.fitHeightProperty();
        mvw.bind(Bindings.selectDouble(mv.sceneProperty(), "width"));
        mvh.bind(Bindings.selectDouble(mv.sceneProperty(), "height"));
        mv.setPreserveRatio(true);

        HBox control = mediaControl();

        GridPane mediaPane = new GridPane();
        GridPane.setConstraints(mv, 0, 0);
        GridPane.setConstraints(control, 0, 1);
        mediaPane.getChildren().addAll(mv, control);
        mediaPane.setStyle("-fx-background-color: whitesmoke;");


        stackPane.getChildren().add(mediaPane);

    }

    private HBox mediaControl() {
        HBox mediaBar = new HBox();
        mediaBar.setAlignment(Pos.CENTER);
        mediaBar.setPadding(new Insets(5, 10, 5, 10));
        BorderPane.setAlignment(mediaBar, Pos.CENTER);

        final Button playButton = new Button(">");
        mediaBar.getChildren().add(playButton);

        return mediaBar;
    }
}

