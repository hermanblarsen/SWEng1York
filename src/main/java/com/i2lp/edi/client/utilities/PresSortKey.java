package com.i2lp.edi.client.utilities;

import com.i2lp.edi.client.presentationElements.Presentation;

import java.util.List;

/**
 * Created by Kacper on 2017-05-24.
 */
@SuppressWarnings("Duplicates")
public enum PresSortKey {
    TITLE_AZ,
    TITLE_ZA,
    SUBJECT_AZ,
    SUBJECT_ZA,
    TIME_SCHEDULED_ASC,
    TIME_SCHEDULED_DESC,
    AUTHOR_AZ,
    AUTHOR_ZA;

    @Override
    public String toString() {
        switch(this.name()) {
            case "TITLE_AZ":
                return "Title A-Z";
            case "TITLE_ZA":
                return "Title Z-A";
            case "SUBJECT_AZ":
                return "Subject A-Z";
            case "SUBJECT_ZA":
                return "Subject Z-A";
            case "TIME_SCHEDULED_ASC":
                return "Time scheduled ascending";
            case "TIME_SCHEDULED_DESC":
                return "Time scheduled descending";
            case "AUTHOR_AZ":
                return "Author A-Z";
            case "AUTHOR_ZA":
                return "Author Z-A";
            default:
                return "Unknown";
        }
    }

    public static void copyAllToList(List<PresSortKey> list) {
        list.add(TITLE_AZ);
        list.add(TITLE_ZA);
        list.add(SUBJECT_AZ);
        list.add(SUBJECT_ZA);
        list.add(TIME_SCHEDULED_ASC);
        list.add(TIME_SCHEDULED_DESC);
        list.add(AUTHOR_AZ);
        list.add(AUTHOR_ZA);
    }

    public int compare(Presentation pres1, Presentation pres2) {
        try {
            switch (this) {
                case TITLE_AZ:
                    return pres1.getDocumentID().compareToIgnoreCase(pres2.getDocumentID());
                case TITLE_ZA:
                    return -pres1.getDocumentID().compareToIgnoreCase(pres2.getDocumentID());
                case SUBJECT_AZ:
                    return pres1.getSubject().getSubjectName().compareToIgnoreCase(pres2.getSubject().getSubjectName());
                case SUBJECT_ZA:
                    return -pres1.getSubject().getSubjectName().compareToIgnoreCase(pres2.getSubject().getSubjectName());
                case TIME_SCHEDULED_ASC:
                    if (pres1.getGoLiveDateTime() != null && pres2.getGoLiveDateTime() != null) {
                        return pres1.getGoLiveDateTime().compareTo(pres2.getGoLiveDateTime());
                    } else if (pres1.getGoLiveDateTime() == null) {
                        return -1;
                    } else {
                        return 1;
                    }
                case TIME_SCHEDULED_DESC:
                    return 0;
                case AUTHOR_AZ:
                    return pres1.getAuthor().compareToIgnoreCase(pres2.getAuthor());
                case AUTHOR_ZA:
                    return -pres1.getAuthor().compareToIgnoreCase(pres2.getAuthor());
                default:
                    return 0;
            }
        } catch (NullPointerException e) {
            return 0;
        }
    }
}
