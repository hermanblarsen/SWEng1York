package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.presentationElements.Presentation;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Kacper on 2017-05-25.
 */
public class Classroom {

    private final String moduleName;
    private ArrayList<Presentation> presentations;
    private Subject subject;

    public Classroom(Subject subject, String moduleName, ArrayList<Classroom> availableClassrooms) {
        this.moduleName = moduleName;
        this.subject = subject;
        presentations = new ArrayList<>();
        availableClassrooms.add(this);
    }

    public String getModuleName() { return moduleName; }

    public ArrayList<Presentation> getPresentations() { return presentations; }

    public void addPresentation(Presentation presentation) {
        presentations.add(presentation);
        presentation.setClassroom(this);
    }

    public Subject getSubject() { return subject; }
}
