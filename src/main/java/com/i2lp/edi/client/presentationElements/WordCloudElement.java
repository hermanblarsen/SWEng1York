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

import static com.i2lp.edi.client.Constants.TEMP_DIR_PATH;

/**
 * Created by Koen on 06/04/2017.
 */
public class WordCloudElement extends InteractiveElement {
    protected String task;
    protected Panel wordCloudPanel;
    protected List<String> wordList;
    protected Label remainingTime;
    protected int timeLimit;
    protected Timeline timeline;
    protected Tile countdownTile;
    protected boolean  writingComplete = false;
    protected String cloudShapePath = null;

    @Override
    public void sendDataToServer() {

    }

    @Override
    public void receiveDataFromServer() {

    }

    @Override
    public void doClassSpecificRender() {

    }

    @Override
    public Node getCoreNode() {
        return wordCloudPanel;
    }

    @Override
    public void setupElement() {
        wordList = new ArrayList<String>();
        wordCloudPanel = new Panel(task);
        wordCloudPanel.getStyleClass().add("panel-primary");
        if(teacher){
            Button start_Task = new Button("Start");
            //wordCloudPanel.getChildren().add(start_Task);
            start_Task.addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
                wordCloudPanel.getChildren().remove(start_Task);
                HBox test = new HBox();
                test.getChildren().addAll(wordCloudElements(),countdownTile);
                wordCloudPanel.setBody(test);
                timeline.play();
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
            wordCloudPanel.getChildren().removeAll();
            FrequencyAnalyzer fa = new FrequencyAnalyzer();
            List<WordFrequency> wordFrequencies = fa.load(wordList);

            Dimension dimention = new Dimension(600,600); //TODO this needs to be set from the XML - Herman
            WordCloud wc = new WordCloud(dimention, CollisionMode.PIXEL_PERFECT);
            wc.setPadding(2);
            if(cloudShapePath != null){
                try {
                    wc.setBackground(new PixelBoundryBackground(cloudShapePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                wc.setBackground(new CircleBackground(300));
             }
            wc.setColorPalette(new ColorPalette(Color.ORANGE,Color.GREEN,Color.cyan));
            wc.setFontScalar(new SqrtFontScalar(10,40));
            wc.build(wordFrequencies);
//            wc.writeToFile(TEMP_DIR_PATH + "Wordclouds/wordcloud" + + ".png");


            File wordcloudPath = new File(TEMP_DIR_PATH + "/Wordclouds/wordcloud.png");
            if (!wordcloudPath.exists()) {
                wordcloudPath.getParentFile().mkdirs(); //Create directory structure if not present yet
            }
            wc.writeToFile(TEMP_DIR_PATH + "Wordclouds/wordcloud.png");
            //TODO filename can include presentationID and slideID, especially if we want them stored and recalled later.

            Image wordCloud = new Image("file:" + TEMP_DIR_PATH + "Wordclouds/wordcloud.png",600,600,true,true);

            ImageView iv = new ImageView(wordCloud);
            wordCloudPanel.setBody(iv);
        });

        countdownTile = TileBuilder.create()
                .skinType(Tile.SkinType.NUMBER)
                .prefSize(300,300)
                .title("Time Limit")
                .value(timeLimit)
                .description("Seconds")
                .descriptionAlignment(Pos.BASELINE_RIGHT)
                .build();

    }

    @Override
    public void destroyElement() {

    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public VBox wordCloudElements(){
        VBox container = new VBox();
        TextField words = new TextField();
        words.setPromptText("Enter a word!");
        words.setPrefColumnCount(20);

        Button sendWord = new Button("Send Word");
        sendWord.addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
           wordList.add(words.getText());
           words.clear();
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

}
