package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.presentationViewerElements.CommentPanel;
import com.i2lp.edi.client.utilities.CursorState;
import com.i2lp.edi.server.packets.Question;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.sql.Timestamp;
import java.util.*;


/**
 * Created by kma517 on 16/03/2017.
 */

/**
 * Presentation Manager for teachers, extending PresentationManager
 * Displays slides with a specific set of tools for teachers.
 */
public class PresentationManagerTeacher extends PresentationManager {
    protected boolean questionClicked = false;
    protected boolean toolkitOpen = false;
    protected boolean test = false;
    protected boolean firstRun = false;
    protected String setText;
    protected List<Student> studentList;
    protected Stage teacherToolKitStage;
    private Double toolKitStageX, toolKitStageY;
    private int numberOfTestQuestions = 10;
    private ArrayList<Question> newQuestionList;
    private boolean buttonActive = false;
    private Tab questions;
    private Panel[] slides;
    private StackPane slidePane;
    private FlowPane fp;
    private ScrollPane sp;
    private ScrollPane sp2;
    private Label lab;
    private Region backgroundRegion;
    private Tab studentStats;
    private Label questionNumberLabel;
    private int questionID;
    private Timer toolkitTimer;
    //private Timestamp openTime;

    public PresentationManagerTeacher(EdiManager ediManager) {
        super(ediManager);
    }

