package com.i2lp.edi.client.presentationElements;

import com.sun.javafx.scene.web.skin.HTMLEditorSkin;
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

    protected HTMLEditor he = new HTMLEditor();
    protected String comment = new String();
    protected boolean submitEnable;

    public CommentPanel(boolean submitEnable) {
        this.submitEnable = submitEnable;

        he.setMaxWidth(Double.MAX_VALUE);

        VBox commentEditor = new VBox();
        commentEditor.getChildren().addAll(he, addControls());

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
        Button saveButton = new Button("Save Locally");
        saveButton.getStyleClass().setAll("btn", "btn-default");
        saveButton.setOnAction(event -> commentSaveFunction());

        Button submitButton = new Button("Submit To Lecturer");
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
        comment = he.getHtmlText();
    }

    protected void commentSubmitFunction(){

        System.out.print("Not yet implemented");
    }

}

