package com.i2lp.edi.client.presentationViewer;
import com.i2lp.edi.client.managers.PresentationManager;
import com.i2lp.edi.client.presentationElements.CommentPanel;
import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.scene.layout.Panel;


/**
 * Created by kma517 on 16/03/2017.
 */
public class TeacherPresentationManager extends PresentationManager {
    protected boolean questionClicked = false;
    protected String setText;

    protected BorderPane questionQueueFunction() {
        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: #34495e");
        bp.setPadding(new Insets(0,10,10,10));

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
        StackPane slidePane = new StackPane();

        for(int i = 0;i< numTestCases;i++){
            slides[i] = new Panel("This is test question: "+i);
            //slides[i].getStyleClass().add("panel-primary");
            slides[i].setMinWidth(400);
            fp.getChildren().add(slides[i]);
            Label lab = new Label(slides[i].getText());
            Region backgroundRegion = new Region();
            backgroundRegion.setBackground(new Background(new BackgroundFill(Color.web("#34495e"), null, null)));
            setText = new String();
            slides[i].addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
                if(!questionClicked) {
                    stackPane.getChildren().remove(slidePane);
                    slidePane.getChildren().removeAll(backgroundRegion,lab);
                    lab.setFont(new Font("Helvetica",50));
                    lab.setTextFill(Color.WHITE);
                    lab.setWrapText(true);
                    setText = lab.getText();
                    slidePane.getChildren().addAll(backgroundRegion,lab);
                    stackPane.getChildren().addAll(slidePane);
                    questionClicked = true;
                }else{
                    if(lab.getText() == setText) {
                        stackPane.getChildren().remove(slidePane);
                        slidePane.getChildren().removeAll(backgroundRegion, lab);
                        questionClicked = false;
                    }else{
                        stackPane.getChildren().remove(slidePane);
                        slidePane.getChildren().removeAll(backgroundRegion,lab);
                        lab.setFont(new Font("Helvetica",50));
                        lab.setTextFill(Color.WHITE);
                        lab.setWrapText(true);
                        setText = lab.getText();
                        slidePane.getChildren().addAll(backgroundRegion,lab);
                        stackPane.getChildren().addAll(slidePane);
                        questionClicked = true;
                    }
                }
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
        return bp;
    }

    @Override
    protected void loadSpecificFeatures() {
        Stage teacherToolKit = new Stage();
        teacherToolKit.setTitle("Teacher toolkit");
        TabPane tp = new TabPane();
        tp.setStyle("-fx-background-color: #34495e");
        scene = new Scene(tp, 450, 450);
        scene.getStylesheets().add("bootstrapfx.css");
        Tab questions = new Tab();
        questions.setText("Question Queue");
        questions.setContent(questionQueueFunction());
        tp.getTabs().add(questions);

        Tab studentStats = new Tab();
        studentStats.setText("Students");
        studentStats.setContent(studentStats());
        tp.getTabs().add(studentStats);

        teacherToolKit.setScene(scene);
        teacherToolKit.show();
        tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

    }

    protected BorderPane studentStats(){
        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: #34495e");
        bp.setPadding(new Insets(0,10,10,10));

        ScrollPane sp = new ScrollPane();
        sp.setMaxWidth(Double.MAX_VALUE);
        FlowPane fp = new FlowPane();
        fp.setPrefWrapLength(100);
        fp.setMaxWidth(Double.MAX_VALUE);
        fp.setPadding(new Insets(5,0,5,0));
        fp.setVgap(4);
        fp.setHgap(4);
        fp.setStyle("-fx-background-color: whitesmoke");

        int numTestCases = 15;

        Panel[] slides = new Panel[numTestCases];

        for(int i = 0;i< numTestCases;i++){
            slides[i] = new Panel("Stu Dent");
            //Label stuLab = new Label();
            Label tasksCompleted = new Label("Completed 5/6 Tasks");
            VBox studentDetails = new VBox();
            studentDetails.getChildren().addAll(tasksCompleted);
            slides[i].setBody(studentDetails);
            slides[i].getStyleClass().add("panel-primary");
            slides[i].setMinWidth(400);
            fp.getChildren().add(slides[i]);
        }
        sp.setContent(fp);
        bp.setCenter(sp);
        return bp;
    }

    @Override
    protected void createCommentPanel() {
        commentPanel = new CommentPanel(false);
    }

}
