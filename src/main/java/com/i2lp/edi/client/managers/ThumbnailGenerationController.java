package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.presentationElements.Slide;
import com.i2lp.edi.client.presentationElements.TextElement;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Created by amriksadhra on 12/04/2017.
 */
public class ThumbnailGenerationController extends PresentationController {
    private static Logger logger = LoggerFactory.getLogger(ThumbnailGenerationController.class);

    public void openPresentation(String path) {
        presentationStage = new Stage();
        displayPane = new StackPane();
        //Lower resolution for thumbnails
        scene = new Scene(displayPane, 320, 240);
        presentationStage.setScene(scene);
        presentationStage.show();

        //Hide the presentation manager
        //TODO: Put the stage in the bottom right of
        presentationStage.toBack();
        loadPresentation(path);
    }

    @Override
    protected void loadSpecificFeatures() {

    }


    @Override
    protected void toggleComments() {

    }

    public static void generateSlideThumbnails(String presentationPath) {
        ThumbnailGenerationController slideGenController = new ThumbnailGenerationController();
        slideGenController.openPresentation(presentationPath);
        slideGenController.generateSlideThumbNail(slideGenController);
    }

    public void generateSlideThumbNail(ThumbnailGenerationController slideGenController) {
        Presentation presentation = slideGenController.presentationElement;

        //Check if thumbnail already there
        File thumbnailFile = new File(this.getXmlPath() + "/Thumbnails/" + "slide" + (slideGenController.currentSlideNumber) + "_thumbnail.png");
        if (!thumbnailFile.exists()) {
            thumbnailFile.getParentFile().mkdirs(); //Create directory structure if not present yet
        } else {
            logger.debug("Thumbnail at " + thumbnailFile.getAbsolutePath() + " already exists");
            slideGenController.close(); //TODO: This causes thumbGen to close even if all thumbs are missing apart from the first one
            if (slideGenController.slideAdvance(presentation, Slide.SLIDE_FORWARD) == Presentation.PRESENTATION_FINISH) {
                logger.info("Done generating thumbnails for presentation " + presentation.getDocumentID());
                slideGenController.close();
            } else {
                generateSlideThumbNail(slideGenController);
            }
            return;
        }

        //Move to end of current slide so all elements are visible in snapshot
        //noinspection StatementWithEmptyBody
        while (slideGenController.slideAdvance(presentation, Slide.SLIDE_FORWARD) != Presentation.SLIDE_LAST_ELEMENT);
        //If we're in last element of slide, take snapshot


        //This task will succeed when the webviews have all rendered
        Task webviewRenderChecker = new Task() {
            @Override
            protected Object call() throws Exception {
                //WebViews don't render immediately, so text doesn't show in snapshots.
                if (presentation.getSlide(slideGenController.currentSlideNumber).getTextElementList().isEmpty()) {
                    //If no TextElements, skip the render delay
                    return null;
                } else {
                    boolean renderDone;
                    //Wait for all elements to be done rendering.
                    while (true) {
                        renderDone = true;
                        //Iterate through elements, query if rendered
                        for (TextElement toGetRenderStatus : presentation.getSlide(slideGenController.currentSlideNumber).getTextElementList()) {
                            if (!toGetRenderStatus.isRendered()) renderDone = false;
                        }
                        if (renderDone) break;
                    }
                    logger.debug("All webviews on TextElements in slide " + (slideGenController.currentSlideNumber) + " have completed rendering.");
                    //TODO: Even though the webview has told us its done rendering, there is some overhead before it is visible on StackPane. Account for this with minor delay. I cant find any state variable that we can check to avoid waiting. Maybe you can Kacper
                    //This value may need to be upped on slower systems to ensure successful screenshot
                    Thread.sleep(50);
                    return null;
                }
            }
        };

        //Begin to check for webview render finish
        Thread webviewRenderCheckThread = new Thread(webviewRenderChecker);
        webviewRenderCheckThread.start();

        //When webviews rendered, can take snapshot
        webviewRenderChecker.setOnSucceeded(event ->
        {
            logger.info("Generating thumbnail file for " + presentation.getDocumentID() + " Slide " + (slideGenController.currentSlideNumber) + " at " + thumbnailFile.getAbsolutePath());
            WritableImage thumbnail = presentation.getSlide(slideGenController.currentSlideNumber).snapshot(new SnapshotParameters(), null);
            try {
                //Write the snapshot to the chosen file
                ImageIO.write(SwingFXUtils.fromFXImage(thumbnail, null), "png", thumbnailFile);
                //Advance to next slide, and generate next Slide Thumbnail
                if (slideGenController.slideAdvance(presentation, Slide.SLIDE_FORWARD) == Presentation.PRESENTATION_FINISH) {
                    logger.info("Done generating thumbnails for presentation " + presentation.getDocumentID());
                    slideGenController.close();
                } else {
                    generateSlideThumbNail(slideGenController);
                }
            } catch (IOException ex) {
                logger.error("Generating presentation thumbnail for " + presentation.getDocumentID() + " at " + thumbnailFile.getAbsolutePath() + " failed");
            }
        });
    }

    @Override
    protected void displayCurrentSlide() {
        displayPane.getChildren().clear();
        Slide slide = presentationElement.getSlide(currentSlideNumber);
        slide.setBackground(new Background(new BackgroundFill(Color.valueOf(presentationElement.getTheme().getBackgroundColour()), null, null)));
        displayPane.getChildren().add(slide);

        resize();
    }
}
