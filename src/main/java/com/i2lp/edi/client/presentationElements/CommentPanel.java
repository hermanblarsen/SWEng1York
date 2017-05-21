package com.i2lp.edi.client.presentationElements;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import org.kordamp.bootstrapfx.scene.layout.Panel;

/**
 * Created by Luke on 19/04/2017.
 */
public class CommentPanel extends Panel {

    protected HTMLEditor htmlEditor = new HTMLEditor();
    protected String comment;
    protected boolean submitEnable;
    protected Button saveButton;
    protected Button submitButton;

    public CommentPanel(boolean submitEnable) {
        this.submitEnable = submitEnable;

        htmlEditor.setMaxWidth(Double.MAX_VALUE);
        VBox commentEditor = new VBox();
        commentEditor.getChildren().addAll(htmlEditor, addControls());

        getStyleClass().add("panel-primary");
        setMaxHeight(300);
        setText("Comments");
        setPadding(new Insets(0,0,0,0));
        setBody(commentEditor);
    }

    public String getComment() {
        return comment;
    }

    private HBox addControls() {
        saveButton = new Button("Save Locally");
        saveButton.getStyleClass().setAll("btn", "btn-default");
        saveButton.setOnAction(event -> commentSaveFunction());

        submitButton = new Button("Submit To Lecturer");
        submitButton.getStyleClass().setAll("btn", "btn-default");
        submitButton.setOnAction(event -> commentSubmitFunction());

        HBox controlBox = new HBox();
        controlBox.setStyle("-fx-background-color: #34495e;");
        controlBox.setPadding(new Insets(5, 12, 5, 12));
        controlBox.setSpacing(12);
        controlBox.setMaxWidth(Double.MAX_VALUE);

        if(submitEnable)
            controlBox.getChildren().addAll(saveButton, submitButton);
        else
            controlBox.getChildren().add(saveButton);

        return controlBox;
    }

    protected void commentSaveFunction(){
        comment = htmlEditor.getHtmlText();
    }

    protected void commentSubmitFunction(){

        System.out.print("Not yet implemented");
    }

}

