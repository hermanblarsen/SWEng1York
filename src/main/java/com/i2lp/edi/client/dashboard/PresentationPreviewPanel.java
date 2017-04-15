package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.utilities.ParserXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.function.Predicate;

/**
 * Created by Kacper on 2017-04-08.
 */
public class PresentationPreviewPanel extends Panel {
    protected static Logger logger = LoggerFactory.getLogger(Dashboard.class);
    private final Pane parentPane;
    private final String presentationPath;
    private final String presentationID;
    private final String presentationSubject;
    private boolean isSelected;
    private boolean isHidden;

    public PresentationPreviewPanel(Pane parentPane, String presentationPath) {
        super();
        this.parentPane = parentPane;
        this.presentationPath = presentationPath;
        this.setSelected(false);
        this.setHidden(false);
        this.getStyleClass().add("panel-primary");

        ParserXML parser = new ParserXML(presentationPath);
        presentationID = parser.getPresentationId();
        Random random = new Random();
        presentationSubject = new String("Subject " + random.nextInt(3)); //TODO: Get presentation subject from XML

        this.setText("ID: " + presentationID);

        ImageView preview;
        try {
            preview = new ImageView("file:"+ System.getProperty("java.io.tmpdir") + "Edi/Thumbnails/" + presentationID + "_slide0_thumbnail.png");
        } catch(NullPointerException | IllegalArgumentException e) {
            preview = new ImageView("file:projectResources/emptyThumbnail.png");
        }

        preview.setFitWidth(150);
        preview.setPreserveRatio(true);
        preview.setSmooth(true);
        preview.setCache(true);
        this.setBody(preview);
        this.setFooter(new Label("Subject: " + presentationSubject));
    }

    public PresentationPreviewPanel(Pane parentPane){
        this(parentPane, "file:projectResources/sampleXMLsimple.xml");
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

    public String getPresentationID() { return presentationID; }

    public String getPresentationSubject() { return presentationSubject; }

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
