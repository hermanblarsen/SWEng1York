package com.i2lp.edi.client.utilities;

import com.i2lp.edi.client.presentationElements.Presentation;

import java.util.List;

/**
 * Created by Kacper on 2017-05-24.
 */
public enum PresSortKey {
    NAME_AZ,
    NAME_ZA,
    SUBJECT_AZ,
    SUBJECT_ZA;

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
            default:
                return "Unknown";
        }
    }

    public static void copyAllToList(List<PresSortKey> list) {
        list.add(NAME_AZ);
        list.add(NAME_ZA);
        list.add(SUBJECT_AZ);
        list.add(SUBJECT_ZA);
    }

    public int compare(Presentation pres1, Presentation pres2) {
        try {
            switch (this) {
                case NAME_AZ:
                    return pres1.getDocumentID().compareToIgnoreCase(pres2.getDocumentID());
                case NAME_ZA:
                    return -pres1.getDocumentID().compareToIgnoreCase(pres2.getDocumentID());
                case SUBJECT_AZ:
                    return pres1.getSubject().compareToIgnoreCase(pres2.getSubject());
                case SUBJECT_ZA:
                    return -pres1.getSubject().compareToIgnoreCase(pres2.getSubject());
                default:
                    return 0;
            }
        } catch (NullPointerException e) {
            return 0;
        }
    }
}
