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
    private final Subject subject;
    private ArrayList<ModulePanel> modulePanels;
    private HBox modulePanelsHBox;

    public SubjectPanel(Subject subject, Pane parentPane) {
        super(parentPane, false);
        this.subject = subject;
        modulePanelsHBox = new HBox(SPACING);
        modulePanels = new ArrayList<>();
        BorderPane.setMargin(modulePanelsHBox, new Insets(5));
        getStyleClass().add("panel-primary");

        Text title = new Text(subject.getSubjectName());
        title.getStyleClass().setAll("h4");
        BorderPane.setMargin(title, new Insets(5));

        modulePanelsHBox.getChildren().addListener((ListChangeListener<? super Node>) observable -> {
            if(modulePanelsHBox.getChildren().size() == 0) {
                this.setHidden(true);
            } else {
                this.setHidden(false);
            }
        });

        setTop(title);
        setCenter(modulePanelsHBox);
    }

    public Subject getSubject() { return subject; }

    public HBox getModulePanelsHBox() { return modulePanelsHBox; }

    @Override
    public ArrayList<String> getSearchableTerms() {
        ArrayList<String> searchableTerms = new ArrayList<>();
        searchableTerms.add(getSubject().getSubjectName());

        return searchableTerms;
    }

    public static SubjectPanel findInArray(String subjectName, ArrayList<SubjectPanel> arrayList) {
        for (SubjectPanel panel : arrayList) {
            if (panel.getSubject().getSubjectName().equals(subjectName)) {
                return panel;
            }
        }

        return null;
    }

    public void addModulePanel(ModulePanel panel) {
        modulePanels.add(panel);
    }

    public ArrayList<ModulePanel> getModulePanels() { return modulePanels; }
}
