package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.presentationElements.Presentation;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.i2lp.edi.client.Constants.SLIDE_PREVIEW_WIDTH;


/**
 * Created by Kacper on 2017-04-08.
 */
public class PresentationPanel extends PreviewPanel {
    private static final double MAX_PRES_PANEL_WIDTH = 200;
    private static final double MAX_PRES_PANEL_HEIGHT = 200;
    protected static Logger logger = LoggerFactory.getLogger(PresentationPanel.class);
    private final Presentation presentation;
    private boolean isLive;

    public PresentationPanel(Presentation presentation, Pane parentPane) { //TODO: Swap the order of input arguments
        super(parentPane);
        this.presentation = presentation;
        this.setText("Title: " + this.presentation.getDocumentTitle());
        this.setMaxSize(MAX_PRES_PANEL_WIDTH, MAX_PRES_PANEL_HEIGHT);

        isLive = presentation.isLive();

        ImageView preview = getPresentation().getSlidePreview(0, SLIDE_PREVIEW_WIDTH);
        this.setBody(preview); //TODO: set this so that all panels are the same size (fit width/height)
        this.setFooter(new Label("Subject: " + presentation.getSubject()));

        Tooltip tooltip = new Tooltip("Title: " + getPresentation().getDocumentTitle() + "\n" +
                                        "Author: " + getPresentation().getAuthor() + "\n" +
                                        "Subject: " + getPresentation().getSubject() + "\n" +
                                        "Description: " + getPresentation().getDescription() + "\n" +
                                        "Tags: " + getPresentation().getTags());
        Tooltip.install(this, tooltip);
    }

    public Presentation  getPresentation() { return presentation; }

    @Override
    public ArrayList<String> getSearchableTerms() {
        ArrayList<String> searchableTerms = new ArrayList<>();
        searchableTerms.add(presentation.getDocumentTitle());
        searchableTerms.add(presentation.getTags());
        searchableTerms.add(presentation.getAuthor());
        searchableTerms.add(presentation.getSubject().getSubjectName());

        return searchableTerms;
    }

    public boolean isLive() { return isLive; }

    public void setLive(boolean live) {
        presentation.setLive(live);
        isLive = live;

        if(live) {
            setText(getText() + " (live)");

        } else {
            setText("Title: " + this.presentation.getDocumentTitle());
        }
    }
}
