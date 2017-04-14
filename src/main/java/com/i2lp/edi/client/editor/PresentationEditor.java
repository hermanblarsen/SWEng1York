package com.i2lp.edi.client.editor;

import com.i2lp.edi.client.managers.PresentationManager;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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
    private VBox vbox;
    private Stage stage;

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

        scene = new Scene(border, 700, 600);
        scene.getStylesheets().add("bootstrapfx.css");
        stage.setScene(scene);
        stage.show();

    }

    private MenuBar addMenuBar() {
        MenuBar menuBar = new MenuBar();

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

    private void addPoll() {
        vbox.getChildren().add(new PollEditorPanel(vbox));
    }
}
