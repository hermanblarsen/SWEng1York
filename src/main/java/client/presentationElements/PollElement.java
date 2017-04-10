package client.presentationElements;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
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
    VBox answerSelection;


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
        Label question = new Label(pollQuestion);
        question.setMinWidth(slideCanvas.getMaxWidth());

        question.setStyle("-fx-background-color: blueviolet");
        //question.setAlignment(Pos.CENTER);
        PauseTransition delay = new PauseTransition(Duration.seconds(10));
        delay.setOnFinished(evt -> {
            pollPane.getChildren().remove(answerSelection);
            Label done = new Label("DONE");
            pollPane.setCenter(done);
        });

        if(!teacher){
            Button startTimer = new Button("START");
            startTimer.addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
                timerStart = true;
                setUpQuestions();
                delay.play();
            });
            pollPane.setCenter(startTimer);

        }
        pollPane.setTop(question);


    }

    @Override
    public void destroyElement() {

    }

    private void setUpQuestions(){
        Button[] answerButton = new Button[possibleAnswers.size()];
        answerSelection = new VBox();
        //answerSelection.setMaxWidth(slideCanvas.getMaxWidth());
        //answerSelection.setMaxHeight(slideCanvas.getMaxHeight());
        for (int i = 0; i < possibleAnswers.size(); i++) {
            final int number = i;
            answerButton[i] = new Button(possibleAnswers.get(i));
            answerButton[i].setMinWidth(slideCanvas.getWidth());
            answerButton[i].addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
                System.out.println("Button " + number + " clicked!");
            });
            answerSelection.getChildren().add(answerButton[i]);
        }
        pollPane.setCenter(answerSelection);
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
}
