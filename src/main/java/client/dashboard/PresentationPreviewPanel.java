package client.dashboard;

import client.utilities.ParserXML;
import javafx.scene.image.ImageView;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.util.function.Predicate;

/**
 * Created by Kacper on 2017-04-08.
 */
public class PresentationPreviewPanel extends Panel {
    private final String presentationPath;
    private final String presentationID;
    private boolean isSelected;

    public PresentationPreviewPanel(String presentationPath) {
        super();
        this.presentationPath = presentationPath;
        this.isSelected = false;
        this.getStyleClass().add("panel-primary");

        ParserXML parser = new ParserXML(presentationPath);
        presentationID = parser.getPresentationId();

        this.setText("ID: " + presentationID);

        ImageView preview;
        try {
            preview = new ImageView("file:"+ System.getProperty("java.io.tmpdir") + "Edi/Thumbnails/" + presentationID + "_slide0_thumbnail.png");
        } catch(NullPointerException | IllegalArgumentException e) {
            preview = new ImageView("file:externalResources/emptyThumbnail.png");
        }

        preview.setFitWidth(150);
        preview.setPreserveRatio(true);
        preview.setSmooth(true);
        preview.setCache(true);
        this.setBody(preview);
    }

    public PresentationPreviewPanel(){
        this("file:externalResources/sampleXMLsimple.xml");
    }

    public String getPresentationPath() {
        return presentationPath;
    }

    public boolean isSelected() { return isSelected; }

    public void setSelected(boolean selected) {
        isSelected = selected;

        if(isSelected) {
            this.getStyleClass().removeIf(new Predicate<String>() {
                @Override
                public boolean test(String s) {
                    if(s.equals("panel-primary")) return true;
                    else return false;
                }
            });
            this.getStyleClass().add("panel-success");
        } else {
            this.getStyleClass().removeIf(new Predicate<String>() {
                @Override
                public boolean test(String s) {
                    if(s.equals("panel-success")) return true;
                    else return false;
                }
            });
            this.getStyleClass().add("panel-primary");
        }
    }

    public String getPresentationID() { return presentationID; }
}
