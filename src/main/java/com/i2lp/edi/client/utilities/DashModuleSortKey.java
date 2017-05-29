package com.i2lp.edi.client.utilities;

import com.i2lp.edi.client.dashboard.DashModule;

import java.util.List;

/**
 * Created by Kacper on 2017-05-26.
 */
@SuppressWarnings("Duplicates")
public enum DashModuleSortKey {
    NAME_AZ,
    NAME_ZA,
    NO_OF_PRES_ASC,
    NO_OF_PRES_DESC;

    @Override
    public String toString() {
        switch(this.name()) {
            case "NAME_AZ":
                return "Name A-Z";
            case "NAME_ZA":
                return "Name Z-A";
            case "NO_OF_PRES_ASC":
                return "No of presentations ascending";
            case "NO_OF_PRES_DESC":
                return "No of presentations descending";
            default:
                return "Unknown";
        }
    }

    public static void copyAllToList(List<DashModuleSortKey> list) {
        list.add(NAME_AZ);
        list.add(NAME_ZA);
        list.add(NO_OF_PRES_ASC);
        list.add(NO_OF_PRES_DESC);
    }

    public int compare(DashModule module1, DashModule module2) {
        try {
            switch (this) {
                case NAME_AZ:
                    return module1.getModuleName().compareToIgnoreCase(module2.getModuleName());
                case NAME_ZA:
                    return -module1.getModuleName().compareToIgnoreCase(module2.getModuleName());
                case NO_OF_PRES_ASC:
                    if(module1.getPresentations().size() < module2.getPresentations().size()) {
                        return -1;
                    } else if(module1.getPresentations().size() > module2.getPresentations().size()){
                        return 1;
                    } else {
                        return 0;
                    }
                case NO_OF_PRES_DESC:
                    if(module1.getPresentations().size() < module2.getPresentations().size()) {
                        return 1;
                    } else if(module1.getPresentations().size() > module2.getPresentations().size()){
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
