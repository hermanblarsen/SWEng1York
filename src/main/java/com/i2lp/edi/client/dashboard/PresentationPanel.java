package com.i2lp.edi.client.dashboard;

import com.i2lp.edi.client.presentationElements.Presentation;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Kacper on 2017-04-08.
 */
public class PresentationPanel extends PreviewPanel {
    protected static Logger logger = LoggerFactory.getLogger(PresentationPanel.class);
    private final String presentationPath;
    private final Presentation presentation;

    public PresentationPanel(Pane parentPane, Presentation presentation) {
        super(parentPane);
        this.presentation = presentation;
        this.presentationPath = presentation.getPath();

        this.setText("Title: " + this.presentation.getDocumentTitle());

        this.setBody(getPresentation().getSlidePreview(0, 170)); //TODO: Make width a constant
        this.setFooter(new Label("Subject: " + presentation.getSubject()));

        Tooltip tooltip = new Tooltip("Title: " + getPresentation().getDocumentTitle() + "\n" +
                                        "Author: " + getPresentation().getAuthor() + "\n" +
                                        "Subject: " + getPresentation().getSubject() + "\n" +
                                        "Description: " + getPresentation().getDescription() + "\n" +
                                        "Tags: " + getPresentation().getTags());
        Tooltip.install(this, tooltip);
    }

    public String getPresentationPath() {
        return presentationPath;
    }

    //public String getPresentationID() { return presentation.getDocumentID(); }

    public String getPresentationSubject() { return presentation.getSubject(); }

    public Presentation  getPresentation() { return presentation; }
}
