package com.i2lp.edi.client.editor;

import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.util.ArrayList;

/**
 * Created by Kacper on 2017-04-13.
 */
public class PollEditorPanel extends Panel {
    private GridPane body;
    private TextField questionTextField;
    private String responseType; //Should this be an enum? Could be a boolean if only two options: open question (text) or multiple-choice question (choose a, b, c)
    private ComboBox<String> responseTypeChoiceBox;
    private Label answersLabel;
    private Button addAnswerButton;
    private ArrayList<TextField> answerTextFields;
    private ArrayList<Label> answerLabels;
    private ArrayList<Button> answerRemoveButtons;
    private int answerNumber;
    private VBox parent;

    public PollEditorPanel(VBox parent) {
        this.parent = parent;
        this.getStyleClass().add("panel-primary");

        HBox titleBox = new HBox();
        Label title = new Label("Poll");
        titleBox.getChildren().add(title);
        Region separatorRegion = new Region();
        titleBox.getChildren().add(separatorRegion);
        titleBox.setHgrow(separatorRegion, Priority.ALWAYS);
        Button moveUpButton = new Button("Move up");
        moveUpButton.getStyleClass().setAll("btn");
        moveUpButton.getStyleClass().add("btn-primary");
        moveUpButton.setOnAction(event -> moveUp());
        titleBox.getChildren().add(moveUpButton);
        Button moveDownButton = new Button("Move down");
        moveDownButton.setOnAction(event -> moveDown());
        moveDownButton.getStyleClass().setAll("btn");
        moveDownButton.getStyleClass().add("btn-primary");
        titleBox.getChildren().add(moveDownButton);
        Button removeButton = new Button("Remove");
        removeButton.setOnAction(event -> parent.getChildren().remove(this));
        removeButton.getStyleClass().setAll("btn");
        removeButton.getStyleClass().add("btn-danger");
        titleBox.getChildren().add(removeButton);
        this.setHeading(titleBox);

        answerTextFields = new ArrayList<>();
        answerLabels = new ArrayList<>();
        answerRemoveButtons = new ArrayList<>();

        body = new GridPane();
        body.setHgap(10);
        body.setVgap(10);
        this.setBody(body);

        body.add(new Label("Question"), 0, 0);
        questionTextField = new TextField("Type your question here");
        body.add(questionTextField, 0, 1);

        body.add(new Label("Response Type"), 1, 0);
        responseTypeChoiceBox = new ComboBox<>();
        responseTypeChoiceBox.getItems().add("Open");
        responseTypeChoiceBox.getItems().add("Multiple choice");
        responseTypeChoiceBox.setValue("Open");
        responseTypeChoiceBox.setOnAction(event -> setResponseType(responseTypeChoiceBox.getValue()));
        body.add(responseTypeChoiceBox, 1, 1);
    }

    private void setResponseType(String responseType) {
        this.responseType = responseType;

        if(responseType.equals("Multiple choice")) {
            answersLabel = new Label("Answers");
            body.add(answersLabel, 2, 0, 2, 1);
            body.setHalignment(answersLabel, HPos.CENTER);
            addAnswerButton = new Button("Add answer");
            addAnswerButton.getStyleClass().setAll("btn");
            addAnswerButton.getStyleClass().add("btn-success");
            addAnswerButton.setOnAction(event -> addAnswer());
            body.add(addAnswerButton, 4, 0);

            answerNumber = 1;
            addAnswer();
            addAnswer();
        } else if(responseType.equals("Open")) {
            for(Label answerLabel : answerLabels)
                body.getChildren().remove(answerLabel);


            for(TextField answerTextField : answerTextFields)
                body.getChildren().remove(answerTextField);


            for(Button answerRemoveButton : answerRemoveButtons)
                body.getChildren().remove(answerRemoveButton);

            answerTextFields.clear();
            answerLabels.clear();
            answerRemoveButtons.clear();

            try {
                body.getChildren().remove(answersLabel);
            } catch(Exception e) {
                //Handle exception?
            }
            try {
                body.getChildren().remove(addAnswerButton);
            } catch(Exception e) {
                //Handle exception?
            }
        }
    }

    private void addAnswer() {
        Label answerLabel = new Label("Answer " + answerNumber);
        answerLabels.add(answerLabel);

        TextField answerTextField = new TextField("Answer " + answerNumber);
        answerTextFields.add(answerTextField);

        Button answerRemoveButton = new Button("Remove");
        //answerRemoveButton.getStylesheets().setAll("btn", "btn-danger");
        answerRemoveButton.setOnAction(event -> removeAnswer(answerTextField, answerLabel, answerRemoveButton));
        answerRemoveButton.getStyleClass().setAll("btn");
        answerRemoveButton.getStyleClass().add("btn-danger");
        answerRemoveButtons.add(answerRemoveButton);

        answerNumber++;
        refreshDisplayedAnswers();
    }

    private void removeAnswer(TextField answerTextField, Label answerLabel, Button answerRemoveButton) {
        answerTextFields.remove(answerTextField);
        body.getChildren().remove(answerTextField);

        answerLabels.remove(answerLabel);
        body.getChildren().remove(answerLabel);

        answerRemoveButtons.remove(answerRemoveButton);
        body.getChildren().remove(answerRemoveButton);

        refreshDisplayedAnswers();
    }

    private void refreshDisplayedAnswers() {
        for(Label answerLabel : answerLabels) {
            body.getChildren().remove(answerLabel);
            body.add(answerLabel, 2, answerLabels.indexOf(answerLabel) + 1);
        }

        for(TextField answerTextField : answerTextFields) {
            body.getChildren().remove(answerTextField);
            body.add(answerTextField, 3, answerTextFields.indexOf(answerTextField) + 1);
        }

        for(Button answerRemoveButton : answerRemoveButtons) {
            body.getChildren().remove(answerRemoveButton);
            if(answerTextFields.size() > 2) //Don't allow fewer than 2 answers
                body.add(answerRemoveButton, 4, answerRemoveButtons.indexOf(answerRemoveButton) + 1);
        }
    }

    private void moveUp() {
        if(parent.getChildren().indexOf(this) != 0) {
            Node nodeAbove = parent.getChildren().get(parent.getChildren().indexOf(this) - 1);
            swap(nodeAbove, this);
        }
    }

    private void moveDown() {
        if(parent.getChildren().indexOf(this) != parent.getChildren().size() - 1) {
            Node nodeBelow = parent.getChildren().get(parent.getChildren().indexOf(this) + 1);
            swap(nodeBelow, this);
        }
    }

    private void swap(Node node1, Node node2) {
        ArrayList<Node> childrenList = new ArrayList<>();

        for(Node node : parent.getChildren())
            childrenList.add(node);

        parent.getChildren().clear();

        for(Node node : childrenList) {
            if(node == node1)
                parent.getChildren().add(node2);
            else if(node == node2)
                parent.getChildren().add(node1);
            else
                parent.getChildren().add(node);
        }
    }
}
