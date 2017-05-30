package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.PresentationSession;
import com.i2lp.edi.client.presentationViewerElements.CommentPanel;
import com.i2lp.edi.server.packets.PresentationMetadata;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.apache.commons.net.ntp.TimeStamp;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
    boolean buttonActive = false;
    //private Timestamp openTime;

    public PresentationManagerTeacher(EdiManager ediManager) {
        super(ediManager);
    }

    private void setListOfQuestions(){
        if(getPresentationSession() != null) {
            newQuestionList = getPresentationSession().getQuestionQueue();
        }
        if(newQuestionList == null){
            System.out.println("NULL QUESTION");
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
            studentList = generateStudentList();
            studentStats.setContent(studentStats(studentList));
            tp.getTabs().add(studentStats);

            teacherToolKit.setScene(scene);
            teacherToolKit.show();
            tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            toolkitOpen = true;

            teacherToolKit.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, evt -> toolkitOpen = false);
        }

    }

    protected ArrayList<Student> generateStudentList(){
        int numberOfStudents = 0;
        if(presentationElement.getPresentationMetadata().getLive()) {
            if(getPresentationSession().getActiveUsers() != null) {
                numberOfStudents = getPresentationSession().getActiveUsers().size();
            }
        }
        ArrayList<Student> studentList = new ArrayList<>();
        for(int i = 0;i<numberOfStudents;i++){
            String studentName = getPresentationSession().getActiveUsers().get(i).getFirstName()+" "+getPresentationSession().getActiveUsers().get(i).getSecondName();
            Student newStudent = new Student(studentName,10,true);//Todo add interaction stuff once it is ready
            studentList.add(newStudent);
        }
        return  studentList;
    }

    protected BorderPane questionQueueFunction(List<Question> questions) {

        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: #34495e");
        bp.setPadding(new Insets(0, 10, 10, 10));

        ScrollPane sp = new ScrollPane();
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
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
            Image answered = new Image("file:projectResources/icons/Tick.png",30,30,true,true);
            ImageView answeredView = new ImageView(answered);
            final int j= i;
            answeredView.addEventHandler(MouseEvent.MOUSE_CLICKED, evt->{
                buttonActive = true;
                ediManager.getSocketClient().answerQuestionInQuestionQueue(questions.get(0).getQuestion_id());
                Label questionComplete = new Label("Question Answered!");
                logger.info("Question: "+j+" Answered");
                slides[j].setBody(questionComplete);
                buttonActive = false;
            });
            answeredView.addEventHandler(MouseEvent.MOUSE_ENTERED,evt->{
                buttonActive = true;
            });
            answeredView.addEventHandler(MouseEvent.MOUSE_EXITED,evt->{
                buttonActive = false;
            });
            slides[i].setBody(answeredView);
            //slides[i].getStyleClass().add("panel-primary");
            slides[i].setMinWidth(400);
            fp.getChildren().add(slides[i]);
            Label lab = new Label(slides[i].getText());
            Region backgroundRegion = new Region();
            backgroundRegion.setBackground(new Background(new BackgroundFill(Color.web("#34495e"), null, null)));
            setText = new String();

                slides[i].addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
                    if(!buttonActive) {
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
                    }
                });

            teacherToolKit.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, evt -> {
                displayPane.getChildren().remove(slidePane);
                slidePane.getChildren().removeAll(backgroundRegion, lab);
                questionClicked = false;

            });

            Date date = new Date();
            long time = date.getTime();
            Timestamp ts = new Timestamp(time);

            long timeTaken = ts.getTime() - newQuestionList.get(i).getTime_created().getTime();
            long oneMin = 60000;
            logger.info("TIME TAKEN: "+timeTaken);
            if(timeTaken <= oneMin){
                System.out.println(">ONE MIN");
              //  slides[i].setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                slides[i].setStyle("-fx-background-color: green");
            }else if(timeTaken> oneMin && timeTaken< oneMin*2){
                System.out.println("ONE MIN");
              //  slides[i].setBackground(new Background(new BackgroundFill(Color.GREENYELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
                slides[i].setStyle("-fx-background-color: greenyellow");
            }else if(timeTaken>= oneMin*2 && timeTaken< oneMin*3){
                System.out.println("TWO MIN");
               // slides[i].setBackground(new Background(new BackgroundFill(Color.YELLOWGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                slides[i].setStyle("-fx-background-color: yellowgreen");
            }else if(timeTaken>= oneMin*3 && timeTaken< oneMin*3){
                System.out.println("THREE MIN");
               // slides[i].setBackground(new Background(new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY, Insets.EMPTY)));
                slides[i].setStyle("-fx-background-color: orange");
            }else if(timeTaken>= oneMin*4 && timeTaken< oneMin*5){
                System.out.println("FOUR MIN");
               // slides[i].setBackground(new Background(new BackgroundFill(Color.DARKORANGE, CornerRadii.EMPTY, Insets.EMPTY)));
                slides[i].setStyle("-fx-background-color: orangered");
            }else{
                System.out.println("FIVE MIN");
                //slides[i].setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
                slides[i].setStyle("-fx-background-color: red");
            }
//            int finalI = i;
//            Animation animation = new Transition() {
//                {
//                    setCycleDuration(Duration.minutes(5));
//                    setInterpolator(Interpolator.EASE_OUT);
//                }
//
//
//                @Override
//                protected void interpolate(double frac) {
//                    Color vColor = new Color(1, 0, 0, 0 + frac);
//                    slides[finalI].setBackground(new Background(new BackgroundFill(vColor, CornerRadii.EMPTY, Insets.EMPTY)));
//                }
//            };
//            animation.play();

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
