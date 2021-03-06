package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.presentationElements.Slide;
import com.i2lp.edi.client.presentationElements.TextElement;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import static com.i2lp.edi.client.Constants.*;

/**
 * Created by amriksadhra on 12/04/2017.
 */

/**
 * Thumbnail generator based on presentation manager, generating thumbnails
 * for presentations in the background.
 */
public class ThumbnailGenerationManager extends PresentationManager {

    private static Logger logger = LoggerFactory.getLogger(ThumbnailGenerationManager.class);
    private File thumbnailFile;

    public ThumbnailGenerationManager(EdiManager ediManager) {
        super(ediManager);
    }

    /**
     * Generate slide thumbnails for presentation, and save to PDF if boolean is true
     *
     * @param presentation          presentation to generate thumbnails for
     * @param savePresentationToPdf true if we want to save to PDF
     */
    public static void generateSlideThumbnails(Presentation presentation, boolean savePresentationToPdf) {
        ThumbnailGenerationManager slideGenController = new ThumbnailGenerationManager(null);
        slideGenController.openPresentation(presentation, true);
        slideGenController.generateSlideThumbNail(slideGenController, savePresentationToPdf);
        if (savePresentationToPdf) slideGenController.savePresentationToPdf(slideGenController);
    }

    public void openPresentation(Presentation presentation, boolean printToggle) {
        this.presentationElement = presentation;
        presentationStage = new Stage();
        displayPane = new StackPane();
        displayPane.setAlignment(Pos.TOP_LEFT);
        //Lower resolution for thumbnails
        if (!printToggle) {
            scene = new Scene(displayPane, THUMBNAIL_GEN_WIDTH, THUMBNAIL_GEN_HEIGHT);
        } else {
            scene = new Scene(displayPane, PRINT_WIDTH_300DPI, PRINT_HEIGHT_300DPI);
        }
        presentationStage.setScene(scene);
        presentationStage.show();

        //Hide the presentation manager
        presentationStage.toBack();
        assignAttributes(presentationElement);
    }

    @Override
    protected void loadSpecificFeatures() {
    }//Empty

    @Override
    protected void toggleCommentsWindow() {
    }//Empty

    @Override
    protected void createCommentPanel() {
    }//Empty

    @Override
    protected VBox addQuestionQueueControls() {
        return null;
    }

