package com.i2lp.edi.client.dashboard;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Created by Kacper on 2017-05-31.
 *
 * Represents a schedule viewer used for visulising the scheduled go live date of presentations.
 */
public class PresSchedulePanel extends PreviewPanel {

    PresentationPanel presPanel;

    /**
     * Creates a new Presentation Schedule Viewer for a given presentation (Given via a PresentationPanel) and a parent node.
     * @param parentPane The parent note of this Scheddule viewer
     * @param presPanel The PresentationPanel of the presentation this shecule viewer is associated with.
     */
    public PresSchedulePanel(Pane parentPane, PresentationPanel presPanel) {
        super(parentPane, false);
        this.presPanel = presPanel;
        getDisplayPanel().setMaxWidth(Dashboard.RIGHT_PANEL_WIDTH);

        HBox body = new HBox();
        Label title = new Label(presPanel.getPresentation().getDocumentTitle());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        Label time = new Label(presPanel.getPresentation().getGoLiveDateTime().toLocalTime().format(dtf));
        time.setMinWidth(Label.USE_PREF_SIZE);
        Region spacer = new Region();
        body.getChildren().addAll(title, spacer, time);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.getDisplayPanel().setBody(body);
    }

    /**
     * Not used in this context.
     * @return null
     */
    @Override
    public ArrayList<String> getSearchableTerms() {
        return null;
    }

    public LocalDateTime getGoLiveDateTime() { return presPanel.getPresentation().getGoLiveDateTime(); }
}
