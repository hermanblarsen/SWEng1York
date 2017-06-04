package com.i2lp.edi.client.editor;

import com.i2lp.edi.client.editor.PollEditorPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * Created by Luke on 29/04/2017.
 */
public class PollEditorTest extends ApplicationTest {

    private static PollEditorPanel myPollEditor;
    private static PollEditorPanel myOtherPollEditor;
    private static VBox container;

    private TextField questionTextField;
    private ComboBox<String> responseTypeChoiceBox;
    private Button addAnswerButton;
    private ArrayList<TextField> answerTextFields;
    private ArrayList<Label> answerLabels;
    private ArrayList<Button> answerRemoveButtons;
    private Button removeButton;
    private Button moveUpButton;
    private Button moveDownButton;

    @Override
    public void start(Stage stage) throws Exception{
        container = new VBox();
        container.setPrefSize(600, 300);
        myPollEditor = new PollEditorPanel(container);
        myOtherPollEditor = new PollEditorPanel(container);
        container.getChildren().addAll(myPollEditor, myOtherPollEditor);

        Scene scene = new Scene(container);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Before
    public void setUp() {
        questionTextField = myPollEditor.questionTextField;
        responseTypeChoiceBox = myPollEditor.responseTypeChoiceBox;
        addAnswerButton = myPollEditor.addAnswerButton;
        answerTextFields = myPollEditor.answerTextFields;
        answerLabels = myPollEditor.answerLabels;
        answerRemoveButtons = myPollEditor.answerRemoveButtons;
        removeButton = myPollEditor.removeButton;
        moveUpButton = myPollEditor.moveUpButton;
        moveDownButton = myPollEditor.moveDownButton;
    }

    @Test
    public void testMoveButtons() {
        clickOn(moveDownButton);
        assertEquals(1, container.getChildren().indexOf(myPollEditor));
        clickOn(moveUpButton);
        assertEquals(0, container.getChildren().indexOf(myPollEditor));
    }

    @Test
    public void testQuestionField() {
        clickOn(questionTextField).write("Question");
        assertEquals("Question", questionTextField.getText());
    }

    @Test
    public void testResponseType() {
        clickOn(responseTypeChoiceBox).clickOn(responseTypeChoiceBox.getItems().get(1));
        assertEquals("Multiple choice", myPollEditor.responseType);
    }

    @Test
    public void testAnswerField() {
        clickOn(responseTypeChoiceBox).clickOn(responseTypeChoiceBox.getItems().get(1));
        clickOn(answerTextFields.get(0)).write("Answer 1");
        clickOn(answerTextFields.get(1)).write("Answer 2");
        assertEquals("Answer 1", answerTextFields.get(0).getText());
        assertEquals("Answer 2", answerTextFields.get(1).getText());
    }

    @Test
    public void testPollCreation() {
        assertEquals("", questionTextField.getText());
        assertEquals("Open", myPollEditor.responseType);
    }

    @After
    public void tearDown() {
        try {
            FxToolkit.hideStage();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }
}
