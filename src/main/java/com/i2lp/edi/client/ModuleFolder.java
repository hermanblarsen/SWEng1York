package com.i2lp.edi.client;

import java.util.ArrayList;

/**
 * Created by amriksadhra on 02/06/2017.
 */
public class ModuleFolder {


    String moduleName;
    ArrayList<String> presentations = new ArrayList<>();


    public ModuleFolder(String moduleName) {
        this.moduleName = moduleName;
    }

    public ModuleFolder(String moduleName, ArrayList<String> presentations) {
        this.moduleName = moduleName;
        this.presentations = presentations;
    }

    public void addPresentation(String presentationDocumentID){
        presentations.add(presentationDocumentID);
    }

    public String getModuleName() {
        return moduleName;
    }

    public ArrayList<String> getPresentations() {
        return presentations;
    }

    public int size(){
        return presentations.size();
    }

    public boolean isEmpty(){
        return presentations.isEmpty();
    }
}
