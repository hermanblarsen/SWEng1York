package client.presentationViewer;
import client.managers.PresentationManager;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.scene.layout.Panel;


/**
 * Created by kma517 on 16/03/2017.
 */
public class TeacherPresentationManager extends PresentationManager {



    @Override
    protected void questionQueueFunction() {
        Stage questionQueueStage = new Stage();
        questionQueueStage.setTitle("Question Queue");
        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: #34495e");
        scene = new Scene(bp, 450, 450);
        scene.getStylesheets().add("bootstrapfx.css");
        Label title = new Label("Question Queue");
        bp.setPadding(new Insets(0,10,10,10));
        title.setPadding(new Insets(10,0,10,0));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        bp.setTop(title);

        ScrollPane sp = new ScrollPane();
        sp.setMaxWidth(Double.MAX_VALUE);
        FlowPane fp = new FlowPane();
        fp.setPrefWrapLength(100);
        fp.setMaxWidth(Double.MAX_VALUE);
        fp.setPadding(new Insets(5,0,5,0));
        fp.setVgap(4);
        fp.setHgap(4);
        fp.setStyle("-fx-background-color: whitesmoke");

        int numTestCases = 10;

        Panel[] slides = new Panel[numTestCases];

        for(int i = 0;i< numTestCases;i++){
            slides[i] = new Panel("This is test question: "+i);
            slides[i].getStyleClass().add("panel-primary");
            slides[i].setMinWidth(400);
            fp.getChildren().add(slides[i]);
            Label lab = new Label(slides[i].getText());
            slides[i].addEventHandler(MouseEvent.MOUSE_CLICKED,evt->{
                    border.setCenter(lab);
            });
        }
        sp.setContent(fp);
        bp.setCenter(sp);
        questionQueueStage.setScene(scene);
        questionQueueStage.show();
    }





}
