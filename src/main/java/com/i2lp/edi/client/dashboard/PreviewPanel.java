package com.i2lp.edi.client.dashboard;

import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by Kacper on 2017-05-25.
 */
public abstract class PreviewPanel extends Panel {

    protected static Logger logger = LoggerFactory.getLogger(PreviewPanel.class);
    private final Pane parentPane;
    private boolean isSelected;
    private boolean isHidden = false;
    private boolean isFiltered = false, isSearchResult = true;

    public PreviewPanel(Pane parentPane, boolean dropshadow) {
        this.parentPane = parentPane;
        this.setSelected(false);
        this.getStyleClass().add("panel-primary");
        this.updateVisibility();

        if(dropshadow) {
            DropShadow shadow = new DropShadow();
            this.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> setEffect(shadow));
            this.addEventHandler(MouseEvent.MOUSE_EXITED, event -> setEffect(null));
        }
    }

    public PreviewPanel(Pane parentPane) {
        this(parentPane, true);
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

    private void updateVisibility() {
        try {
            if(isHidden() && parentPane.getChildren().contains(this)) {
                parentPane.getChildren().remove(this);
            } else if(!isHidden() && !parentPane.getChildren().contains(this)) {
                parentPane.getChildren().add(this);
            }
        } catch(NullPointerException e) {
            logger.debug("Couldn't show/hide previewPanel");
            //Do nothing
        }
    }

    public boolean isFiltered() { return isFiltered; }

    public void setFiltered(boolean filtered) {
        isFiltered = filtered;
        updateVisibility();
    }

    public boolean isSearchResult() { return isSearchResult; }

    public void setSearchResult(boolean searchResult) {
        isSearchResult = searchResult;
        updateVisibility();
    }

    public boolean isHidden() { return isFiltered || !isSearchResult || isHidden; }

    protected void setHidden(boolean hidden) {
        this.isHidden = hidden;
        updateVisibility();
    }

    public abstract ArrayList<String> getSearchableTerms();

    public void search(String text) {
        boolean match = false;

        for(String term : this.getSearchableTerms()) {
            if(StringUtils.containsIgnoreCase(term, text)) {
                match = true;
                break;
            }
        }

        if(match) {
            this.setSearchResult(true);
        } else {
            this.setSearchResult(false);
        }
    }
}
