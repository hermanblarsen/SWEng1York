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
    private String subject;

    public Classroom(String moduleName, Presentation presentation) {
        this.moduleName = moduleName;
        Random random = new Random();
        subject = "Subject: " + random.nextInt(2);
        presentations = new ArrayList<>();
        addPresentation(presentation);
    }

    public String getModuleName() { return moduleName; }

    public ArrayList<Presentation> getPresentations() { return presentations; }

    public void addPresentation(Presentation presentation) {
        presentations.add(presentation);
        presentation.setClassroom(this);
    }

    public String getSubject() { return subject; }
}
