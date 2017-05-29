package com.i2lp.edi.client.dashboard;

import java.util.ArrayList;

/**
 * Created by Kacper on 2017-05-26.
 */
public class Subject {

    private final String subjectName;
    private ArrayList<DashModule> modules;

    public Subject(String subjectName) {
        this.subjectName = subjectName;
        modules = new ArrayList<>();
    }

    public void addModule(DashModule module) {
        modules.add(module);
    }

    public String getSubjectName() { return subjectName; }

    public int getNumberOfPresentations() {
        int numOfPres = 0;

        for(DashModule module : modules) {
            numOfPres += module.getPresentations().size();
        }

        return numOfPres;
    }

    public ArrayList<DashModule> getModules() { return modules; }

    public static Subject findInArray(String subjectName, ArrayList<Subject> arrayList) {
        for (Subject subject : arrayList) {
            if (subject.getSubjectName().equals(subjectName)) {
                return subject;
            }
        }

        return null;
    }
}
