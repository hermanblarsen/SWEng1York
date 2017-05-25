package com.i2lp.edi.client.dashboard;

import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Kacper on 2017-05-25.
 */
public abstract class PreviewPanel extends Panel {

    protected static Logger logger = LoggerFactory.getLogger(PreviewPanel.class);
    private final Pane parentPane;
    private boolean isSelected;
    private boolean isHidden;

    public PreviewPanel(Pane parentPane) {
        this.parentPane = parentPane;

        this.setSelected(false);
        this.setHidden(false);
        this.getStyleClass().add("panel-primary");

        DropShadow shadow = new DropShadow();
        this.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> setEffect(shadow));
        this.addEventHandler(MouseEvent.MOUSE_EXITED, event -> setEffect(null));
    }

    public boolean isSelected() { return isSelected; }

    public void setSelected(boolean selected) {
        isSelected = selected;

        if(isSelected) {
            this.getStyleClass().removeIf(s -> {
                if(s.equals("panel-primary")) return true;
                else return false;
            });
            this.getStyleClass().add("panel-success");
        } else {
            this.getStyleClass().removeIf(s -> {
                if(s.equals("panel-success")) return true;
                else return false;
            });
            this.getStyleClass().add("panel-primary");
        }
    }

    public boolean isHidden() { return isHidden; }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
        if(hidden)
            try {
                parentPane.getChildren().remove(this);
            } catch(NullPointerException e) {
                logger.debug("Couldn't hide previewPanel");
                //Do nothing
            }
        else
            parentPane.getChildren().add(this);
    }
}
