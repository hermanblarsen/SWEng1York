package com.i2lp.edi.client.dashboard;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * Created by Kacper on 2017-05-25.
 */
public class ClassroomPanel extends PreviewPanel {

    private final Classroom classroom;

    public ClassroomPanel(Pane parentPane, Classroom classroom) {
        super(parentPane);
        this.classroom = classroom;

        setText(classroom.getModuleName());
        setFooter(new Label(classroom.getPresentations().size() + " presentations"));
    }

    public String getModuleName() { return classroom.getModuleName(); }

    public Classroom getClassroom() { return classroom; }
}
