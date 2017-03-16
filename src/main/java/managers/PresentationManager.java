package managers;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import utilities.*;

import java.util.ArrayList;

/**
 * Created by kma517 on 16/03/2017.
 */
public abstract class PresentationManager extends Application {
    protected Scene scene;
    protected BorderPane border;
    protected Presentation myPresentationElement;
    protected ProgressBar pb;
    protected Label slideNumber;
    protected Boolean isFullscreen = false;
    protected Boolean buttonsRemoved = false;
    protected Boolean questionQueueActive = false;



    @Override
    public void start(Stage primaryStage){
        primaryStage.setTitle("Edi");

        border = new BorderPane();
        scene = new Scene(border, 1000, 600);
        scene.getStylesheets().add("bootstrapfx.css");
        primaryStage.setScene(scene);
        primaryStage.show();
        loadPresentation(border, "shit");
        pb = new ProgressBar(0);
        slideNumber = new Label("Slide 1 of "+myPresentationElement.getSlideList().size());
        border.setBottom(addPresentationControls(primaryStage));
    }

    protected void loadPresentation(BorderPane mainUI, String path) {
        ParserXML readPresentationParser = new ParserXML(path);

        myPresentationElement = Presentation.generateTestPresentation();     //TEST
        //       myPresentationElement = readPresentationParser.parsePresentation();

        mainUI.setCenter(myPresentationElement.getCurrentSlide());
        //mainUI.setBottom(addStatBar(myPresentationElement.getCurrentSlide()));

//        for (Slide currentSlide : myPresentationElement.getSlideList()) {
//            for (SlideElement element : currentSlide.getSlideElementList()) {
//                element.setSlideCanvas(currentSlide);
//            }
//        }


        //Keyboard listener for moving through presentation
        scene.setOnKeyPressed(key -> {

            if (key.getCode().equals(KeyCode.RIGHT)) {
                controlPresentation(Slide.SLIDE_FORWARD);
                slideProgress(myPresentationElement);
            } else if (key.getCode().equals(KeyCode.LEFT)) {
                controlPresentation(Slide.SLIDE_BACKWARD);
                slideProgress(myPresentationElement);
            }
        });


    }

    public abstract HBox addPresentationControls(Stage primaryStage);

    protected void slideProgress(Presentation pe){
        double slideNo = pe.getCurrentSlideNumber()+1;
        double slideMax = pe.getSlideList().size();
        double progress  = slideNo/slideMax;
        pb.setProgress(progress);
        slideNumber.setText("Slide "+(int) slideNo+" of "+(int) slideMax);
    }

    protected void controlPresentation(int direction) {
        int presentationStatus = myPresentationElement.advance(direction);
        // If Presentation handler told us that slide is changing, update the Slide present on Main screen
        // Can do specific things when presentation reached end, or start.
        if ((presentationStatus == Presentation.SLIDE_CHANGE) || (presentationStatus == Presentation.PRESENTATION_FINISH) || (presentationStatus == Presentation.PRESENTATION_START)) {
            //logger.info("Changing Slides");
            System.out.println("Changing Slides");

            // Update MainUI panes when changing slides to account for new Slide root pane.
            border.setCenter(myPresentationElement.getCurrentSlide());
            // border.setBottom(addStatBar(myPresentationElement.getCurrentSlide()));
        }


    }


}
