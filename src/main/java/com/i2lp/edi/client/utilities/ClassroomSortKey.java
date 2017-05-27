package com.i2lp.edi.client.utilities;

import com.i2lp.edi.client.dashboard.Classroom;

import java.util.List;

/**
 * Created by Kacper on 2017-05-26.
 */
@SuppressWarnings("Duplicates")
public enum ClassroomSortKey {
    NAME_AZ,
    NAME_ZA,
    SUBJECT_AZ,
    SUBJECT_ZA,
    NO_OF_PRES_ASC,
    NO_OF_PRES_DESC;

    @Override
    public String toString() {
        switch(this.name()) {
            case "NAME_AZ":
                return "Name A-Z";
            case "NAME_ZA":
                return "Name Z-A";
            case "SUBJECT_AZ":
                return "Subject A-Z";
            case "SUBJECT_ZA":
                return "Subject Z-A";
            case "NO_OF_PRES_ASC":
                return "No of presentations ascending";
            case "NO_OF_PRES_DESC":
                return "No of presentations descending";
            default:
                return "Unknown";
        }
    }

    public static void copyAllToList(List<ClassroomSortKey> list) {
        list.add(NAME_AZ);
        list.add(NAME_ZA);
        list.add(SUBJECT_AZ);
        list.add(SUBJECT_ZA);
        list.add(NO_OF_PRES_ASC);
        list.add(NO_OF_PRES_DESC);
    }

    public int compare(Classroom classroom1, Classroom classroom2) {
        try {
            switch (this) {
                case NAME_AZ:
                    return classroom1.getModuleName().compareToIgnoreCase(classroom2.getModuleName());
                case NAME_ZA:
                    return -classroom1.getModuleName().compareToIgnoreCase(classroom2.getModuleName());
                case SUBJECT_AZ:
                    return classroom1.getSubject().getSubjectName().compareToIgnoreCase(classroom2.getSubject().getSubjectName());
                case SUBJECT_ZA:
                    return -classroom1.getSubject().getSubjectName().compareToIgnoreCase(classroom2.getSubject().getSubjectName());
                case NO_OF_PRES_ASC:
                    if(classroom1.getPresentations().size() < classroom2.getPresentations().size()) {
                        return -1;
                    } else if(classroom1.getPresentations().size() > classroom2.getPresentations().size()){
                        return 1;
                    } else {
                        return 0;
                    }
                case NO_OF_PRES_DESC:
                    if(classroom1.getPresentations().size() < classroom2.getPresentations().size()) {
                        return 1;
                    } else if(classroom1.getPresentations().size() > classroom2.getPresentations().size()){
                        return -1;
                    } else {
                        return 0;
                    }
                default:
                    return 0;
            }
        } catch (NullPointerException e) {
            return 0;
        }
    }
}
