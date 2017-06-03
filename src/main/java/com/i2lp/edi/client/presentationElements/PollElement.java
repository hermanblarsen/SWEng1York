package com.i2lp.edi.client.presentationElements;

import edu.emory.mathcs.backport.java.util.Arrays;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.tools.ChartData;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Koen on 06/04/2017.
 */
public class PollElement extends InteractiveElement {
    protected String question;
    protected List<String> possibleAnswers;
    protected List<String> pollOutput;
    protected String answers;
    protected boolean answered = false;
    protected boolean timerStart = false;
    protected LocalTime startTime;

    protected VBox pollOptions;
    protected Timeline timeline;
    protected Panel questionPane;
    //protected ResponseIndicator responseIndicator;
    protected Label remainingTime;
    private Tile countdownTile;
    protected Tile answerOutputTile;
    protected ToggleButton[] answerButton;
    private ChartData[] chartDataArray;
    private Color assignedColour;
    protected float xSize = 0.8f;
    protected float ySize = 0.8f;
    protected float xPosition = 0.1f;
    protected float yPosition = 0.1f;
    protected boolean buttonActive = false;
    protected int setValue;
    protected Button startTimer;
    protected ContextMenu cm;

    public PollElement() {

    }

    @Override
    public void doClassSpecificRender() {
        if (xSize == 0 || ySize == 0) {
            questionPane.setPrefHeight(slideHeight);
            questionPane.setPrefWidth(slideWidth);
        } else {
            questionPane.setPrefHeight(slideHeight * ySize);
            questionPane.setPrefWidth(slideWidth * xSize);
        }

        questionPane.setTranslateX(slideWidth * xPosition);
        questionPane.setTranslateY(slideHeight * yPosition);

        getCoreNode().addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            if (this.isElementActive() == false && this.isButtonActive() == false) {
                performOnClickAction();
            }
        });
    }

    @Override
    public Node getCoreNode() {
        return questionPane;
    }

    @Override
    public void setupElement() {

        questionPane = new Panel();
        setUpQuestionList(answers);
        //questionPane.getStyleClass().add("panel-primary");
        //questionPane.setBackground(new Background(new BackgroundFill(Tile.BACKGROUND, null, null)));
        questionPane.setStyle("-fx-background-color: #2a2a2a");
        //question.setMinWidth(Double.MAX_VALUE);


        //question.setStyle("-fx-background-color: blueviolet");
        //question.setAlignment(Pos.CENTER);
//        PauseTransition delay = new PauseTransition(Duration.seconds(10));
//        delay.setOnFinished(evt -> {
//            pollPane.getChildren().remove(answerSelection);

//        });



        countdownTile = TileBuilder.create()
                .skinType(Tile.SkinType.NUMBER)
                .prefSize((xSize * slideWidth) / 5, (ySize * slideHeight) / 2)
                .title("Time Limit")
                .value(timeLimit)
                .description("Seconds")
                .descriptionAlignment(Pos.BASELINE_RIGHT)
                .build();
        answerOutputTile = TileBuilder.create()
                .skinType(Tile.SkinType.RADIAL_CHART)
                .prefSize((xSize * slideWidth), (ySize * slideHeight))
                .title("Responses")
                .build();
        if (teacher && !timerStart) {
            Image startPollIcon = new Image("file:projectResources/icons/StartPoll.png");
            ImageView startPoll = new ImageView(startPollIcon);
            HBox startBox = new HBox();
            startBox.getChildren().add(startPoll);
            startBox.setAlignment(Pos.CENTER);
            //startTimer = new Button("START");
            //startTimer.getStyleClass().setAll("btn", "btn-default");
            startPoll.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
                setUpPollData(new Time(Instant.now().toEpochMilli()));
                if (ediManager.getPresentationManager().getTeacherSession()!= null) {
                    ediManager.getPresentationManager().getTeacherSession().beginInteraction(this, true);
                }
            });
            startPoll.addEventHandler(MouseEvent.MOUSE_ENTERED, evt -> {
                buttonActive = true;
            });
            startPoll.addEventHandler(MouseEvent.MOUSE_EXITED, evt -> {
                buttonActive = false;
            });
            //pollPane.setCenter(startTimer);
            questionPane.setBody(startBox);
        }
        questionPane.setVisible(visibility);
    }

    @Override
    public void destroyElement() {

    }

    public void setUpPollData(Time startTime) {
        this.startTime = startTime.toLocalTime();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                LocalTime currentTime = LocalTime.now();
                int timeDifference = currentTime.getSecond()-startTime.toLocalTime().getSecond();
                int newTimeLimit = Math.round(timeLimit-timeDifference);

                remainingTime = new Label("Time Remaining: " + newTimeLimit);
                final IntegerProperty i = new SimpleIntegerProperty(newTimeLimit);
                timeline = new Timeline(
                        new KeyFrame(
                                Duration.seconds(1),
                                evt -> {
                                    i.set(i.get() - 1);
                                    //double percentage = ((i.get()/timeLimit)*100);
                                    countdownTile.setMaxValue(newTimeLimit);
                                    countdownTile.setValue(i.get());

                                    remainingTime.setText("Time Remaining: " + i.get());
                                }
                        )
                );

                timeline.setCycleCount(newTimeLimit);
                timeline.setOnFinished(event -> {
                    elementActive = false;
                    //displayDone();
                });


                elementActive = true;
                timerStart = true;
                if(countdownTile.getValue() == 0.0){
                    countdownTile.setValue(newTimeLimit);
                }
                Label pollQuestion = new Label(question);
                pollQuestion.setFont(new javafx.scene.text.Font("Helvetica", 50));
                pollQuestion.setTextFill(javafx.scene.paint.Color.WHITE);
                pollQuestion.setWrapText(true);
                //responseIndicator = new ResponseIndicator();
                //responseIndicator.setNumberOfStudents(20); //TODO: Get this from server
                pollOptions = new VBox();
                pollOptions.getChildren().setAll(pollQuestion,countdownTile, setUpQuestions());
                pollOptions.setAlignment(Pos.CENTER);
                pollOptions.setSpacing(20);
                //pollPane.setCenter(pollOptions);
                questionPane.setBody(pollOptions);
                //delay.play();
                timeline.play();
            }
        });
    }

    private HBox setUpQuestions() {
        elementActive = true;
        answerButton = new ToggleButton[possibleAnswers.size()];
        final ToggleGroup group = new ToggleGroup();
        HBox answerSelection = new HBox();
        //answerSelection.setMaxWidth(slideCanvas.getMaxWidth());
        //answerSelection.setMaxHeight(slideCanvas.getMaxHeight());
        for (int i = 0; i < possibleAnswers.size(); i++) {
            final int number = i;
            //chartDataArray[i].setName(possibleAnswers.get(i));
            answerButton[i] = new ToggleButton(possibleAnswers.get(i));
            //answerButton[i].setMinWidth(slideCanvas.getWidth());
            answerButton[i].getStyleClass().setAll("btn", "btn-default");
            int finalI = i;
            answerButton[i].addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
                //responseIndicator.incrementResponses();
                //checkIfDone();
                setValue = number;
                if((ediManager.getPresentationManager().getTeacherSession()) != null){
                    ediManager.getPresentationManager().getTeacherSession().sendResponse(this, Integer.toString(finalI));
                } else if((ediManager.getPresentationManager().getStudentSession() != null)){
                    ediManager.getPresentationManager().getStudentSession().sendResponse(this, Integer.toString(finalI));
                }
            });
            answerButton[i].addEventHandler(MouseEvent.MOUSE_ENTERED, evt -> {
                buttonActive = true;
            });
            answerButton[i].addEventHandler(MouseEvent.MOUSE_EXITED, evt -> {
                buttonActive = false;
            });
            answerSelection.getChildren().add(answerButton[i]);
            answerButton[i].setToggleGroup(group);
        }
        answerSelection.setAlignment(Pos.CENTER);
        return answerSelection;
    }

    public void displayDone() {

        double testShit[] = new double[possibleAnswers.size()];
        chartDataArray = new ChartData[possibleAnswers.size()];
        questionPane.getChildren().remove(questionPane.getBody());
        for (int i = 0; i < possibleAnswers.size(); i++) {
            chartDataArray[i] = new ChartData(possibleAnswers.get(i), 0);
            chartDataArray[i].setColor(assignBarColour(i));
        }

        for(int i=0; i<pollOutput.size();i++){
            testShit[Integer.parseInt(pollOutput.get(i))]++;
        }

        for(int i=0; i<possibleAnswers.size();i++){
            chartDataArray[i].setValue(testShit[i]);
        }

        answerOutputTile.setRadialChartData(chartDataArray);
        questionPane.setBody(answerOutputTile);

        if(ediManager.getPresentationManager().getTeacherSession() != null) {
            answerOutputTile.addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
                if(cm != null){
                    cm.hide();
                }
                cm = new ContextMenu();


                if(evt.getButton() == MouseButton.SECONDARY){
                    MenuItem reset = new MenuItem("Reset Element");
                    reset.setOnAction(event->{
                        ediManager.getPresentationManager().getTeacherSession().resetInteractiveElement(this);
                    });
                    cm.getItems().add(reset);
                    cm.show(this.getCoreNode(),evt.getScreenX(),evt.getScreenY());
                }
            });
        }
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getPossibleAnswers() {
        return possibleAnswers;
    }

    public void setPossibleAnswers(List<String> possibleAnswers) {
        this.possibleAnswers = possibleAnswers;
    }

    public boolean isTeacher() {
        return teacher;
    }

    public void setTeacher(boolean teacher) {
        this.teacher = teacher;
    }

    public boolean isTimerStart() {
        return timerStart;
    }

    public void setTimerStart(boolean timerStart) {
        this.timerStart = timerStart;
    }


    private Color assignBarColour(int i) {
        i = i % possibleAnswers.size();
        //assignedColour = new Color();
        //for(int j = i; j<possibleAnswers.size(); j++){
        switch (i) {
            case 1:
                assignedColour = Tile.BLUE;
                break;
            case 8:
                assignedColour = Tile.DARK_BLUE;
                break;
            case 6:
                assignedColour = Tile.GREEN;
                break;
            case 2:
                assignedColour = Tile.LIGHT_GREEN;
                break;
            case 3:
                assignedColour = Tile.LIGHT_RED;
                break;
            case 5:
                assignedColour = Tile.MAGENTA;
                break;
            case 9:
                assignedColour = Tile.ORANGE;
                break;
            case 7:
                assignedColour = Tile.RED;
                break;
            case 4:
                assignedColour = Tile.YELLOW;
                break;
            case 0:
                assignedColour = Tile.YELLOW_ORANGE;
                break;
            default:
                assignedColour = Tile.RED;
                break;
        }
        // Random rand = new Random();
        //int n = rand.nextInt(10)+1;

        return assignedColour;
    }

    public float getxSize() {
        return xSize;
    }

    public void setxSize(float xSize) {
        this.xSize = xSize;
    }

    public float getySize() {
        return ySize;
    }

    public void setySize(float ySize) {
        this.ySize = ySize;
    }

    public float getxPosition() {
        return xPosition;
    }

    public void setxPosition(float xPosition) {
        this.xPosition = xPosition;
    }

    public float getyPosition() {
        return yPosition;
    }

    public void setyPosition(float yPosition) {
        this.yPosition = yPosition;
    }

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public void setPollOutput(ArrayList<String> answers) {
        pollOutput = answers;
    }

    public void setUpQuestionList(String answers) {
        possibleAnswers = new ArrayList<String>();
        possibleAnswers = Arrays.asList(answers.split(","));
    }


    public boolean isButtonActive() {
        return buttonActive;
    }

}
