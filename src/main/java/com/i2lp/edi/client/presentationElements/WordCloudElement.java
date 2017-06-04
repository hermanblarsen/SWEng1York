package com.i2lp.edi.client.presentationElements;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.bg.PixelBoundryBackground;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.ColorPalette;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.i2lp.edi.client.Constants.PRESENTATIONS_PATH;

//import java.awt.*;

/**
 * Created by Koen on 06/04/2017.
 */
public class WordCloudElement extends InteractiveElement {
    protected Time startTime;
    protected String question;
    protected Panel wordCloudPanel;
    protected List<String> wordList;
    protected Label remainingTime;
    protected int timeLimit = 30;
    protected Timeline timeline;
    protected Tile countdownTile;
    protected boolean  writingComplete = false;
    protected String cloudShapePath = null;
    protected float xPosition =0f;
    protected float yPosition =0f;
    protected float xSize =1f;
    protected float ySize=1f;
    protected boolean buttonActive = false;
    private ContextMenu cm;
    private ImageView iv;
    private ImageView startWordCloud;
    protected Button sendWord;
    protected TextField words;

    @Override
    public void doClassSpecificRender() {
        if (xSize == 0 || ySize == 0) {
            wordCloudPanel.setPrefHeight(slideHeight);
            wordCloudPanel.setPrefWidth(slideWidth);
        } else {
            wordCloudPanel.setPrefHeight(slideHeight * ySize);
            wordCloudPanel.setPrefWidth(slideWidth * xSize);
        }

        if(iv != null){
            if (xSize == 0 || ySize == 0) {
                iv.setFitHeight(slideHeight);
                iv.setFitWidth(slideWidth);
            } else {
                iv.setFitHeight(slideHeight * ySize);
                iv.setFitWidth(slideWidth * xSize);
            }
        }

        if(startWordCloud != null){
            if (xSize == 0 || ySize == 0) {
                startWordCloud.setFitHeight(slideHeight);
                startWordCloud.setFitWidth(slideWidth);
            } else {
                startWordCloud.setFitHeight(slideHeight * ySize);
                startWordCloud.setFitWidth(slideWidth * xSize);
            }
        }
        wordCloudPanel.setTranslateX(slideWidth * xPosition);
        wordCloudPanel.setTranslateY(slideHeight * yPosition);

        getCoreNode().addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
            if(!buttonActive){
                performOnClickAction();
            }
        });
    }

    @Override
    public Node getCoreNode() {
        return wordCloudPanel;
    }

    @Override
    public void setupElement() {
        wordList = new ArrayList<String>();
        wordCloudPanel = new Panel();
        //wordCloudPanel.getStyleClass().add("panel-primary");
        wordCloudPanel.setStyle("-fx-background-color: #2a2a2a");
        if(teacher){
//            Button start_Task = new Button("Start");
            Image startWordCloudTask = new Image("file:projectResources/icons/startWC.png");
            startWordCloud = new ImageView(startWordCloudTask);
            HBox startBox = new HBox();
            startBox.setAlignment(Pos.CENTER);
            startBox.getChildren().add(startWordCloud);
//            start_Task.getStyleClass().setAll("btn","btn-default");
//            start_Task.setAlignment(Pos.TOP_CENTER);
//            //wordCloudPanel.getChildren().add(start_Task);
            startWordCloud.addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
                wordCloudPanel.getChildren().remove(startBox);
                setUpWordCloudData(new Time(Instant.now().toEpochMilli()));
                if(ediManager.getPresentationManager().getTeacherSession() != null) {
                    ediManager.getPresentationManager().getTeacherSession().beginInteraction(this, true);
                }
            });
            startWordCloud.addEventHandler(MouseEvent.MOUSE_ENTERED,evt->{
                buttonActive = true;
            });
            startWordCloud.addEventHandler(MouseEvent.MOUSE_EXITED,evt->{
                buttonActive = false;
            });
            wordCloudPanel.setBody(startBox);
        }


    }

    public void setUpWordCloudData(Time startTime){
        this.startTime = startTime;

        //Start Time
        //We count out from current system time
        //And display current System time - startTime
        //Time currentTime = new Time(Instant.now().toEpochMilli());
        LocalTime currentTime = LocalTime.now();
        LocalTime startTimeNew = startTime.toLocalTime();
        int timeDifference = currentTime.getSecond()-startTimeNew.getSecond();
        //Time timeDifference = new Time(currentTime.toInstant().getEpochSecond()-startTime.toInstant().getEpochSecond());
        int newTimeLimit = Math.round(timeLimit-timeDifference);
        remainingTime = new Label("Time Remaining: " + (newTimeLimit));
        final IntegerProperty i = new SimpleIntegerProperty(newTimeLimit);
        timeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        evt -> {
                            i.set(i.get() - 1);
                            countdownTile.setMaxValue(newTimeLimit);
                            countdownTile.setValue(i.get());

                        }
                )
        );

        timeline.setCycleCount(newTimeLimit);
        timeline.setOnFinished(event -> {
            ediManager.getPresentationManager().setWordCloudActive(false);
            elementActive = false;
            wordCloudPanel.getChildren().removeAll();
        });
        countdownTile = TileBuilder.create()
                .skinType(Tile.SkinType.NUMBER)
                .prefSize((xSize*slideWidth)/3,(ySize*slideHeight)/3)
                .title("Time Limit")
                .value(timeLimit)
                .description("Seconds")
                .descriptionAlignment(Pos.BASELINE_RIGHT)
                .build();
        wordCloudPanel.setVisible(visibility);
        //getCoreNode().addEventFilter(MouseEvent.MOUSE_CLICKED, event -> performOnClickAction());

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(countdownTile.getValue() == 0){
                    countdownTile.setValue(timeLimit);
                }
                Label wordCloudQuestion = new Label(question);
                wordCloudQuestion.setFont(new javafx.scene.text.Font("Helvetica", 50));
                wordCloudQuestion.setTextFill(javafx.scene.paint.Color.WHITE);
                wordCloudQuestion.setWrapText(true);
                elementActive = true;
                VBox wordCloudBox = new VBox();
                HBox dataBox = wordCloudElements();
                dataBox.setAlignment(Pos.CENTER);
                wordCloudBox.setAlignment(Pos.CENTER);
                wordCloudBox.getChildren().addAll(wordCloudQuestion,countdownTile,dataBox);
                wordCloudPanel.setBody(wordCloudBox);
                timeline.play();
            }
        });

    }

    @Override
    public void destroyElement() {

    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public HBox wordCloudElements(){
        ediManager.getPresentationManager().setWordCloudActive(true);
        HBox container = new HBox();
        words = new TextField();
        words.setPromptText("Enter a word!");
        words.setPrefColumnCount(20);
        words.setAlignment(Pos.TOP_CENTER);
        sendWord = new Button("Send Word");
        sendWord.setAlignment(Pos.CENTER);
        sendWord.addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
            if((ediManager.getPresentationManager().getTeacherSession()) != null){
                ediManager.getPresentationManager().getTeacherSession().sendResponse(this, words.getText().toLowerCase());
            } else if((ediManager.getPresentationManager().getStudentSession() != null)){
                ediManager.getPresentationManager().getStudentSession().sendResponse(this, words.getText().toLowerCase());
            }
           words.clear();
        });
        sendWord.addEventHandler(MouseEvent.MOUSE_ENTERED,evt->{
            buttonActive = true;
        });
        sendWord.addEventHandler(MouseEvent.MOUSE_EXITED,evt->{
            buttonActive = false;
        });
        words.addEventHandler(KeyEvent.KEY_PRESSED,keyEvent->{
            if(keyEvent.getCode().equals(KeyCode.ENTER)){
                if((ediManager.getPresentationManager().getTeacherSession()) != null){
                    ediManager.getPresentationManager().getTeacherSession().sendResponse(this, words.getText());
                } else if((ediManager.getPresentationManager().getStudentSession() != null)){
                    ediManager.getPresentationManager().getStudentSession().sendResponse(this, words.getText());
                }
                words.clear();
            }
        });
        container.getChildren().addAll(words,sendWord);
        return container;
    }


    public void setWordList(List<String> wordList) {
        this.wordList = wordList;
    }

    public void generateWordCloud(){
        FrequencyAnalyzer fa = new FrequencyAnalyzer();
        List<WordFrequency> wordFrequencies = fa.load(wordList);

        Dimension dimension = new Dimension((int)(xSize*slideWidth),(int)(ySize*slideHeight));
        WordCloud wc = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
        wc.setPadding(2);
        if(cloudShapePath != null){
            try {
                wc.setBackground(new PixelBoundryBackground(cloudShapePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            float rad = (ySize*(float)slideHeight)/2;
            wc.setBackground(new CircleBackground(Math.round(rad)));
            wc.setBackgroundColor(new Color(26,188,156));
        }
        wc.setColorPalette(new ColorPalette(Color.ORANGE, Color.GREEN,Color.cyan));
        wc.setFontScalar(new SqrtFontScalar(10,40));
        wc.build(wordFrequencies);

        String pathName = presentationID+"_"+Integer.toString(slideID)+"_"+Integer.toString(elementID);
        File wordcloudPath = new File(PRESENTATIONS_PATH+"/" +presentationID+"/Wordclouds/"+pathName+".png");
        if (!wordcloudPath.exists()) {
            wordcloudPath.getParentFile().mkdirs(); //Create directory structure if not present yet
        }
        wc.writeToFile(PRESENTATIONS_PATH+"/" + presentationID+"/Wordclouds/"+pathName+".png");

        Image wordCloud = new Image("file:" + PRESENTATIONS_PATH +"/"+ presentationID+"/Wordclouds/"+pathName+".png",xSize*slideWidth,ySize*slideHeight,true,true);

        iv = new ImageView(wordCloud);

        VBox wordCloudBox  = new VBox();
        wordCloudBox.getChildren().addAll(iv);
        wordCloudPanel.setBody(iv);

        if(ediManager.getPresentationManager().getTeacherSession() != null){
            wordCloudPanel.addEventFilter(MouseEvent.MOUSE_CLICKED, event->{
                //wordCloudPanel.addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
                    if(cm != null){
                        cm.hide();
                    }
                    cm = new ContextMenu();

                    if(event.getButton() == MouseButton.SECONDARY){
                        MenuItem reset = new MenuItem("Reset Element");
                        reset.setOnAction(event1->{
                            ediManager.getPresentationManager().getTeacherSession().resetInteractiveElement(this);
                            slideCanvas.getChildren().remove(this.getCoreNode());
                            setupElement();
                            doClassSpecificRender();
                            slideCanvas.getChildren().add(wordCloudPanel);
                        });
                        cm.getItems().add(reset);
                        cm.show(this.getCoreNode(),event.getScreenX(),event.getScreenY());


                    }
                //});
                event.consume();
            });


        }

    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getCloudShapePath() {
        return cloudShapePath;
    }

    public void setCloudShapePath(String cloudShapePath) {
        this.cloudShapePath = cloudShapePath;
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


}
