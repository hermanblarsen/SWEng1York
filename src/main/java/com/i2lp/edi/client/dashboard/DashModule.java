package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.server.packets.Module;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by Kacper on 2017-05-25.
 */
public class DashModule {

    private final String moduleName;
    private final int moduleID;

    private final String moduleDescription;
    private final Subject subject;
    private final Time timeLastUpdated;
    private final Timestamp timeCreated;
    private final ArrayList<Presentation> presentations;
    private ModulePanel modulePanel;

    public DashModule(Module module, Subject subject) {
        moduleName = module.getModule_name();
        moduleID = module.getModule_id();
        moduleDescription = module.getDescription();
        timeLastUpdated = module.getTime_last_updated();
        timeCreated = module.getTime_created();
        this.subject = subject;

        presentations = new ArrayList<>();
        subject.addModule(this);
    }

    public String getModuleName() { return moduleName; }

    public ArrayList<Presentation> getPresentations() { return presentations; }

    public void addPresentation(Presentation presentation) {
        presentations.add(presentation);
        presentation.setModule(this);
    }

    public int getModuleID() { return moduleID; }

    public Subject getSubject() { return subject; }

    public static DashModule findInArray(int moduleID, ArrayList<DashModule> arrayList) {
        for (DashModule module : arrayList) {
            if (module.getModuleID() == moduleID) {
                return module;
            }
        }

        return  null;
    }

    @Override
    public String toString() { return moduleName; }

    public String getModuleDescription() { return moduleDescription; }

    public Time getTimeLastUpdated() { return timeLastUpdated; }

    public Timestamp getTimeCreated() { return timeCreated; }

    public void setModulePanel(ModulePanel panel) { this.modulePanel = panel; }

    public ModulePanel getModulePanel() { return  modulePanel; }
}
