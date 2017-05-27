package com.i2lp.edi.client.dashboard;

import java.util.ArrayList;

/**
 * Created by Kacper on 2017-05-26.
 */
public class Subject {

    private final String subjectName;
    private ArrayList<Module> modules;

    public Subject(String subjectName, ArrayList<Subject> availableSubjects) {
        this.subjectName = subjectName;
        modules = new ArrayList<>();
        availableSubjects.add(this);
    }

    public void addModule(Module module) {
        modules.add(module);
    }

    public String getSubjectName() { return subjectName; }

    public int getNumberOfPresentations() {
        int numOfPres = 0;

        for(Module module : modules) {
            numOfPres += module.getPresentations().size();
        }

        return numOfPres;
    }

    public ArrayList<Module> getModules() { return modules; }
}
