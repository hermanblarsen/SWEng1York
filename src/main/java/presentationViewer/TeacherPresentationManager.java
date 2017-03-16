package presentationViewer;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import managers.PresentationManager;
import utilities.Slide;

/**
 * Created by kma517 on 16/03/2017.
 */
public class TeacherPresentationManager extends PresentationManager {
    public static void main(String[] args){launch(args);}
    public TeacherPresentationManager() {

    }

    @Override
    public HBox addPresentationControls(Stage primaryStage) {
        HBox presControls = new HBox();
        presControls.setStyle("-fx-background-color: #34495e;");
        //presControls.setMinHeight(10);
        presControls.setPadding(new Insets(5, 12, 5, 12));
        presControls.setSpacing(5);
        Image next = new Image("file:externalResources/Right_NEW.png",30,30,true,true);
        ImageView nextButton = new ImageView(next);
        nextButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                controlPresentation(Slide.SLIDE_FORWARD);
                slideProgress(myPresentationElement);

            }
        });

        Image back = new Image("file:externalResources/Left_NEW.png",30,30,true,true);
        ImageView backButton = new ImageView(back);
        backButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                controlPresentation(Slide.SLIDE_BACKWARD);
                slideProgress(myPresentationElement);

            }
        });

        Image fullScreen = new Image("file:externalResources/Fullscreen_NEW.png", 30,30,true,true);

        ImageView fullScreenButton = new ImageView(fullScreen);

        fullScreenButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                if(isFullscreen == false) {
                    primaryStage.setFullScreen(true);
                    isFullscreen = true;
                }
                else{
                    primaryStage.setFullScreen(false);
                    isFullscreen = false;
                }

            }

        });

//        Image questionBubble = new Image("file:externalResources/SB_Filled.png",30,30,true,true);
//        ImageView questionQ = new ImageView(questionBubble);
//        questionQ.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                if(questionQueueActive == false) {
//                    addQuestionQ(true);
//                    questionQueueActive = true;
//                }else{
//                    addQuestionQ(false);
//                    questionQueueActive = false;
//                }
//            }//       });


        StackPane sp = new StackPane();
        pb.setMinSize(200,10);
        sp.getChildren().addAll(pb,slideNumber);

        presControls.addEventHandler(MouseEvent.MOUSE_ENTERED, evt ->{
            if(buttonsRemoved == false) {
                presControls.getChildren().addAll(backButton, nextButton, fullScreenButton,sp);
                buttonsRemoved = true;
            }

            FadeTransition ft0 = new FadeTransition(Duration.millis(300),presControls);
            ft0.setFromValue(0.0);
            ft0.setToValue(1.0);
            ft0.play();
            FadeTransition ft = new FadeTransition(Duration.millis(300),backButton);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
            FadeTransition ft2 = new FadeTransition(Duration.millis(300),nextButton);
            ft2.setFromValue(0.0);
            ft2.setToValue(1.0);
            ft2.play();
            FadeTransition ft3 = new FadeTransition(Duration.millis(300),fullScreenButton);
            ft3.setFromValue(0.0);
            ft3.setToValue(1.0);
            ft3.play();
            FadeTransition ft4 = new FadeTransition(Duration.millis(300),sp);
            ft4.setFromValue(0.0);
            ft4.setToValue(1.0);
            ft4.play();


        });
        presControls.addEventHandler(MouseEvent.MOUSE_EXITED, evt->{
            FadeTransition ft0 = new FadeTransition(Duration.millis(300),presControls);
            ft0.setFromValue(1.0);
            ft0.setToValue(0.0);
            ft0.play();
            FadeTransition ft = new FadeTransition(Duration.millis(300),backButton);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.play();
            FadeTransition ft2 = new FadeTransition(Duration.millis(300),nextButton);
            ft2.setFromValue(1.0);
            ft2.setToValue(0.0);
            ft2.play();
            FadeTransition ft3 = new FadeTransition(Duration.millis(300),fullScreenButton);
            ft3.setFromValue(1.0);
            ft3.setToValue(0.0);
            ft3.play();
            FadeTransition ft4 = new FadeTransition(Duration.millis(300),sp);
            ft4.setFromValue(1.0);
            ft4.setToValue(0.0);
            ft4.play();


            ft4.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if(buttonsRemoved == true) {
                        presControls.getChildren().removeAll(backButton, nextButton, fullScreenButton, sp);
                        buttonsRemoved = false;
                    }
                }
            });

        });
        return presControls;
    }
}
