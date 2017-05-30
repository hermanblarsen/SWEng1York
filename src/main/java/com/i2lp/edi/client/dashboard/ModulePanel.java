package com.i2lp.edi.client.dashboard;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.apache.xpath.operations.Mod;

import java.util.ArrayList;

/**
 * Created by Kacper on 2017-05-25.
 */
public class ModulePanel extends PreviewPanel {

    private final DashModule module;

    public ModulePanel(DashModule module, SubjectPanel parentPanel) {
        super(parentPanel.getModulePanelsHBox());
        this.module = module;
        parentPanel.addModulePanel(this);

        setText(module.getModuleName());
        setFooter(new Label(module.getPresentations().size() + " presentations"));
    }

    public String getModuleName() { return module.getModuleName(); }

    public DashModule getModule() { return module; }

    @Override
    public ArrayList<String> getSearchableTerms() {
        ArrayList<String> searchableTerms = new ArrayList<>();
        searchableTerms.add(module.getModuleName());
        searchableTerms.add(module.getSubject().getSubjectName());

        return searchableTerms;
    }

    public static ModulePanel findInArray(int moduleId, ArrayList<ModulePanel> arrayList) {
        for (ModulePanel panel : arrayList) {
            if (panel.getModule().getModuleID() == moduleId) {
                return panel;
            }
        }

        return null;
    }
}
