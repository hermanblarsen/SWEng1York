package com.i2lp.edi.client.presentationElements;

import edu.emory.mathcs.backport.java.util.Arrays;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.tools.ChartData;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.DefaultProperty;
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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Koen on 06/04/2017.
 */
public class PollElement extends InteractiveElement {
    protected String pollQuestion;
    protected List<String> possibleAnswers;
    protected String answers;
    protected boolean answered = false;
    protected boolean timerStart = false;
    protected int timeLimit = 20;
    protected HBox pollOptions;
    protected Timeline timeline;
    protected Panel questionPane;
    //protected ResponseIndicator responseIndicator;
    private Label remainingTime;
    private Tile countdownTile;
    private Tile answerOutputTile;
    private ToggleButton[] answerButton;
    private ChartData[] chartDataArray;
    private Color assignedColour;
    protected float xSize= 0;
    protected float ySize= 0;
    protected float xPosition=0;
    protected float yPosition=0;

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
        if (xSize == 0 || ySize == 0) {
            questionPane.setPrefHeight(slideHeight);
            questionPane.setPrefWidth(slideWidth);
        } else {
            questionPane.setPrefHeight(slideHeight * ySize);
            questionPane.setPrefWidth(slideWidth * xSize);
        }

        questionPane.setTranslateX(slideWidth * xPosition);
        questionPane
                .setTranslateY(slideHeight * yPosition);

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
       //questionPane.setBackground(new Background(new BackgroundFill(Tile.BACKGROUND, null, null)));
        questionPane.setStyle("-fx-background-color: #2a2a2a");
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
                            //double percentage = ((i.get()/timeLimit)*100);
                            //System.out.println("Test "+percentage);
                            countdownTile.setMaxValue(timeLimit);
                            countdownTile.setValue(i.get());

                            remainingTime.setText("Time Remaining: " + i.get());
                        }
                )
        );

        timeline.setCycleCount(timeLimit);
        timeline.setOnFinished(event -> {
            //pollPane.getChildren().remove(pollOptions);
            displayDone();
        });

        countdownTile = TileBuilder.create()
                                   .skinType(Tile.SkinType.NUMBER)
                                   .prefSize(300,300)
                                   .title("Time Limit")
                                   .value(timeLimit)
                                   .description("Seconds")
                                   .descriptionAlignment(Pos.BASELINE_RIGHT)
                                   .build();
        answerOutputTile = TileBuilder.create()
                                      .skinType(Tile.SkinType.RADIAL_CHART)
                                      .prefSize(300,300)
                                      .title("Responses")
                                      .build();
        if(teacher && !timerStart) {
            Button startTimer = new Button("START");
            startTimer.getStyleClass().setAll("btn", "btn-default");
            startTimer.addEventHandler(MouseEvent.MOUSE_CLICKED, evt->{
                timerStart = true;
                //responseIndicator = new ResponseIndicator();
                //responseIndicator.setNumberOfStudents(20); //TODO: Get this from server
                pollOptions = new HBox();
                pollOptions.getChildren().setAll(answerOutputTile, setUpQuestions(), countdownTile);
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
        questionPane.setVisible(visibility);
    }

    @Override
    public void destroyElement() {

    }

    private VBox setUpQuestions(){
        setUpQuestionList(answers);
        answerButton = new ToggleButton[possibleAnswers.size()];
        chartDataArray = new ChartData[possibleAnswers.size()];
        final ToggleGroup group = new ToggleGroup();
        VBox answerSelection = new VBox();
        //answerSelection.setMaxWidth(slideCanvas.getMaxWidth());
        //answerSelection.setMaxHeight(slideCanvas.getMaxHeight());
        for (int i = 0; i < possibleAnswers.size(); i++) {
            final int number = i;
            chartDataArray[i] = new ChartData(possibleAnswers.get(i),0);
            //chartDataArray[i].setName(possibleAnswers.get(i));
            chartDataArray[i].setColor(assignBarColour(i));
            answerButton[i] = new ToggleButton(possibleAnswers.get(i));
            //answerButton[i].setMinWidth(slideCanvas.getWidth());
            answerButton[i].getStyleClass().setAll("btn","btn-default");
            answerButton[i].addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
                //responseIndicator.incrementResponses();
                System.out.println("Button " + number + " clicked!");
                //checkIfDone();
                chartDataArray[number].setValue(chartDataArray[number].getValue()+1);
            });
            answerSelection.getChildren().add(answerButton[i]);
            answerButton[i].setToggleGroup(group);

        }
        answerOutputTile.setRadialChartData(chartDataArray);
        return answerSelection;
    }

//    private void checkIfDone() {
//        if(responseIndicator.isDone()) {
//            timeline.stop();
//            displayDone();
//        }
//    }

    private void displayDone() {
//        questionPane.getChildren().remove(pollOptions);
//        Label done = new Label("DONE");
//        //pollPane.setCenter(done);
//        questionPane.setBody(done);
        pollOptions.getChildren().remove(remainingTime);
        //responseIndicator.setDone();
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

    private Color assignBarColour(int i){
        i = i%possibleAnswers.size();
        //assignedColour = new Color();
        //for(int j = i; j<possibleAnswers.size(); j++){
            System.out.println("in i = "+ i);
            switch (i){
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

        return  assignedColour;
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

    public void setUpQuestionList(String answers){
        possibleAnswers = new ArrayList<String>();
        possibleAnswers = Arrays.asList(answers.split(","));
    }

}
