package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.presentationViewerElements.CommentPanel;
import com.i2lp.edi.client.utilities.CursorState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import static com.i2lp.edi.client.Constants.MAX_QUESTION_LENGTH;


/**
 * Created by kma517 on 16/03/2017.
 */

/**
 * Presentation Manager for students, extending PresentationManager
 * Displays slides with a specific set of buttons.
 */
public class PresentationManagerStudent extends PresentationManager {
    protected Boolean elementClicked = false;
    protected Stage questionQueueStage;
    private boolean isQuestionQueueVisible = false;

    public PresentationManagerStudent(EdiManager ediManager) {
        super(ediManager);
    }

    @Override
    protected void loadSpecificFeatures() {
        questionQueueFunction();
    }


    @Override
    protected void createCommentPanel() {
        commentPanel = new CommentPanel(false);
    }

    @Override
    protected VBox addQuestionQueueControls() {
        if (studentSession != null) {
            ImageView questionBase = makeCustomButton("file:projectResources/icons/Question_Filled.png", event -> {
                loadSpecificFeatures();
                if (!questionQueueActive) {
                    questionQueueActive = true;

                } else {
                    questionQueueActive = false;
                }
            });
            questionBase.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
                if (!mouseDown) {
                    isMouseOverControls = true;
                    setCursorState(CursorState.DEFAULT);
                }
            });
            questionBase.addEventFilter(MouseEvent.MOUSE_EXITED, event -> isMouseOverControls = false);

            VBox contentVBox = new VBox();
            contentVBox.setAlignment(Pos.TOP_CENTER);
            contentVBox.setPadding(new Insets(5));

            contentVBox.getChildren().add(questionBase);

            return contentVBox;
        } else {
            return new VBox();
        }
    }

    protected void questionQueueFunction() {
        if (!isQuestionQueueVisible) {
            isQuestionQueueVisible = true;
            if (questionQueueStage == null) {
                questionQueueStage = new Stage();
            }
            questionQueueStage.setTitle("Send a Question");
            BorderPane border = new BorderPane();
            border.setStyle("-fx-background-color: #34495e");
            Scene questionScene = new Scene(border, 450, 450);
            questionScene.getStylesheets().add("bootstrapfx.css");
            Label title = new Label("Send a question to the question queue");
            border.setPadding(new Insets(0, 10, 10, 10));
            title.setPadding(new Insets(10, 0, 10, 0));
            title.setTextFill(Color.WHITE);
            title.setAlignment(Pos.CENTER);
            title.setMaxWidth(Double.MAX_VALUE);
            border.setTop(title);


            TextArea ta = new TextArea();
            ta.setMaxWidth(Double.MAX_VALUE);
            ta.setMaxHeight(Double.MAX_VALUE);
            ta.setWrapText(true);
            ta.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    sendQuestion(ta);
                }
            });
            border.setCenter(ta);

            //TODO: @Koen Hide this all when in offline mode/presentation not live (check through ediManager.getPresentationManager().getPresentationElement().getPresentationMetadata().isLive())
            Image tick = new Image("file:projectResources/icons/Tick.png", 30, 30, true, true);
            ImageView tickPanel = new ImageView(tick);
            tickPanel.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> sendQuestion(ta));

            Image cross = new Image("file:projectResources/icons/cancel.png", 30, 30, true, true);
            ImageView crossPanel = new ImageView(cross);
            crossPanel.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> doCloseSequence());

            HBox controlBox = new HBox();
            controlBox.setMaxWidth(Double.MAX_VALUE);
            controlBox.getChildren().addAll(tickPanel, crossPanel);
            border.setBottom(controlBox);
            questionQueueStage.setScene(questionScene);
            questionQueueStage.show();
        } else {
            isQuestionQueueVisible = false;
            questionQueueStage.close();
        }
    }

    private void sendQuestion(TextArea ta) {
        if (ta.getText().length() <= MAX_QUESTION_LENGTH) {
            if (!ta.getText().isEmpty()) {
                getStudentSession().addQuestionToQueue(ta.getText());
                questionQueueStage.close();
            }
        } else {
            ta.setText(ta.getText() + " \n Response is too long. Reduce length by " + (MAX_QUESTION_LENGTH -ta.getText().length()) + " characters.");
        }
    }

    @Override
    protected void doCloseSequence() {
        if (questionQueueStage != null) {
            questionQueueStage.close();
        }
        super.doCloseSequence();
    }
}

