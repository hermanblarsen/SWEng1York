package com.i2lp.edi.client.utilities;

import com.i2lp.edi.client.dashboard.Subject;

import java.util.List;

/**
 * Created by Kacper on 2017-05-26.
 */
@SuppressWarnings("Duplicates")
public enum SubjectSortKey {
    NAME_AZ,
    NAME_ZA,
    NO_OF_MODULES_ASC,
    NO_OF_MODULES_DESC,
    NO_OF_PRES_ASC,
    NO_OF_PRES_DESC;

    @Override
    public String toString() {
        switch(this.name()) {
            case "NAME_AZ":
                return "Name A-Z";
            case "NAME_ZA":
                return "Name Z-A";
            case "NO_OF_MODULES_ASC":
                return "No of modules ascending";
            case "NO_OF_MODULES_DESC":
                return "No of modules descending";
            case "NO_OF_PRES_ASC":
                return "No of presentations ascending";
            case "NO_OF_PRES_DESC":
                return "No of presentations descending";
            default:
                return "Unknown";
        }
    }

    public static void copyAllToList(List<SubjectSortKey> list) {
        list.add(NAME_AZ);
        list.add(NAME_ZA);
        list.add(NO_OF_MODULES_ASC);
        list.add(NO_OF_MODULES_DESC);
        list.add(NO_OF_PRES_ASC);
        list.add(NO_OF_PRES_DESC);
    }

    public int compare(Subject subject1, Subject subject2) {
        try {
            switch (this) {
                case NAME_AZ:
                    return subject1.getSubjectName().compareToIgnoreCase(subject2.getSubjectName());
                case NAME_ZA:
                    return -subject1.getSubjectName().compareToIgnoreCase(subject2.getSubjectName());
                case NO_OF_MODULES_ASC:
                    if(subject1.getModules().size() < subject2.getModules().size()) {
                        return -1;
                    } else if(subject1.getModules().size() > subject2.getModules().size()){
                        return 1;
                    } else {
                        return 0;
                    }
                case NO_OF_MODULES_DESC:
                    if(subject1.getModules().size() < subject2.getModules().size()) {
                        return 1;
                    } else if(subject1.getModules().size() > subject2.getModules().size()){
                        return -1;
                    } else {
                        return 0;
                    }
                case NO_OF_PRES_ASC:
                    if(subject1.getNumberOfPresentations() < subject2.getNumberOfPresentations()) {
                        return -1;
                    } else if(subject1.getNumberOfPresentations() > subject2.getNumberOfPresentations()){
                        return 1;
                    } else {
                        return 0;
                    }
                case NO_OF_PRES_DESC:
                    if(subject1.getNumberOfPresentations() < subject2.getNumberOfPresentations()) {
                        return 1;
                    } else if(subject1.getNumberOfPresentations() > subject2.getNumberOfPresentations()){
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
