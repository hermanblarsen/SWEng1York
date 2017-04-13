package client.editor;

import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

    public PollEditorPanel() {
        this.getStyleClass().add("panel-primary");

        HBox titleBox = new HBox();
        Label title = new Label("Poll");
        titleBox.getChildren().add(title);
        //Button removeButton = new Button("Remove");
        //removeButton.setOnAction(event -> ); //Removing a panel from its parent is apparently trickier than expected
        //titleBox.getChildren().add(removeButton);
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
            //addAnswerButton.getStylesheets().setAll("btn", "btn-primary");
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
}
