package dashboard;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;


import org.kordamp.bootstrapfx.scene.layout.Panel;
import utilities.GraphicElement;
import utilities.SlideElement;
import utilities.TextElement;

import java.util.ArrayList;

/**
 * Created by amriksadhra on 24/01/2017.
 */
public class Dashboard extends Application {
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


        //------ Testing Code!--------
        ArrayList<SlideElement> slideElements = new ArrayList<>();

        TextElement myTextElement = new TextElement();
        myTextElement.setTextContent("<h1 style='background : rgba(0,0,0,0);'><b><font color=\"red\">IILP </font><font color=\"blue\">HTML</font> <font color=\"green\">Support Test</font></b></h1>");
        myTextElement.setSlideCanvas(stack);
        slideElements.add(myTextElement);

        GraphicElement myGraphicElement = new GraphicElement();
        myGraphicElement.setSlideCanvas(stack);
        slideElements.add(myGraphicElement);

        //Render Loop
        for(SlideElement slideElement : slideElements){
            slideElement.renderElement();
        }

        //Test a simple animation
        animationTest(slideElements.get(1).getCoreNode());
//        addMediaPlayerElement(stack);

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
//        DoubleProperty mvw = mv.fitWidthProperty();
//        DoubleProperty mvh = mv.fitHeightProperty();
//        mvw.bind(Bindings.selectDouble(mv.sceneProperty(), "width"));
//        mvh.bind(Bindings.selectDouble(mv.sceneProperty(), "height"));
        mv.setPreserveRatio(true);

        HBox control = mediaControl(mediaPlayer);

        BorderPane mediaPane = new BorderPane();
        //GridPane.setConstraints(mv, 0, 0);
        //GridPane.setConstraints(control, 0, 1);
        mediaPane.setCenter(mv);
        mediaPane.setBottom(control);
        mediaPane.setStyle("-fx-background-color: black;");


        stackPane.getChildren().add(mediaPane);

    }

    private HBox mediaControl(MediaPlayer mp) {

        Duration currentTime = mp.getCurrentTime();

        HBox mediaBar = new HBox();
        mediaBar.setStyle("-fx-background-color: whitesmoke");
        mediaBar.setAlignment(Pos.CENTER);
        mediaBar.setPadding(new Insets(5, 10, 5, 10));
        BorderPane.setAlignment(mediaBar, Pos.CENTER);


        //Control Buttons
        if(mp.isAutoPlay() == false) {
            final Button playButton = new Button(">");
            playButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if(mp.getStatus() != MediaPlayer.Status.PLAYING) {
                        playButton.setText("||");
                        mp.play();
                    }else {
                        playButton.setText(">");
                        mp.pause();
                    }
                }
            });
            mediaBar.getChildren().add(playButton);
        }else{
            final Button pauseButton = new Button("||");
            pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if(mp.getStatus() == MediaPlayer.Status.PLAYING) {
                        pauseButton.setText(">");
                        mp.pause();
                    }else {
                        pauseButton.setText("||");
                        mp.play();
                    }
                }
            });
            mediaBar.getChildren().add(pauseButton);
        }



        final Button stopButton = new Button("STOP");
        stopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mp.stop();

            }
        });


        mediaBar.getChildren().add(stopButton);

        //Seek Control
        ProgressBar videoTime = new ProgressBar(0);
        videoTime.setMaxWidth(Double.MAX_VALUE);
        ChangeListener progressChangeListener = new ChangeListener<Duration>() {
            @Override public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
                videoTime.setProgress(1.0 * mp.getCurrentTime().toMillis() / mp.getTotalDuration().toMillis());
            }
        };
        mp.currentTimeProperty().addListener(progressChangeListener);
        mediaBar.getChildren().add(videoTime);

        //Remaining Time
        Label playTime = new Label();
        //playTime.setText(mp.getCurrentTime()+"/"+mp.getTotalDuration());
        mediaBar.getChildren().add(playTime);

        //Volume Label
        Label volume = new Label("  Volume: ");
        mediaBar.getChildren().add(volume);

        //Volume Slider
        Slider volumeSlider = new Slider();
        mediaBar.getChildren().add(volumeSlider);

        return mediaBar;
    }
}


