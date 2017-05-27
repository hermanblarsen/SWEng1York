package com.i2lp.edi.client.dashboard;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

/**
 * Created by Kacper on 2017-05-25.
 */
public class ModulePanel extends PreviewPanel {

    private final Module module;

    public ModulePanel(Module module, Pane parentPane) {
        super(parentPane);
        this.module = module;

        setText(module.getModuleName());
        setFooter(new Label(module.getPresentations().size() + " presentations"));
    }

    public String getModuleName() { return module.getModuleName(); }

    public Module getModule() { return module; }

    @Override
    public ArrayList<String> getSearchableTerms() {
        ArrayList<String> searchableTerms = new ArrayList<>();
        searchableTerms.add(module.getModuleName());
        searchableTerms.add(module.getSubject().getSubjectName());

        return searchableTerms;
    }
}
