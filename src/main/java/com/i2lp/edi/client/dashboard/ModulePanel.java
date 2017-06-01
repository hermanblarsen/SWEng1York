package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.presentationElements.Presentation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.util.ArrayList;

/**
 * Created by Kacper on 2017-05-25.
 */
public class ModulePanel extends PreviewPanel {

    public static final int WIDTH = 140;
    private final DashModule module;
    private boolean isLive = false;
    private ImageView liveIcon;
    private SubjectPanel subjectPanel;

    public ModulePanel(DashModule module, SubjectPanel parentPanel) {
        super(parentPanel.getModulePanelsHBox());
        this.module = module;
        module.setModulePanel(this);
        parentPanel.addModulePanel(this);
        getDisplayPanel().setPrefWidth(WIDTH);
        getDisplayPanel().setMinWidth(Panel.USE_PREF_SIZE);
        getDisplayPanel().setMaxWidth(Panel.USE_PREF_SIZE);

        getDisplayPanel().setText(module.getModuleName());
        getDisplayPanel().setFooter(new Label(module.getPresentations().size() + " presentations"));

        liveIcon = new ImageView(new Image("file:projectResources/icons/live_icon.png"));
        StackPane.setAlignment(liveIcon, Pos.TOP_RIGHT);
        StackPane.setMargin(liveIcon, new Insets(2, 4, 2, 4));

        Tooltip tooltip = new Tooltip("Name: " + getModuleName() + "\n" +
                                        "Subject: " + getModule().getSubject().getSubjectName() + "\n" +
                                        "Description: " + getModule().getModuleDescription() + "\n" +
                                        "Last updated: " + getModule().getTimeLastUpdated());
        Tooltip.install(this, tooltip);
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

    public void updateIsLive() {
        boolean isLive = false;
        for (Presentation presentation : module.getPresentations()) {
            if (presentation.getPresentationMetadata().getLive()) {
                isLive = true;
                break;
            }
        }

        this.isLive = isLive;
        updateVisibility();
    }

    @Override
    public void updateVisibility() {
        super.updateVisibility();

        try {
            if(isLive) {
                this.getChildren().add(liveIcon);
            } else {
                this.getChildren().remove(liveIcon);
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            //Do nothing
        }

        if (subjectPanel != null) {
            subjectPanel.layoutBorderPane();
        }
    }

    public SubjectPanel getSubjectPanel() { return subjectPanel; }

    public void setSubjectPanel(SubjectPanel subjectPanel) { this.subjectPanel = subjectPanel; }
}