    /**
     * Generate slide thumbnails for presentation, and save to PDF if boolean is true
     *
     * @param slideGenController    slideGenController to save from
     * @param savePresentationToPdf true if wanting to save to PDF
     */
    public void generateSlideThumbNail(ThumbnailGenerationManager slideGenController, boolean savePresentationToPdf) {
        Presentation presentation = slideGenController.presentationElement;

        //Check if thumbnail already there
        if (!savePresentationToPdf) {
            if (slideGenController.presentationElement.getPresentationMetadata() != null) {
                thumbnailFile = new File(PRESENTATIONS_PATH + File.separator + presentation.getModule().getModuleName() + File.separator + this.presentationElement.getDocumentID() + "/Thumbnails/" + "slide" + (slideGenController.currentSlideNumber) + "_thumbnail.png");
            } else {
                thumbnailFile = new File(TEMP_PATH + slideGenController.getPresentationElement().getDocumentID() + "/Thumbnails/" + "slide" + (slideGenController.currentSlideNumber) + "_thumbnail.png");
            }
        } else {
            thumbnailFile = new File(PRESENTATIONS_PATH + File.separator + presentation.getModule().getModuleName() + File.separator + this.presentationElement.getDocumentID() + "/Print/" + "slide" + (slideGenController.currentSlideNumber) + "_thumbnail.png");
        }

        if (!thumbnailFile.exists()) {
            thumbnailFile.getParentFile().mkdirs(); //Create directory structure if not present yet
        }

        //Move to end of current slide so all elements are visible in snapshot.
        int moveStatus = Presentation.SAME_SLIDE;
        while (moveStatus != Presentation.SLIDE_LAST_ELEMENT && moveStatus != Presentation.SLIDE_CHANGE) {
            moveStatus = slideGenController.slideAdvance(presentation, Slide.SLIDE_FORWARD);
            if (moveStatus == Presentation.PRESENTATION_FINISH) {
                break;
            }
        }

        //If we're in last element of slide, take snapshot
        displayCurrentSlide();

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
                    //This value may need to be upped on slower systems to ensure successful screenshot
                    Thread.sleep(100);
                    return null;
                }
            }
        };

        //Begin to check for webview render finish
        Thread webviewRenderCheckThread = new Thread(webviewRenderChecker);
        webviewRenderCheckThread.start();

        //When webviews rendered, can take snapshot
        int finalMoveStatus = moveStatus;
        webviewRenderChecker.setOnSucceeded(event ->
        {
            logger.info("Generating thumbnail file for " + presentation.getDocumentID() + " Slide " + (slideGenController.currentSlideNumber) + " at " + thumbnailFile.getAbsolutePath());
            Slide slideToRender = presentation.getSlide(slideGenController.currentSlideNumber);
            SnapshotParameters snapParam = new SnapshotParameters();
            Point2D upperLeft = slideToRender.sceneToLocal(0, 0);
            Point2D lowerRight = slideToRender.sceneToLocal(slideToRender.getWidth(), slideToRender.getHeight());
            snapParam.setViewport(new Rectangle2D(upperLeft.getX(), upperLeft.getY(), lowerRight.getX(), lowerRight.getY()));
            WritableImage thumbnail = slideToRender.snapshot(snapParam, null);
            try {
                //Write the snapshot to the chosen file
                if (!thumbnailFile.exists()) {
                    ImageIO.write(SwingFXUtils.fromFXImage(thumbnail, null), "png", thumbnailFile);
                }
                //Advance to next slide, and generate next Slide Thumbnail
                if (finalMoveStatus == Presentation.PRESENTATION_FINISH) {
                    logger.info("Done generating thumbnails for presentation " + presentation.getDocumentID());
                    slideGenController.close();
                    return;
                } else {
                    generateSlideThumbNail(slideGenController, savePresentationToPdf);
                }
            } catch (IOException ex) {
                logger.error("Generating presentation thumbnail for " + presentation.getDocumentID() + " at " + thumbnailFile.getAbsolutePath() + " failed");
            }
        });
    }

    /**
     * Save presentation to PDF
     * @param slideGenController contrller to save from
     */
    private void savePresentationToPdf(ThumbnailGenerationManager slideGenController) {
        PDDocument doc = new PDDocument();
        Presentation presentation = slideGenController.presentationElement;

        File path = new File(PRESENTATIONS_PATH + File.separator + presentation.getModule().getModuleName() + File.separator + this.presentationElement.getDocumentID() + "/Print/");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        File file = new File(System.getProperty("user.home"), "Desktop");
        fileChooser.setInitialDirectory(file);
        fileChooser.setInitialFileName(presentationElement.getDocumentID());
        Stage saveStage = new Stage();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        File pdfPath = fileChooser.showSaveDialog(saveStage);


        if (pdfPath == null) {
            logger.info("Invalid path entered! PDF Not Generated");
        }
        if (pdfPath != null) {
            if (pdfPath.exists()) {
                pdfPath.delete();
            }
            logger.info("PDF Save path: " + pdfPath.getAbsolutePath());
            String[] ext = {"png"};
            FilenameFilter imageFilter = (dir, name) -> {
                for (final String ext1 : ext) {
                    if (name.endsWith("." + ext1)) {
                        return true;
                    }
                }
                return false;
            };
            if (path.isDirectory()) {
                for (File f : path.listFiles(imageFilter)) {

                    try {
                        logger.info("Slide Image Generation Path: " + f.getAbsolutePath().toString());
                        PDPage page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        PDImageXObject imageObject = PDImageXObject.createFromFile(f.getAbsolutePath(), doc);
                        PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, false);
                        float imageWidth = imageObject.getWidth();
                        float imageHeight = imageObject.getHeight();
                        float imageAspectRatio = imageWidth / imageHeight;
                        PDRectangle mediaBox = page.getMediaBox();
                        float margin = 10;
                        float boxWidth = mediaBox.getWidth() - 2 * margin;
                        float boxHeight = mediaBox.getHeight() - 2 * margin;
                        float boxAspectRatio = boxWidth / boxHeight;

                        if (imageAspectRatio > boxAspectRatio) {
                            contentStream.drawImage(imageObject, margin, mediaBox.getHeight() - boxWidth / imageAspectRatio - margin, boxWidth, boxWidth / imageAspectRatio);
                        } else {
                            contentStream.drawImage(imageObject, margin, mediaBox.getHeight() - boxHeight * imageAspectRatio - margin, mediaBox.getHeight() * imageAspectRatio, boxHeight);
                        }

                        contentStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    doc.save(pdfPath);
                    doc.close();
                    logger.info("PDF Generation Complete.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Desktop.getDesktop().open(pdfPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
