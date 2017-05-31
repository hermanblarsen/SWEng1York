package com.i2lp.edi.client.dashboard;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Created by Kacper on 2017-05-31.
 */
public class PresSchedulePanel extends PreviewPanel {

    PresentationPanel presPanel;

    public PresSchedulePanel(Pane parentPane, PresentationPanel presPanel) {
        super(parentPane, false);
        this.presPanel = presPanel;

        HBox body = new HBox();
        Label title = new Label(presPanel.getPresentation().getDocumentTitle());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        Label time = new Label(presPanel.getPresentation().getGoLiveDateTime().toLocalTime().format(dtf));
        Region spacer = new Region();
        body.getChildren().addAll(title, spacer, time);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.getDisplayPanel().setBody(body);
    }

    @Override
    public ArrayList<String> getSearchableTerms() {
        return null;
    }

    public LocalDateTime getGoLiveDateTime() { return presPanel.getPresentation().getGoLiveDateTime(); }
}
