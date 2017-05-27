package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.presentationElements.Presentation;

import java.util.ArrayList;

/**
 * Created by Kacper on 2017-05-25.
 */
public class Module {

    private final String moduleName;
    private ArrayList<Presentation> presentations;
    private Subject subject;

    public Module(Subject subject, String moduleName, ArrayList<Module> availableModules) {
        this.moduleName = moduleName;
        this.subject = subject;
        presentations = new ArrayList<>();
        availableModules.add(this);
    }

    public String getModuleName() { return moduleName; }

    public ArrayList<Presentation> getPresentations() { return presentations; }

    public void addPresentation(Presentation presentation) {
        presentations.add(presentation);
        presentation.setModule(this);
    }

    public Subject getSubject() { return subject; }
}
