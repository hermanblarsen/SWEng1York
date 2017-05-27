package com.i2lp.edi.client.dashboard;

import javax.rmi.CORBA.StubDelegate;
import java.util.ArrayList;

/**
 * Created by Kacper on 2017-05-26.
 */
public class Subject {

    private final String subjectName;
    private ArrayList<Classroom> classrooms;

    public Subject(String subjectName, ArrayList<Subject> availableSubjects) {
        this.subjectName = subjectName;
        classrooms = new ArrayList<>();
        availableSubjects.add(this);
    }

    public void addClassroom(Classroom classroom) {
        classrooms.add(classroom);
    }

    public String getSubjectName() { return subjectName; }

    public int getNumberOfPresentations() {
        int numOfPres = 0;

        for(Classroom classroom : classrooms) {
            numOfPres += classroom.getPresentations().size();
        }

        return numOfPres;
    }

    public ArrayList<Classroom> getClassrooms() { return classrooms; }
}
