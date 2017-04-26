package com.i2lp.edi.client.presentationElements;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.util.List;

/**
 * Created by Koen on 06/04/2017.
 */
public class PollElement extends InteractiveElement {
    protected String pollQuestion;
    protected List<String> possibleAnswers;
    protected boolean answered = false;
    protected boolean timerStart = false;
    protected int timeLimit = 20;
    protected HBox pollOptions;
    protected Timeline timeline;
    protected Panel questionPane;
    protected ResponseIndicator responseIndicator;
    private Label remainingTime;
    ToggleButton[] answerButton;

    public PollElement() {

    }

    @Override
    public void sendDataToServer() {

    }

    @Override
    public void receiveDataFromServer() {

    }

    @Override
    public void doClassSpecificRender() {
        //TODO: Need to figure out resizing behaviour
    }

    @Override
    public Node getCoreNode() {
        return questionPane;
    }

    @Override
    public void setupElement() {
        //Label question = new Label(pollQuestion);
        questionPane = new Panel(pollQuestion);

        questionPane.getStyleClass().add("panel-primary");
        //question.setMinWidth(Double.MAX_VALUE);


        //question.setStyle("-fx-background-color: blueviolet");
        //question.setAlignment(Pos.CENTER);
//        PauseTransition delay = new PauseTransition(Duration.seconds(10));
//        delay.setOnFinished(evt -> {
//            pollPane.getChildren().remove(answerSelection);

//        });

        remainingTime = new Label("Time Remaining: " + timeLimit);
        final IntegerProperty i = new SimpleIntegerProperty(timeLimit);
        timeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        evt -> {
                            i.set(i.get() - 1);
                            remainingTime.setText("Time Remaining: " + i.get());
                        }
                )
        );

        timeline.setCycleCount(timeLimit);
        timeline.setOnFinished(event -> {
            //pollPane.getChildren().remove(pollOptions);
            displayDone();
        });

        if(teacher && !timerStart) {
            Button startTimer = new Button("START");
            startTimer.getStyleClass().setAll("btn", "btn-default");
            startTimer.addEventHandler(MouseEvent.MOUSE_CLICKED, evt->{
                timerStart = true;
                responseIndicator = new ResponseIndicator();
                responseIndicator.setNumberOfStudents(20); //TODO: Get this from server
                pollOptions = new HBox();
                pollOptions.getChildren().setAll(responseIndicator, setUpQuestions(), remainingTime);
                pollOptions.setAlignment(Pos.TOP_CENTER);
                pollOptions.setSpacing(20);
                //pollPane.setCenter(pollOptions);
                questionPane.setBody(pollOptions);
                //delay.play();
                timeline.play();
            });
            //pollPane.setCenter(startTimer);
            questionPane.setBody(startTimer);
        }
    }

    @Override
    public void destroyElement() {

    }

    private VBox setUpQuestions(){
        answerButton = new ToggleButton[possibleAnswers.size()];
        final ToggleGroup group = new ToggleGroup();
        VBox answerSelection = new VBox();
        //answerSelection.setMaxWidth(slideCanvas.getMaxWidth());
        //answerSelection.setMaxHeight(slideCanvas.getMaxHeight());
        for (int i = 0; i < possibleAnswers.size(); i++) {
            final int number = i;
            answerButton[i] = new ToggleButton(possibleAnswers.get(i));
            //answerButton[i].setMinWidth(slideCanvas.getWidth());
            answerButton[i].getStyleClass().setAll("btn","btn-default");
            answerButton[i].addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
                responseIndicator.incrementResponses();
                System.out.println("Button " + number + " clicked!");
                checkIfDone();
            });
            answerSelection.getChildren().add(answerButton[i]);
            answerButton[i].setToggleGroup(group);
        }
        return answerSelection;
    }

    private void checkIfDone() {
        if(responseIndicator.isDone()) {
            timeline.stop();
            displayDone();
        }
    }

    private void displayDone() {
//        questionPane.getChildren().remove(pollOptions);
//        Label done = new Label("DONE");
//        //pollPane.setCenter(done);
//        questionPane.setBody(done);
        pollOptions.getChildren().remove(remainingTime);
        responseIndicator.setDone();
        for(int i=0; i < answerButton.length; i++) {
            answerButton[i].setDisable(true);
        }
    }

    public String getPollQuestion() {
        return pollQuestion;
    }

    public void setPollQuestion(String pollQuestion) {
        this.pollQuestion = pollQuestion;
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


    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

}
