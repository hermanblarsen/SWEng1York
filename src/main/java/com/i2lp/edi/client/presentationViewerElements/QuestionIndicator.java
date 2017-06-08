package com.i2lp.edi.client.presentationViewerElements;

import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Created by Luke on 07/05/2017.
 */

/**
 * Question indicator indicating in presentation how many
 * questions are unanswered
 */
public class QuestionIndicator extends HBox{
    private int numberOfQuestions;
    protected GraphicsContext gc;
    protected Text numberText;
    private static double sensitivity = 10;
    private static int size = 60;

    public QuestionIndicator() {
        Canvas canvas = new Canvas(size, size);
        gc = canvas.getGraphicsContext2D();

        numberText = new Text();
        numberText.setFill(new Color(0,0,0,1));
        HBox textPane = new HBox(numberText);
        textPane.setAlignment(Pos.CENTER);
        //textPane.setPadding(new Insets(0,0,0,0));

        StackPane stack = new StackPane(canvas, textPane);
        stack.setAlignment(Pos.TOP_CENTER);
        setAlignment(Pos.TOP_RIGHT);
        getChildren().add(stack);

        setNumberOfQuestions(0);
    }

    public void setNumberOfQuestions(int number) {
        numberOfQuestions = number;
        update();
    }

    public void incrementQuestions() {
        numberOfQuestions++;
        update();
    }

    private void update() {
        Double green;
        Double red;

        if (numberOfQuestions <= 0)
        {
            red = (double) 1;
            green = (double) 1;
        }
        else if(numberOfQuestions <= sensitivity) {
            red = (double) 1;
            green = 1 - (numberOfQuestions  / sensitivity);
        }
        else
        {
            red = (double) 1;
            green = (double) 0;
        }

        gc.clearRect(size,size,size,size);
        gc.setFill(new Color(0.9, 0.9, 0.9,0.5));
        gc.setStroke(new Color(0,0,0,0.5));
        gc.fillOval(size/12,size/12,((5*size)/6),((5*size)/6));
        gc.setFill(new Color(red, green, 0.18,0.5));
        gc.fillOval(size/6, size/6, ((2*size)/3), ((2*size)/3));
        gc.strokeOval(size/12,size/12,((5*size)/6),((5*size)/6));

        if(numberOfQuestions >= 0)
            numberText.setText(Integer.toString(numberOfQuestions));
        else
            numberText.setText("");
    }
}
