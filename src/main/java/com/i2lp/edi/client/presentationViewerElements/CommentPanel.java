package com.i2lp.edi.client.presentationViewerElements;


import com.i2lp.edi.client.presentationElements.Slide;
import com.i2lp.edi.client.utilities.ResizeDirection;
import com.i2lp.edi.client.utilities.ResizeHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by Luke on 19/04/2017.
 */
public class CommentPanel extends Panel {
    private static final String EMPTY_HTML_TEXT = "<html dir=\"ltr\"><head></head><body contenteditable=\"true\"></body></html>";
    Logger logger = LoggerFactory.getLogger(CommentPanel.class);
    protected HTMLEditor htmlEditor;
    protected String comment;
    protected boolean teacher;
    protected Button saveButton;
    protected Button submitButton;
    protected Slide currentSlide = new Slide();

    public CommentPanel(boolean teacher) {
        this.teacher = teacher;
        htmlEditor = new HTMLEditor();
        htmlEditor.setMaxWidth(Double.MAX_VALUE);
        htmlEditor.setHtmlText(EMPTY_HTML_TEXT);
        VBox commentEditor = new VBox();
        commentEditor.getChildren().addAll(htmlEditor);//, controlPanel());
        addActionListeners();

        getStyleClass().add("panel-primary");
        setPrefHeight(300);
        setMinHeight(100);
        setText("Comments");
        setPadding(new Insets(0,0,0,0));
        setBody(commentEditor);

        ArrayList<ResizeDirection> resizeDirections = new ArrayList<>();
        resizeDirections.add(ResizeDirection.UP);
        addEventFilter(MouseEvent.ANY, new ResizeHandler(this, resizeDirections));
    }

    private void addActionListeners() {
        htmlEditor.addEventHandler(InputEvent.ANY, event -> currentSlide.setUserComments(htmlEditor.getHtmlText()));
    }

    private HBox controlPanel() { //TODO delete if not used for question que tabs or something
//        submitButton = new Button("Submit To Lecturer"); //TODO What will this submit?,
//                                                // todo) BUT maybe a very similar thing can be implemented for question que
//        submitButton.getStyleClass().setAll("btn", "btn-default");

        HBox controlBox = new HBox();
        controlBox.setStyle("-fx-background-color: #34495e;");
        controlBox.setPadding(new Insets(5, 12, 5, 12));
        controlBox.setSpacing(12);
        controlBox.setMaxWidth(Double.MAX_VALUE);

//        if(!teacher) controlBox.getChildren().addAll(submitButton);

        return controlBox;
    }

    public String getComment() {
        return currentSlide.getUserComments();
    }

    public void setSlide(Slide currentSlide) {
        this.currentSlide.setUserComments(htmlEditor.getHtmlText()); //Make sure the previous slide comment is stored
        this.currentSlide = currentSlide; //Set new slide
        updateHtmlEditor();
    }

    private void updateHtmlEditor() {
        if (currentSlide.getUserComments() != null){
            htmlEditor.setHtmlText(currentSlide.getUserComments());
        } else  htmlEditor.setHtmlText(EMPTY_HTML_TEXT);
    }
}

