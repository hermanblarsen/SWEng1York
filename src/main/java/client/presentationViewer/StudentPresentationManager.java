package client.presentationViewer;

import client.managers.PresentationManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;


/**
 * Created by kma517 on 16/03/2017.
 */
public class StudentPresentationManager extends PresentationManager {
    protected Boolean elementClicked = false;
    protected HTMLEditor he = new HTMLEditor();

    @Override
    protected void questionQueueFunction() {
        Stage questionQueueStage = new Stage();
        questionQueueStage.setTitle("Send a Question");
        BorderPane border = new BorderPane();
        border.setStyle("-fx-background-color: #34495e");
        scene = new Scene(border, 450, 450);
        scene.getStylesheets().add("bootstrapfx.css");
        Label title = new Label("Send a question to the question queue");
        border.setPadding(new Insets(0,10,10,10));
        title.setPadding(new Insets(10,0,10,0));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        border.setTop(title);


        TextArea ta = new TextArea();
        ta.setMaxWidth(Double.MAX_VALUE);
        ta.setMaxHeight(Double.MAX_VALUE);
        ta.setWrapText(true);
        border.setCenter(ta);

        Image tick = new Image("file:projectResources/Tick.png",30,30,true,true);
        ImageView tickPanel = new ImageView(tick);
        tickPanel.addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
            questionQueueStage.close();
        });

        Image cross = new Image("file:projectResources/cancel.png",30,30,true,true);
        ImageView crossPanel = new ImageView(cross);
        crossPanel.addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
            questionQueueStage.close();
        });

        HBox controlBox = new HBox();
        controlBox.setMaxWidth(Double.MAX_VALUE);
        controlBox.getChildren().addAll(tickPanel,crossPanel);
        border.setBottom(controlBox);
        questionQueueStage.setScene(scene);
        questionQueueStage.show();
    }

}

