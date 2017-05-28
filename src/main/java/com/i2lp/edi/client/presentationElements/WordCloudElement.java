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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.scene.layout.Panel;

//import java.awt.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.i2lp.edi.client.Constants.PRESENTATIONS_PATH;

/**
 * Created by Koen on 06/04/2017.
 */
public class WordCloudElement extends InteractiveElement {
    protected String question;
    protected Panel wordCloudPanel;
    protected List<String> wordList;
    protected Label remainingTime;
    protected int timeLimit = 30;
    protected Timeline timeline;
    protected Tile countdownTile;
    protected boolean  writingComplete = false;
    protected String cloudShapePath = null;
    protected float xPosition =0.1f;
    protected float yPosition =0.1f;
    protected float xSize =0.8f;
    protected float ySize=0.8f;
    protected float wordCloudHeight=0.25f;
    protected float wordCloudWidth=0.25f;
    protected boolean buttonActive = false;

    protected Button sendWord;
    protected TextField words;

    @Override
    public void sendDataToServer() {

    }

    @Override
    public void receiveDataFromServer() {

    }

    @Override
    public void doClassSpecificRender() {
        if (xSize == 0 || ySize == 0) {
            wordCloudPanel.setPrefHeight(slideHeight);
            wordCloudPanel.setPrefWidth(slideWidth);
        } else {
            wordCloudPanel.setPrefHeight(slideHeight * ySize);
            wordCloudPanel.setPrefWidth(slideWidth * xSize);
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
        wordCloudPanel = new Panel(question);
        wordCloudPanel.getStyleClass().add("panel-primary");
        if(teacher){
            Button start_Task = new Button("Start");
            //wordCloudPanel.getChildren().add(start_Task);
            start_Task.addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
                elementActive = true;
                wordCloudPanel.getChildren().remove(start_Task);
                HBox test = new HBox();
                test.getChildren().addAll(wordCloudElements(),countdownTile);
                wordCloudPanel.setBody(test);
                timeline.play();
            });
            start_Task.addEventHandler(MouseEvent.MOUSE_ENTERED,evt->{
                buttonActive = true;
            });
            start_Task.addEventHandler(MouseEvent.MOUSE_EXITED,evt->{
                buttonActive = false;
            });
            wordCloudPanel.setBody(start_Task);
        }
        remainingTime = new Label("Time Remaining: " + timeLimit);
        final IntegerProperty i = new SimpleIntegerProperty(timeLimit);
        timeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        evt -> {
                            i.set(i.get() - 1);
                            countdownTile.setMaxValue(timeLimit);
                            countdownTile.setValue(i.get());

                        }
                )
        );

        timeline.setCycleCount(timeLimit);
        timeline.setOnFinished(event -> {
            elementActive = false;
            wordCloudPanel.getChildren().removeAll();
            FrequencyAnalyzer fa = new FrequencyAnalyzer();
            List<WordFrequency> wordFrequencies = fa.load(wordList);

            Dimension dimension = new Dimension((int)Math.round(slideWidth*wordCloudWidth),(int)Math.round(slideWidth*wordCloudHeight));
            WordCloud wc = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
            wc.setPadding(2);
            if(cloudShapePath != null){
                try {
                    wc.setBackground(new PixelBoundryBackground(cloudShapePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                wc.setBackground(new CircleBackground(Math.round(xSize/2)));
             }
            wc.setColorPalette(new ColorPalette(Color.ORANGE,Color.GREEN,Color.cyan));
            wc.setFontScalar(new SqrtFontScalar(10,40));
            wc.build(wordFrequencies);

            String pathName = presentationID+Integer.toString(slideID);
            File wordcloudPath = new File(PRESENTATIONS_PATH + "/Wordclouds/"+presentationID+"/"+pathName+".png");
            if (!wordcloudPath.exists()) {
                wordcloudPath.getParentFile().mkdirs(); //Create directory structure if not present yet
            }
            wc.writeToFile(PRESENTATIONS_PATH + "/Wordclouds/"+pathName+".png");
            //TODO @Koen filename can include presentationID and slideID, especially if we want them stored and recalled later. -Herman

            Image wordCloud = new Image("file:" + PRESENTATIONS_PATH + "/Wordclouds/"+pathName+".png",xSize,ySize,true,true);

            ImageView iv = new ImageView(wordCloud);
            wordCloudPanel.setBody(iv);
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

        getCoreNode().addEventFilter(MouseEvent.MOUSE_CLICKED, event -> performOnClickAction());
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

    public VBox wordCloudElements(){
        VBox container = new VBox();
        words = new TextField();
        words.setPromptText("Enter a word!");
        words.setPrefColumnCount(20);

        sendWord = new Button("Send Word");
        sendWord.addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
           wordList.add(words.getText());
           words.clear();
        });
        sendWord.addEventHandler(MouseEvent.MOUSE_ENTERED,evt->{
            buttonActive = true;
        });
        sendWord.addEventHandler(MouseEvent.MOUSE_EXITED,evt->{
            buttonActive = false;
        });
        container.getChildren().addAll(words,sendWord);
        return container;
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
