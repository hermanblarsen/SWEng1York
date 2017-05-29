package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.presentationElements.Presentation;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


/**
 * Created by Kacper on 2017-04-08.
 */
public class PresentationPanel extends PreviewPanel {
    private static final double MAX_PRES_PREVIEW_WIDTH = 200;
    private static final double MAX_PRES_PREVIEW_HEIGHT = 100;
    protected static Logger logger = LoggerFactory.getLogger(PresentationPanel.class);
    private final Presentation presentation;
    private boolean isLive;

    public PresentationPanel(Presentation presentation, Pane parentPane) {
        super(parentPane);
        this.presentation = presentation;
        this.setText("Title: " + this.presentation.getDocumentTitle());
        this.setPrefWidth(MAX_PRES_PREVIEW_WIDTH + 10);

        isLive = presentation.getServerSideDetails().getLive();

        double previewWidth = MAX_PRES_PREVIEW_WIDTH;

        if(presentation.getDocumentAspectRatio() < MAX_PRES_PREVIEW_WIDTH/MAX_PRES_PREVIEW_HEIGHT)
            previewWidth = presentation.getDocumentAspectRatio() * MAX_PRES_PREVIEW_HEIGHT;

        ImageView preview = getPresentation().getSlidePreview(0, previewWidth);
        StackPane bodyPane = new StackPane(preview);
        this.setBody(bodyPane);
        this.setFooter(new Label("Subject: " + presentation.getSubject().getSubjectName()));

        Tooltip tooltip = new Tooltip("Title: " + getPresentation().getDocumentTitle() + "\n" +
                                        "Author: " + getPresentation().getAuthor() + "\n" +
                                        "Subject: " + getPresentation().getSubject().getSubjectName() + "\n" +
                                        "Description: " + getPresentation().getDescription() + "\n" +
                                        "Tags: " + getPresentation().getTags());
        Tooltip.install(this, tooltip);
    }

    public Presentation getPresentation() { return presentation; }

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
        presentation.getServerSideDetails().setLive(live);
        isLive = live;
        updateVisibility();
    }

    @Override
    public void updateVisibility() {
        super.updateVisibility();

        try {
            if(isLive) {
                setText("Title: " + this.presentation.getDocumentTitle() + " (live)");

            } else {
                setText("Title: " + this.presentation.getDocumentTitle());
            }
        } catch (NullPointerException e) {
            //Do nothing
        }
    }
}
