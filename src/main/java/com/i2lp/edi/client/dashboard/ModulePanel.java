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
 *
 * UI Container for Teaching Modules.
 */
public class ModulePanel extends PreviewPanel {

    public static final int WIDTH = 170;
    private static final double MARGIN_FOR_LIVE_ICON = 50;
    private final DashModule module;
    private boolean isLive = false;
    private ImageView liveIcon;
    private SubjectPanel subjectPanel;

    /**
     * Constructs a ModulePanel based on a given module and its parent subject.
     * @param module The module the module belongs to.
     * @param parentPanel
     */
    public ModulePanel(DashModule module, SubjectPanel parentPanel) {
        super(parentPanel.getModulePanelsHBox());
        this.module = module;
        module.setModulePanel(this);
        parentPanel.addModulePanel(this);
        getDisplayPanel().setPrefWidth(WIDTH);
        getDisplayPanel().setMinWidth(Panel.USE_PREF_SIZE);
        getDisplayPanel().setMaxWidth(Panel.USE_PREF_SIZE);

        Label heading = new Label(module.getModuleName());
        getDisplayPanel().setHeading(heading);
        heading.setMaxWidth(WIDTH - MARGIN_FOR_LIVE_ICON);
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

    /**
     * Returns an ArrayList of items which could match a search for thig module.
     * @return An arraylist of relevant terms which can be used to match search terms.
     */
    @Override
    public ArrayList<String> getSearchableTerms() {
        ArrayList<String> searchableTerms = new ArrayList<>();
        searchableTerms.add(module.getModuleName());
        searchableTerms.add(module.getSubject().getSubjectName());

        return searchableTerms;
    }

    /**
     * Finds a module in an array based on the module ID
     * @param moduleId Id to search for
     * @param arrayList List to search in
     * @return The ModulePanel for the corresponding module.
     */
    public static ModulePanel findInArray(int moduleId, ArrayList<ModulePanel> arrayList) {
        for (ModulePanel panel : arrayList) {
            if (panel.getModule().getModuleID() == moduleId) {
                return panel;
            }
        }

        return null;
    }

    /**
     * Updates the isLive indicator on the module panels.
     */
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

    /**
     * Adds or removed the live icon from the module panel, depending on whether isLive is set.
     */
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
    }

    public SubjectPanel getSubjectPanel() { return subjectPanel; }

    public void setSubjectPanel(SubjectPanel subjectPanel) { this.subjectPanel = subjectPanel; }
}
