package com.i2lp.edi.client.editor;

import com.i2lp.edi.client.managers.PresentationManager;
import com.i2lp.edi.client.utilities.Status;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Kacper on 2017-04-13.
 */
//Depending on how we want the editor to work, it might be useful for this to extend PresentationManager.
//For now, this is only a quiz editor
public class PresentationEditor {
    Logger logger = LoggerFactory.getLogger(PresentationManager.class);

    private Scene scene;
    private String presentationPath;
    private BorderPane border;
    private ScrollPane scroll;
    protected VBox vbox;
    private Stage stage;

    private Text statusText;

    protected MenuBar menuBar;
    protected HBox statusBar;

    public PresentationEditor(String path) {
        presentationPath = path;

        stage = new Stage();
        stage.setTitle("Presentation Editor");

        vbox = new VBox();
        scroll = new ScrollPane();
        scroll.setContent(vbox);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);

        border = new BorderPane();
        border.setTop(addMenuBar());
        border.setCenter(scroll);
        border.setBottom(addStatusBar());

        scene = new Scene(border, 700, 600);
        scene.getStylesheets().add("bootstrapfx.css");
        stage.setScene(scene);
        stage.show();

    }

    private MenuBar addMenuBar() {
        menuBar = new MenuBar();

        //Menu fileMenu = new Menu("File");
        //Menu editMenu = new Menu("Edit");
        Menu addMenu = new Menu("Add");
        Menu addInteractiveElementMenu = new Menu("Interactive element");
        MenuItem addPollMenuItem = new MenuItem("Poll");
        addPollMenuItem.setOnAction(event -> addPoll());
        addInteractiveElementMenu.getItems().add(addPollMenuItem);
        addMenu.getItems().add(addInteractiveElementMenu);

        menuBar.getMenus().add(addMenu);

        return menuBar;
    }

    private HBox addStatusBar() {
        statusText = new Text();
        statusText.setFill(Color.WHITESMOKE);
        statusText.setFont(Font.font(15));

        HBox statusBar = new HBox();
        statusBar.setStyle("-fx-background-color: #34495e;");
        statusBar.setPadding(new Insets(5, 12, 5, 12));
        statusBar.setSpacing(5);
        statusBar.setAlignment(Pos.BASELINE_RIGHT);
        statusBar.getChildren().add(statusText);

        return statusBar;
    }

    public HBox getStatusBar() {
        return statusBar;
    }

    public Text getStatusText() {
        return statusText;
    }

    private void updateStatusBar(Status status) {
        String statusString = new String();

        if(!status.getxName().isEmpty())
            statusString = status.getxName() + ": " + status.getxValue();
        if(!status.getyName().isEmpty())
            statusString += " | " + status.getyName() + ": " + status.getyValue();
        if(!status.getzName().isEmpty())
            statusString += " | " + status.getzName() + ": " + status.getzValue();

        statusText.setText(statusString);
    }

    private void addPoll() {
        PollEditorPanel pePanel = new PollEditorPanel(vbox);

        EventHandler eventHandler = event -> {
            Status status = new Status(
                    "Number of Answers", Integer.toString(pePanel.getAnswerNumber()),
                    "", "",
                    "","");
            updateStatusBar(status);
        };
        pePanel.addEventHandler(MouseEvent.MOUSE_MOVED, eventHandler);

        vbox.getChildren().add(pePanel);
    }
}