    private void setListOfQuestions() {
        if (slidePane != null && !firstRun) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    displayPane.getChildren().remove(slidePane);
                    slidePane.getChildren().removeAll(backgroundRegion, lab);
                    if (!test) {
                        lab = new Label(slides[questionID].getText());
                        lab.setFont(new Font("Helvetica", 50));
                        lab.setTextFill(Color.WHITE);
                        lab.setWrapText(true);
                        setText = lab.getText();
                        slidePane.setPrefSize(slideWidth, slideHeight);
                        slidePane.getChildren().addAll(backgroundRegion, lab);
                        displayPane.getChildren().addAll(slidePane);
                    } else {
                        test = false;
                    }
                    questionClicked = true;
                }
            });

        }
        if (getTeacherSession() != null) {
            newQuestionList = getTeacherSession().getQuestionQueue();
        }
        if (newQuestionList == null) {
            newQuestionList = new ArrayList<>();
        }
        for (Question question : newQuestionList) {
            logger.info("Question: " + question.getQuestion_data());
        }
    }

    @Override
    protected void loadSpecificFeatures() {
        if (!toolkitOpen) {
            teacherToolKitStage = new Stage();
            if (toolKitStageX != null) {
                teacherToolKitStage.setX(toolKitStageX);
            }
            if (toolKitStageY != null) {
                teacherToolKitStage.setY(toolKitStageY);
            }
            teacherToolKitStage.initStyle(StageStyle.UTILITY);
            teacherToolKitStage.setTitle("Teacher toolkit");
            TabPane tp = new TabPane();
            tp.setStyle("-fx-background-color: #34495e");
            Scene toolkitScene = new Scene(tp, 450, 450);
            toolkitScene.getStylesheets().add("bootstrapfx.css");
            toolkitScene.addEventFilter(KeyEvent.ANY, event -> {
                Event.fireEvent(scene, event.copyFor(event.getSource(), scene));
                event.consume();
            });

            toolkitTimer = new Timer();
            TimerTask toolkitUpdateTask = new TimerTask() {
                @Override
                public void run() {
                    updateQuestionList();
                    updateStudentList();
                }
            };
            toolkitTimer.schedule(toolkitUpdateTask, 5000, 10000);
            firstRun = true;
            questions = new Tab();
            questions.setText("Question Queue");
            setListOfQuestions();
            questions.setContent(questionQueueFunction(newQuestionList));

            tp.getTabs().add(questions);

            studentStats = new Tab();
            studentStats.setText("Students");
            studentList = generateStudentList();
            studentStats.setContent(studentStats(studentList));
            tp.getTabs().add(studentStats);

            teacherToolKitStage.setScene(toolkitScene);
            teacherToolKitStage.show();
            tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            toolkitOpen = true;

            backgroundRegion = new Region();
            backgroundRegion.setBackground(new Background(new BackgroundFill(Color.web("#34495e"), null, null)));

            teacherToolKitStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, evt -> toolkitOpen = false);
        } else {
            toolKitStageX = teacherToolKitStage.getX();
            toolKitStageY = teacherToolKitStage.getY();
            teacherToolKitStage.close();
            toolkitOpen = false;
        }

    }

    protected ArrayList<Student> generateStudentList() {
        int numberOfStudents = 0;
        if (presentationElement.getPresentationMetadata().getLive()) {
            if (getTeacherSession().getActiveUsers() != null) {
                numberOfStudents = getTeacherSession().getActiveUsers().size();
            }
        }
        ArrayList<Student> studentList = new ArrayList<>();
        for (int i = 0; i < numberOfStudents; i++) {
            String studentName = getTeacherSession().getActiveUsers().get(i).getFirstName() + " " + getTeacherSession().getActiveUsers().get(i).getSecondName();
            Student newStudent = new Student(studentName, 10, true);//Todo add interaction stuff once it is ready
            studentList.add(newStudent);
        }
        return studentList;
    }

    protected BorderPane questionQueueFunction(List<Question> questions) {

        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: #34495e");
        bp.setPadding(new Insets(0, 10, 10, 10));

        sp = new ScrollPane();
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setMaxWidth(Double.MAX_VALUE);

        sp.setStyle("-fx-background-color: whitesmoke");

        setUpQuestionList(questions);

        bp.setCenter(sp);
        return bp;
    }

    private void setUpQuestionList(List<Question> questions) {
        slides = new Panel[questions.size()];
        slidePane = new StackPane();
        slidePane.setPickOnBounds(false);
        if (sp != null) {
            sp.setContent(null);
        }
        fp = new FlowPane();
        fp.setPrefWrapLength(100);
        fp.setMaxWidth(Double.MAX_VALUE);
        fp.setPadding(new Insets(5, 0, 5, 0));
        fp.setVgap(4);
        fp.setHgap(4);
        fp.setStyle("-fx-background-color: whitesmoke");

        for (int i = 0; i < questions.size(); i++) {
            slides[i] = new Panel(questions.get(i).getQuestion_data());
            final int j = i;
            slides[i].setMinWidth(400);
            slides[i].setPickOnBounds(false);
            fp.getChildren().add(slides[i]);
            Image answered = new Image("file:projectResources/icons/Tick.png", 50, 50, true, true);
            ImageView answeredView = new ImageView(answered);

            answeredView.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
                test = true;
                if (lab.getText() == slides[j].getText()) {
                    displayPane.getChildren().remove(slidePane);
                    slidePane.getChildren().removeAll(backgroundRegion, lab);
                }
                //buttonActive = true;
                ediManager.getSocketClient().answerQuestionInQuestionQueue(questions.get(j).getQuestion_id());
                Label questionComplete = new Label("Question Answered!");
                logger.info("Question: " + j + " Answered");
                slides[j].setBody(questionComplete);
                evt.consume();
            });

            slides[i].setBody(answeredView);

            setText = new String();
            final int val = i;
            slides[i].addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {

                if (!questionClicked) {
                    displayPane.getChildren().remove(slidePane);
                    lab = new Label(slides[val].getText());
                    slidePane.getChildren().removeAll(backgroundRegion, lab);
                    lab.setFont(new Font("Helvetica", 50));
                    lab.setTextFill(Color.WHITE);
                    lab.setWrapText(true);
                    setText = lab.getText();
                    slidePane.setPrefSize(slideWidth, slideHeight);
                    if (!buttonActive) {
                        slidePane.getChildren().addAll(backgroundRegion, lab);
                        displayPane.getChildren().addAll(slidePane);
                        questionID = val;
                    }
                    questionClicked = true;
                } else {
                    if (Objects.equals(lab.getText(), setText)) {
                        slidePane.getChildren().removeAll(backgroundRegion, lab);
                        displayPane.getChildren().remove(slidePane);

                        questionClicked = false;
                    } else {
                        displayPane.getChildren().remove(slidePane);
                        slidePane.getChildren().removeAll(backgroundRegion, lab);
                        lab.setFont(new Font("Helvetica", 50));
                        lab.setTextFill(Color.WHITE);
                        lab.setWrapText(true);
                        setText = lab.getText();
                        if (!buttonActive) {
                            slidePane.getChildren().addAll(backgroundRegion, lab);
                            displayPane.getChildren().addAll(slidePane);
                            questionID = val;
                        }
                        questionClicked = true;
                    }
                }
            });
            presentationStage.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
                displayPane.getChildren().remove(slidePane);
                slidePane.getChildren().removeAll(backgroundRegion, lab);
                questionClicked = false;
            });
            if (teacherToolKitStage != null) {
                teacherToolKitStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, evt -> {
                    displayPane.getChildren().remove(slidePane);
                    slidePane.getChildren().removeAll(backgroundRegion, lab);
                    questionClicked = false;

                });
            }

            Date date = new Date();
            long time = date.getTime();
            Timestamp ts = new Timestamp(time);

            long timeTaken = ts.getTime() - newQuestionList.get(i).getTime_created().getTime();
            long oneMin = 60000;
            logger.info("TIME TAKEN: " + timeTaken);
            if (timeTaken <= oneMin) {
                slides[i].setStyle("-fx-background-color: green");
            } else if (timeTaken > oneMin && timeTaken < oneMin * 2) {

                slides[i].setStyle("-fx-background-color: greenyellow");
            } else if (timeTaken >= oneMin * 2 && timeTaken < oneMin * 3) {

                slides[i].setStyle("-fx-background-color: yellowgreen");
            } else if (timeTaken >= oneMin * 3 && timeTaken < oneMin * 3) {

                slides[i].setStyle("-fx-background-color: orange");
            } else if (timeTaken >= oneMin * 4 && timeTaken < oneMin * 5) {
                slides[i].setStyle("-fx-background-color: orangered");
            } else {
                slides[i].setStyle("-fx-background-color: red");
            }
        }
        if (sp != null) {
            sp.setContent(fp);
        }
    }

    private BorderPane studentStats(List<Student> studentList) {
        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: #34495e");
        bp.setPadding(new Insets(0, 10, 10, 10));

        sp2 = new ScrollPane();
        sp2.setMaxWidth(Double.MAX_VALUE);
        sp2.setStyle("-fx-background-color: whitesmoke");

        setUpStudentList(studentList);

        bp.setCenter(sp2);
        return bp;
    }

    private void setUpStudentList(List<Student> studentList) {
        FlowPane fp = new FlowPane();
        fp.setPrefWrapLength(100);
        fp.setMaxWidth(Double.MAX_VALUE);
        fp.setPadding(new Insets(5, 0, 5, 0));
        fp.setVgap(4);
        fp.setHgap(4);
        fp.setStyle("-fx-background-color: whitesmoke");


        Panel[] slides = new Panel[studentList.size()];

        for (int i = 0; i < studentList.size(); i++) {
            slides[i] = new Panel(studentList.get(i).getName());
            slides[i].setMinWidth(400);

            double questionPercentage = (studentList.get(i).getQuestionsAnswered() / (double) numberOfTestQuestions);
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

        for (int i = 0; i < studentList.size(); i++) {
            fp.getChildren().add(slides[i]);
        }

        sp2.setContent(fp);
    }

    @Override
    protected void createCommentPanel() {
        commentPanel = new CommentPanel(true);
    }

    @Override
    protected VBox addQuestionQueueControls() {
        if (teacherSession != null) {
            if (newQuestionList != null) {
                questionNumberLabel = new Label(Integer.toString(newQuestionList.size()));
            } else {
                questionNumberLabel = new Label("0");
            }
            questionNumberLabel.setMouseTransparent(true);
            questionNumberLabel.getStyleClass().add("b");

            ImageView questionBase = new ImageView(new Image("file:projectResources/icons/empty_icon.png", DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT, true, true));
            ImageView questionIcon = makeCustomButton("file:projectResources/icons/Question_Filled.png", event -> {
                if (!questionQueueActive) {
                    loadSpecificFeatures();
                    questionQueueActive = true;
                } else {
                    loadSpecificFeatures();
                    questionQueueActive = false;
                }
            });
            questionIcon.setOpacity(0);
            questionIcon.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                if (!mouseDown) {
                    controlsFadeIn(questionIcon);
                    controlsFadeOut(questionNumberLabel);
                    isMouseOverControls = true;
                    setCursorState(CursorState.DEFAULT);
                }
            });
            questionIcon.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                controlsFadeOut(questionIcon);
                controlsFadeIn(questionNumberLabel);
                hideControlsTimed(presControls);
                hideControlsTimed(drawControls);
                isMouseOverControls = false;
            });

            StackPane questionQueueButton = new StackPane(questionBase, questionIcon, questionNumberLabel);
            questionQueueButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                if (!mouseDown) {
                    isMouseOverControls = true;
                    setCursorState(CursorState.DEFAULT);
                }
            });
            questionQueueButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> isMouseOverControls = false);

            VBox contentVBox = new VBox();
            contentVBox.setAlignment(Pos.TOP_CENTER);
            contentVBox.setPadding(new Insets(5));

            contentVBox.getChildren().add(questionQueueButton);

            return contentVBox;
        } else {
            return new VBox();
        }
    }

    private void controlsFadeIn(Node controls) {
        FadeTransition ft0 = new FadeTransition(Duration.millis(500), controls);
        ft0.setFromValue(controls.getOpacity());
        ft0.setToValue(1.0);
        ft0.play();
    }

    public void updateQuestionList() {
        setListOfQuestions();
        Platform.runLater(() -> setUpQuestionList(newQuestionList));
        Platform.runLater(() -> notifyQuestionQueueUI());
    }

    private void notifyQuestionQueueUI() {
        questionNumberLabel.setText(Integer.toString(newQuestionList.size()));
        logger.info("Setting question queue number to " + newQuestionList.size());
    }

    public void updateStudentList() {
        logger.info("STUDENT LIST UPDATE");
        studentList = generateStudentList();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (teacherToolKitStage != null) {
                    setUpStudentList(studentList);
                }
            }
        });
    }

    @Override
    protected void doCloseSequence() {
        if (toolkitTimer != null) {
            toolkitTimer.cancel();
        }
        if (teacherToolKitStage != null) {
            teacherToolKitStage.close();
        }
        super.doCloseSequence();
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
