package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.presentationElements.Presentation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


/**
 * Created by Kacper on 2017-04-08.
 *
 * Panel representing  presentations in the ui.
 */
public class PresentationPanel extends PreviewPanel {
    private static final double MAX_PRES_PREVIEW_WIDTH = 200;
    private static final double MAX_PRES_PREVIEW_HEIGHT = 100;
    private static final double MARGIN_FOR_LIVE_ICON = 55;
    private static final double MARGIN_AROUND_PRES_PREVIEW = 10;
    protected static Logger logger = LoggerFactory.getLogger(PresentationPanel.class);
    private final Presentation presentation;
    private boolean isLive;
    private ImageView liveIcon;

    /**
     * Constructs a presentationPanel for a given presentation inside a given parent node
     * @param presentation
     * @param parentPane
     */
    public PresentationPanel(Presentation presentation, Pane parentPane) {
        super(parentPane);
        this.presentation = presentation;
        Label titleLabel = new Label(this.getPresentation().getDocumentTitle());
        getDisplayPanel().widthProperty().addListener(observable -> titleLabel.setPrefWidth(getDisplayPanel().getWidth() - MARGIN_FOR_LIVE_ICON));
        getDisplayPanel().setHeading(titleLabel);
        this.setPrefWidth(MAX_PRES_PREVIEW_WIDTH + MARGIN_AROUND_PRES_PREVIEW);


        isLive = presentation.getPresentationMetadata().getLive();
        presentation.getModule().getModulePanel().updateIsLive();

        double previewWidth = MAX_PRES_PREVIEW_WIDTH;

        if(presentation.getDocumentAspectRatio() < MAX_PRES_PREVIEW_WIDTH/MAX_PRES_PREVIEW_HEIGHT)
            previewWidth = presentation.getDocumentAspectRatio() * MAX_PRES_PREVIEW_HEIGHT;

        ImageView preview = getPresentation().getSlidePreview(0, previewWidth);
        if (!getPresentation().hasThumbnails()) {
            preview.setFitWidth(MAX_PRES_PREVIEW_WIDTH);
        }

        StackPane bodyPane = new StackPane(preview);
        getDisplayPanel().setBody(bodyPane);
        getDisplayPanel().setFooter(new Label("Tags: " + presentation.getTags()));

        liveIcon = new ImageView(new Image("file:projectResources/icons/live_icon.png"));
        StackPane.setAlignment(liveIcon, Pos.TOP_RIGHT);
        StackPane.setMargin(liveIcon, new Insets(2, 4, 2, 4));

        String scheduledForString = "";
        if (getPresentation().getGoLiveDateTime() != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("E, dd.MM.YYYY HH:mm");
            scheduledForString = "Scheduled for: " + getPresentation().getGoLiveDateTime().format(dtf) + "\n";
        }

        Tooltip tooltip = new Tooltip("Title: " + getPresentation().getDocumentTitle() + "\n" +
                                        "Author: " + getPresentation().getAuthor() + "\n" +
                                        "Subject: " + getPresentation().getSubject().getSubjectName() + "\n" +
                                        scheduledForString +
                                        "Tags: " + getPresentation().getTags() + "\n");
        Tooltip.install(this, tooltip);
    }

    public Presentation getPresentation() { return presentation; }

    /**
     * Returns a list of words which can be used to search against to identify this presentation
     */
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

    /**
     * Sets the isLive property and causes an update on relevant parts of this preview.
     * @param live
     */
    public void setLive(boolean live) {
        isLive = live;
        updateVisibility();
        if (presentation.isLive() != live) {
            presentation.setLive(live);
        }
        presentation.getModule().getModulePanel().updateIsLive();
    }

    /**
     * Updates the visibility of the live icon on the presentation preview.
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

    /**
     * Finds the presentation panel with a given presentation id in an array.
     * @param presentationID  The presentation id of teh presentation to search for.
     * @param arrayList The array to search in.
     * @return The Presentation Panel with teh matching ID, or null if none is found.
     */
    public static PresentationPanel findInArray(int presentationID, ArrayList<PresentationPanel> arrayList) {
        for (PresentationPanel panel : arrayList) {
            if (panel.getPresentation().getPresentationMetadata().getPresentationID() == presentationID) {
                return panel;
            }
        }

        return null;
    }

}
