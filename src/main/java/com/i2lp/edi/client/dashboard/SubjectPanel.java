package com.i2lp.edi.client.dashboard;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.util.ArrayList;

/**
 * Created by Kacper on 2017-05-26.
 */
public class SubjectPanel extends PreviewPanel {

    private static double SPACING = 5;
    private final String subject;
    private HBox subjectPanels;

    public SubjectPanel(String subject, Pane parentPane) {
        super(parentPane, false);
        this.subject = subject;
        subjectPanels = new HBox(SPACING);
        BorderPane.setMargin(subjectPanels, new Insets(5));
        getStyleClass().add("panel-primary");

        Text title = new Text(subject);
        title.getStyleClass().setAll("h4");
        BorderPane.setMargin(title, new Insets(5));

        subjectPanels.getChildren().addListener((ListChangeListener<? super Node>) observable -> {
            if(subjectPanels.getChildren().size() == 0) {
                this.setHidden(true);
            } else {
                this.setHidden(false);
            }
        });

        setTop(title);
        setCenter(subjectPanels);
    }

    public String getSubject() { return subject; }

    public HBox getSubjectPanelsHBox() { return subjectPanels; }

    @Override
    public ArrayList<String> getSearchableTerms() {
        ArrayList<String> searchableTerms = new ArrayList<>();
        searchableTerms.add(getSubject());

        return searchableTerms;
    }
}
