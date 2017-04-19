package com.i2lp.edi.client.presentationViewer;
import com.i2lp.edi.client.managers.PresentationManager;
import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.scene.layout.Panel;


/**
 * Created by kma517 on 16/03/2017.
 */
public class TeacherPresentationManager extends PresentationManager {



    @Override
    protected void questionQueueFunction() {
        Stage questionQueueStage = new Stage();
        questionQueueStage.setTitle("Question Queue");
        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: #34495e");
        scene = new Scene(bp, 450, 450);
        scene.getStylesheets().add("bootstrapfx.css");
        Label title = new Label("Question Queue");
        bp.setPadding(new Insets(0,10,10,10));
        title.setPadding(new Insets(10,0,10,0));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        bp.setTop(title);

        ScrollPane sp = new ScrollPane();
        sp.setMaxWidth(Double.MAX_VALUE);
        FlowPane fp = new FlowPane();
        fp.setPrefWrapLength(100);
        fp.setMaxWidth(Double.MAX_VALUE);
        fp.setPadding(new Insets(5,0,5,0));
        fp.setVgap(4);
        fp.setHgap(4);
        fp.setStyle("-fx-background-color: whitesmoke");

        int numTestCases = 10;

        Panel[] slides = new Panel[numTestCases];

        for(int i = 0;i< numTestCases;i++){
            slides[i] = new Panel("This is test question: "+i);
            //slides[i].getStyleClass().add("panel-primary");
            slides[i].setMinWidth(400);
            fp.getChildren().add(slides[i]);
            Label lab = new Label(slides[i].getText());
            slides[i].addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
                    border.setCenter(lab);
            });

            int finalI = i;
            Animation animation = new Transition() {
                {
                    setCycleDuration(Duration.minutes(5));
                    setInterpolator(Interpolator.EASE_OUT);
                }
                @Override
                protected void interpolate(double frac) {
                    Color vColor = new Color(1,0,0,0 + frac);
                    slides[finalI].setBackground(new Background(new BackgroundFill(vColor, CornerRadii.EMPTY, Insets.EMPTY)));
                }
            };
            animation.play();

        }
        sp.setContent(fp);
        bp.setCenter(sp);
        questionQueueStage.setScene(scene);
        questionQueueStage.show();
    }

    @Override
    protected void createCommentEditor() {
        he.setMaxWidth(Double.MAX_VALUE);

        Button saveButton = new Button("Save Locally");
        saveButton.getStyleClass().setAll("btn", "btn-default");
        saveButton.setOnAction(event -> {
            comment = he.getHtmlText();
            WebView webV = new WebView();
            WebEngine webE = webV.getEngine();
            webE.loadContent(comment);
            commentView.setBody(webV);
        });

        HBox controlBox = new HBox();
        controlBox.setStyle("-fx-background-color: #34495e;");
        controlBox.setPadding(new Insets(5, 12, 5, 12));
        controlBox.setSpacing(12);
        controlBox.setMaxWidth(Double.MAX_VALUE);
        controlBox.getChildren().add(saveButton);

        commentEditor = new VBox();
        commentEditor.setMaxSize(600, 600);
        commentEditor.getChildren().addAll(he, controlBox);
    }



}
