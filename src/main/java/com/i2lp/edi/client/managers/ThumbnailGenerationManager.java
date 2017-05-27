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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
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
public class ThumbnailGenerationManager extends PresentationManager {

    private static Logger logger = LoggerFactory.getLogger(ThumbnailGenerationManager.class);
    private File thumbnailFile;

    public void openPresentation(String path, boolean printToggle) {
        presentationStage = new Stage();
        displayPane = new StackPane();
        //Lower resolution for thumbnails
        if(!printToggle) {
            scene = new Scene(displayPane, THUMBNAIL_GEN_WIDTH, THUMBNAIL_GEN_HEIGHT);
        }else{
            scene = new Scene(displayPane,PRINT_WIDTH_300DPI,PRINT_HEIGHT_300DPI);
        }
        presentationStage.setScene(scene);
        presentationStage.show();

        //Hide the presentation manager
        //TODO: Put the stage in the bottom right of the screen
        //TODO a possibility would be to only show it exactly when the snapshot is taken, and then rehide it again - Herman
        presentationStage.toBack();
        loadPresentation(path);
    }

    @Override
    protected void loadSpecificFeatures() {}//Empty

    @Override
    protected void toggleCommentsWindow() {}//Empty

    @Override
    protected void createCommentPanel() {}//Empty

    public static void generateSlideThumbnails(String presentationPath, boolean savePresentationToPdf) {
        ThumbnailGenerationManager slideGenController = new ThumbnailGenerationManager();
        slideGenController.openPresentation(presentationPath, savePresentationToPdf);
        slideGenController.generateSlideThumbNail(slideGenController, savePresentationToPdf);
        if(savePresentationToPdf) slideGenController.savePresentationToPdf();
    }

    public void generateSlideThumbNail(ThumbnailGenerationManager slideGenController, boolean savePresentationToPdf) {
        Presentation presentation = slideGenController.presentationElement;

        //Check if thumbnail already there
        if(!savePresentationToPdf) {
            thumbnailFile = new File(PRESENTATIONS_PATH + this.presentationElement.getDocumentID() + "/Thumbnails/" + "slide" + (slideGenController.currentSlideNumber) + "_thumbnail.png");
        }else{
            thumbnailFile = new File(PRESENTATIONS_PATH + this.presentationElement.getDocumentID() + "/Print/" + "slide" + (slideGenController.currentSlideNumber) + "_thumbnail.png");

        }
        if (!thumbnailFile.exists()) {
            thumbnailFile.getParentFile().mkdirs(); //Create directory structure if not present yet
        } else {
            logger.debug("Thumbnail at " + thumbnailFile.getAbsolutePath() + " already exists");
            slideGenController.close(); //TODO: This causes thumbGen to close even if all thumbs are missing apart from the first one
            if (slideGenController.slideAdvance(presentation, Slide.SLIDE_FORWARD) == Presentation.PRESENTATION_FINISH) {
                logger.info("Done generating thumbnails for presentation " + presentation.getDocumentID());
                slideGenController.close();
            } else {
                generateSlideThumbNail(slideGenController, savePresentationToPdf);
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
            Slide slideToRender = presentation.getSlide(slideGenController.currentSlideNumber);
            WritableImage thumbnail = slideToRender.snapshot(new SnapshotParameters(), new WritableImage((int) slideToRender.getWidth(), (int) slideToRender.getHeight()));
            try {
                //Write the snapshot to the chosen file
                ImageIO.write(SwingFXUtils.fromFXImage(thumbnail, null), "png", thumbnailFile);
                //Advance to next slide, and generate next Slide Thumbnail
                if (slideGenController.slideAdvance(presentation, Slide.SLIDE_FORWARD) == Presentation.PRESENTATION_FINISH) {
                    logger.info("Done generating thumbnails for presentation " + presentation.getDocumentID());
                    slideGenController.close();
                } else {
                    generateSlideThumbNail(slideGenController, savePresentationToPdf);
                }
            } catch (IOException ex) {
                logger.error("Generating presentation thumbnail for " + presentation.getDocumentID() + " at " + thumbnailFile.getAbsolutePath() + " failed");
            }
        });
    }

    private void savePresentationToPdf() {
        PDDocument doc = new PDDocument();

        String[] ext = {"png"};
        File path = new File(PRESENTATIONS_PATH + this.presentationElement.getDocumentID() + "/Print/");
        File pathPDF = new File(PRESENTATIONS_PATH + this.presentationElement.getDocumentID() + "/Print/" + "/pdf/");
        if (!pathPDF.exists()) {
            pathPDF.mkdir();
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.setInitialFileName(presentationElement.getDocumentID());
        Stage testStage = new Stage();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File pdfPath = fileChooser.showSaveDialog(testStage);


        if(pdfPath == null){
            logger.info("Invalid path entered! PDF Not Generated");
        }
        if (pdfPath != null) {
            if (pdfPath.exists()) {
                pdfPath.delete();
            }
            logger.info("PDF Save path: " + pdfPath.getAbsolutePath());
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
                        page.setRotation(90);
                        doc.addPage(page);
                        PDImageXObject imageObject = PDImageXObject.createFromFile(f.getAbsolutePath(), doc);
                        PDPageContentStream contents = new PDPageContentStream(doc, page);
                        PDRectangle cropBox = page.getCropBox();
                        float tx = ((cropBox.getLowerLeftX() + cropBox.getUpperRightX()) / 2);
                        float ty = ((cropBox.getLowerLeftY() + cropBox.getUpperRightY()) / 2);
                        contents.transform(Matrix.getTranslateInstance(tx, ty));
                        contents.transform(Matrix.getRotateInstance(Math.toRadians(90), 0, 0));
                        contents.transform(Matrix.getTranslateInstance(-tx, -ty));
                        contents.drawImage(imageObject, -115, 135, PDRectangle.A4.getHeight() - 30, PDRectangle.A4.getWidth() - 30);
                        contents.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    //doc.save(PRESENTATIONS_PATH + this.presentationElement.getDocumentID() + "/Print/"+"/pdf/"+"output.pdf");
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