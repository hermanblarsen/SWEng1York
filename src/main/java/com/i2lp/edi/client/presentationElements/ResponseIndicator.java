package com.i2lp.edi.client.presentationElements;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

/**
 * Created by Luke on 22/04/2017.
 */
public class ResponseIndicator extends HBox {

    private int numberOfStudents;
    private int numberOfResponses;
    private ProgressIndicator progressIndicator;
    private Text numberText;

    public ResponseIndicator() {
        progressIndicator = new ProgressIndicator(0);
        progressIndicator.setPrefSize(60, 60);
        progressIndicator.setStyle("-fx-background-color: rgba(0, 100, 100, 0.5)");

        numberText = new Text();
        HBox textPane = new HBox(numberText);
        textPane.setAlignment(Pos.TOP_CENTER);
        textPane.setPadding(new Insets(15,0,0,0));

        StackPane stack = new StackPane(progressIndicator, textPane);
        stack.setAlignment(Pos.TOP_CENTER);
        setAlignment(Pos.TOP_RIGHT);
        getChildren().add(stack);

        setNumberOfResponses(0);
    }

    public void setNumberOfStudents(int number) {
        numberOfStudents = number;
        updateProgress();
    }

    public void setNumberOfResponses(int number) {
        numberOfResponses = number;
        updateProgress();
    }

    public void incrementResponses() {
        numberOfResponses++;
        updateProgress();
    }

    private void updateProgress() {
        double ratio = 0;

        if(numberOfStudents == 0) {
            progressIndicator.setProgress(-1);
            numberText.setText("");
        }
        else {
            ratio = ((double) numberOfResponses) / ((double) numberOfStudents);
            progressIndicator.setProgress(ratio);
            if(ratio < 1)
                numberText.setText(Integer.toString(numberOfResponses));
            else {
                numberText.setText("");
                ratio = 1;
            }
        }
        String red = Integer.toString(255 - (int)(ratio * 180));
        progressIndicator.setStyle("-fx-progress-color: rgb(" + red + ", 255, 20)");
    }

    public boolean isDone() {
        if(progressIndicator.getProgress() >= 1)
            return true;
        else
            return false;
    }

    public void setDone() {
        setNumberOfResponses(numberOfStudents);
        updateProgress();
    }
}
