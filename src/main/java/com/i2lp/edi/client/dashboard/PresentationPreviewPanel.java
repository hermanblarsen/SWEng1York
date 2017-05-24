package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.utilities.ParserXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Random;


/**
 * Created by Kacper on 2017-04-08.
 */
public class PresentationPreviewPanel extends Panel {
    protected static Logger logger = LoggerFactory.getLogger(Dashboard.class);
    private final Pane parentPane;
    private final String presentationPath;
    private final Presentation presentation;
    private boolean isSelected;
    private boolean isHidden;

    public PresentationPreviewPanel(Pane parentPane, String presentationPath) {
        super();
        this.parentPane = parentPane;
        this.presentationPath = presentationPath;
        this.setSelected(false);
        this.setHidden(false);
        this.getStyleClass().add("panel-primary");

        DropShadow shadow = new DropShadow();
        this.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> setEffect(shadow));
        this.addEventHandler(MouseEvent.MOUSE_EXITED, event -> setEffect(null));

        ParserXML parser = new ParserXML(presentationPath);
        presentation = parser.parsePresentation();

        this.setText("ID: " + this.presentation.getDocumentID());

        this.setBody(getPresentation().getSlidePreview(0, 170)); //TODO: Make width a constant
        this.setFooter(new Label("Subject: " + presentation.getSubject()));

        Tooltip tooltip = new Tooltip("Title: " + getPresentation().getDocumentTitle() + "\n" +
                                        "Author: " + getPresentation().getAuthor() + "\n" +
                                        "Subject: " + getPresentation().getSubject() + "\n" +
                                        "Description: " + getPresentation().getDescription() + "\n" +
                                        "Tags: " + getPresentation().getTags());
        Tooltip.install(this, tooltip);
    }

    public PresentationPreviewPanel(Pane parentPane){
        this(parentPane, "file:projectResources/sampleFiles/xml/sampleXmlSimple.xml");
    }

    public String getPresentationPath() {
        return presentationPath;
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

    //public String getPresentationID() { return presentation.getDocumentID(); }

    public String getPresentationSubject() { return presentation.getSubject(); }

    public Presentation  getPresentation() { return presentation; }

    public boolean isHidden() { return isHidden; }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
        if(hidden)
            try {
                parentPane.getChildren().remove(this);
            } catch(NullPointerException e) {
                logger.info("Couldn't hide previewPanel");
                //Do nothing
            }
        else
            parentPane.getChildren().add(this);
    }
}
