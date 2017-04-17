package com.i2lp.edi.client.presentationElements;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

/**
 * Created by Koen on 06/04/2017.
 */
public class PollElement extends InteractiveElement {
    protected BorderPane pollPane = new BorderPane();
    protected String pollQuestion;
    protected List<String> possibleAnswers;
    protected boolean answered = false;
    protected boolean teacher = false;
    protected boolean timerStart = false;
    protected int timeLimit = 20;
    protected HBox pollOptions;
    protected Timeline timeline;

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


        startAnimation = new Animation();
        startAnimation.setCoreNodeToAnimate(getCoreNode());
        startAnimation.setAnimationType(Animation.SIMPLE_APPEAR);
        endAnimation = new Animation();
        endAnimation.setCoreNodeToAnimate(getCoreNode());
        endAnimation.setAnimationType(Animation.SIMPLE_DISAPPEAR);
    }

    @Override
    public Node getCoreNode() {
        return pollPane;
    }

    @Override
    public void setupElement() {
        pollPane = new BorderPane();
        pollPane.getStylesheets().add("bootstrapfx.css");
        Label question = new Label(pollQuestion);
        //question.setMinWidth(Double.MAX_VALUE);


        //question.setStyle("-fx-background-color: blueviolet");
        //question.setAlignment(Pos.CENTER);
//        PauseTransition delay = new PauseTransition(Duration.seconds(10));
//        delay.setOnFinished(evt -> {
//            pollPane.getChildren().remove(answerSelection);

//        });
        Label remainingTime = new Label("Time Remaining: "+ timeLimit);
        final IntegerProperty i = new SimpleIntegerProperty(timeLimit);
        timeline = new Timeline(
            new KeyFrame(
                    Duration.seconds(1),
                    evt->{
                        i.set(i.get()-1);
                        remainingTime.setText("Time Remaining: "+i.get());
                    }
            )
        );
        timeline.setCycleCount(timeLimit);
        timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                pollPane.getChildren().remove(pollOptions);
                Label done = new Label("DONE");
                pollPane.setCenter(done);
            }
        });

        if(!teacher){
            Button startTimer = new Button("START");
            startTimer.getStyleClass().setAll("btn", "btn-default");
            startTimer.addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
                timerStart = true;
                pollOptions= new HBox();
                pollOptions.getChildren().addAll(setUpQuestions(),remainingTime);
                pollPane.setCenter(pollOptions);
                //delay.play();
                timeline.play();
            });
            pollPane.setCenter(startTimer);

        }
        pollPane.setTop(question);


    }

    @Override
    public void destroyElement() {

    }

    private VBox setUpQuestions(){
        ToggleButton[] answerButton = new ToggleButton[possibleAnswers.size()];
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
                System.out.println("Button " + number + " clicked!");
            });
            answerSelection.getChildren().add(answerButton[i]);
            answerButton[i].setToggleGroup(group);
        }
        return answerSelection;
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
