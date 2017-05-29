package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.presentationViewerElements.CommentPanel;
import com.i2lp.edi.server.packets.Question;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Created by kma517 on 16/03/2017.
 */
public class PresentationManagerTeacher extends PresentationManager {
    protected boolean questionClicked = false;
    protected boolean toolkitOpen = false;
    protected String setText;
    protected List<String> questionList;
    protected List<Student> studentList;
    protected Stage teacherToolKit;
    private int numberOfTestQuestions = 10;
    private int numberOnline = 0;
    private ArrayList<Question> newQuestionList;

    public PresentationManagerTeacher(EdiManager ediManager) {
        super(ediManager);
    }

    private void setListOfQuestions(){
        if(getPresentationSession() != null) {
            newQuestionList = getPresentationSession().getQuestionQueue();
        }
        if(newQuestionList == null){
            newQuestionList = new ArrayList<>();
        }
        for(Question question : newQuestionList){
            logger.info("Question: " + question.getQuestion_data());
        }
    }

    @Override
    protected void loadSpecificFeatures() {
        if (!toolkitOpen) {
            teacherToolKit = new Stage();
            teacherToolKit.initStyle(StageStyle.UTILITY);
            teacherToolKit.initOwner(presentationStage);
            teacherToolKit.setTitle("Teacher toolkit");
            TabPane tp = new TabPane();
            tp.setStyle("-fx-background-color: #34495e");
            scene = new Scene(tp, 450, 450);
            scene.getStylesheets().add("bootstrapfx.css");
            Tab questions = new Tab();
            questions.setText("Question Queue");
            questionList = new ArrayList<String>();
            setListOfQuestions();
            //questionList = generateQuestions();
            questions.setContent(questionQueueFunction(newQuestionList));
            tp.getTabs().add(questions);

            Tab studentStats = new Tab();
            studentStats.setText("Students");
            studentList = generateTestStudents();
            studentStats.setContent(studentStats(studentList));
            tp.getTabs().add(studentStats);

            teacherToolKit.setScene(scene);
            teacherToolKit.show();
            tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            toolkitOpen = true;

            teacherToolKit.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, evt -> toolkitOpen = false);
        }

    }

    protected List<Student> generateTestStudents() { //TODO remove
        ArrayList<Student> studentList = new ArrayList<Student>();

        Student stu1 = new Student("Koen Arroo", 4, true);
        studentList.add(stu1);
        Student stu2 = new Student("Herman Larsen", 10, true);
        studentList.add(stu2);
        Student stu3 = new Student("Amrik Sadhra", 2, true);
        studentList.add(stu3);
        Student stu4 = new Student("Kacper Sagnowski", 7, true);
        studentList.add(stu4);
        Student stu5 = new Student("Zain Rajput", 8, false);
        studentList.add(stu5);


        return studentList;
    }

    protected BorderPane questionQueueFunction(List<Question> questions) {
        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: #34495e");
        bp.setPadding(new Insets(0, 10, 10, 10));

        ScrollPane sp = new ScrollPane();
        sp.setMaxWidth(Double.MAX_VALUE);
        FlowPane fp = new FlowPane();
        fp.setPrefWrapLength(100);
        fp.setMaxWidth(Double.MAX_VALUE);
        fp.setPadding(new Insets(5, 0, 5, 0));
        fp.setVgap(4);
        fp.setHgap(4);
        fp.setStyle("-fx-background-color: whitesmoke");
        sp.setStyle("-fx-background-color: whitesmoke");


        Panel[] slides = new Panel[questions.size()];
        StackPane slidePane = new StackPane();

        for (int i = 0; i < questions.size(); i++) {
            slides[i] = new Panel(questions.get(i).getQuestion_data());
            //slides[i].getStyleClass().add("panel-primary");
            slides[i].setMinWidth(400);
            fp.getChildren().add(slides[i]);
            Label lab = new Label(slides[i].getText());
            Region backgroundRegion = new Region();
            backgroundRegion.setBackground(new Background(new BackgroundFill(Color.web("#34495e"), null, null)));
            setText = new String();
            slides[i].addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
                if (!questionClicked) {
                    displayPane.getChildren().remove(slidePane);
                    slidePane.getChildren().removeAll(backgroundRegion, lab);
                    lab.setFont(new Font("Helvetica", 50));
                    lab.setTextFill(Color.WHITE);
                    lab.setWrapText(true);
                    setText = lab.getText();
                    slidePane.setPrefSize(slideWidth, slideHeight);
                    slidePane.getChildren().addAll(backgroundRegion, lab);
                    displayPane.getChildren().addAll(slidePane);
                    questionClicked = true;
                } else {
                    if (Objects.equals(lab.getText(), setText)) {
                        displayPane.getChildren().remove(slidePane);
                        slidePane.getChildren().removeAll(backgroundRegion, lab);
                        questionClicked = false;
                    } else {
                        displayPane.getChildren().remove(slidePane);
                        slidePane.getChildren().removeAll(backgroundRegion, lab);
                        lab.setFont(new Font("Helvetica", 50));
                        lab.setTextFill(Color.WHITE);
                        lab.setWrapText(true);
                        setText = lab.getText();
                        slidePane.getChildren().addAll(backgroundRegion, lab);
                        displayPane.getChildren().addAll(slidePane);
                        questionClicked = true;
                    }
                }
            });

            teacherToolKit.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, evt -> {
                displayPane.getChildren().remove(slidePane);
                slidePane.getChildren().removeAll(backgroundRegion, lab);
                questionClicked = false;
            });

            int finalI = i;
            Animation animation = new Transition() {
                {
                    setCycleDuration(Duration.minutes(5));
                    setInterpolator(Interpolator.EASE_OUT);
                }

                @Override
                protected void interpolate(double frac) {
                    Color vColor = new Color(1, 0, 0, 0 + frac);
                    slides[finalI].setBackground(new Background(new BackgroundFill(vColor, CornerRadii.EMPTY, Insets.EMPTY)));
                }
            };
            animation.play();

        }
        sp.setContent(fp);
        bp.setCenter(sp);
        return bp;
    }

    protected BorderPane studentStats(List<Student> studentList) {
        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: #34495e");
        bp.setPadding(new Insets(0, 10, 10, 10));

        ScrollPane sp = new ScrollPane();
        sp.setMaxWidth(Double.MAX_VALUE);
        FlowPane fp = new FlowPane();
        fp.setPrefWrapLength(100);
        fp.setMaxWidth(Double.MAX_VALUE);
        fp.setPadding(new Insets(5, 0, 5, 0));
        fp.setVgap(4);
        fp.setHgap(4);
        fp.setStyle("-fx-background-color: whitesmoke");
        sp.setStyle("-fx-background-color: whitesmoke");


        Panel[] slides = new Panel[studentList.size()];

        for (int i = 0; i < studentList.size(); i++) {
            slides[i] = new Panel(studentList.get(i).getName());
            if (studentList.get(i).isOnline()) {
                numberOnline++;
            }
            Label tasksCompleted = new Label("Completed " + studentList.get(i).getQuestionsAnswered() + "/" + numberOfTestQuestions);
            VBox studentDetails = new VBox();
            studentDetails.getChildren().addAll(tasksCompleted);
            slides[i].setBody(studentDetails);
            //slides[i].getStyleClass().add("panel-primary");
            slides[i].setMinWidth(400);

            double questionPercentage = (studentList.get(i).getQuestionsAnswered() / (double) numberOfTestQuestions);
            System.out.println(questionPercentage);
            if (questionPercentage < 0.25) {
                slides[i].setStyle("-fx-background-color: red");
            } else if (questionPercentage < 0.5) {
                slides[i].setStyle("-fx-background-color: orange");
            } else if (questionPercentage < 0.75) {
                slides[i].setStyle("-fx-background-color: gold");
            } else {
                slides[i].setStyle("-fx-background-color: yellowgreen");
            }
            if (!studentList.get(i).isOnline()) {
                slides[i].setStyle("-fx-background-color: grey");
            }
        }

        Panel generalStats = new Panel("General Stats");
        generalStats.getStyleClass().add("panel-primary");
        Label online = new Label("Students Online: " + numberOnline);
        int numberOffline = studentList.size() - numberOnline;
        Label offline = new Label("Students Offline: " + numberOffline);
        VBox stats = new VBox();
        stats.getChildren().addAll(online, offline);
        generalStats.setBody(stats);
        generalStats.setMinWidth(400);
        fp.getChildren().add(generalStats);
        for (int i = 0; i < studentList.size(); i++) {

            fp.getChildren().add(slides[i]);
        }

        sp.setContent(fp);
        bp.setCenter(sp);
        return bp;
    }

    @Override
    protected void createCommentPanel() {
        commentPanel = new CommentPanel(true);
    }

    public List<String> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<String> questionList) {
        this.questionList = questionList;
    }

    private class Student {
        private String name;
        private int questionsAnswered;
        private boolean online;

        public Student(String name, int questionsAnswered, boolean online) {
            this.name = name;
            this.questionsAnswered = questionsAnswered;
            this.online = online;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQuestionsAnswered() {
            return questionsAnswered;
        }

        public void setQuestionsAnswered(int questionsAnswered) {
            this.questionsAnswered = questionsAnswered;
        }

        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }
    }
}
