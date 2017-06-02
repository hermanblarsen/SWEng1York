package com.i2lp.edi.client.dashboard;

import javafx.animation.FadeTransition;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Kacper on 2017-05-26.
 */
public class SubjectPanel extends PreviewPanel {

    private static final double SCROLL_BUTTON_SIZE = 15;
    private static final long SCROLL_RATE = 20;
    private static final double SCROLL_INCREMENT_PX = 15;
    private static final double SPACING = 5;
    private final Subject subject;
    private ArrayList<ModulePanel> modulePanels;
    private HBox modulePanelsHBox;
    private Text numOfModules;
    private StackPane leftStackPane, rightStackPane;
    private BorderPane arrowOverlay;
    private ScrollPane centerScroll;
    private StackPane rootPane;
    private Timer scrollTimer;

    public SubjectPanel(Subject subject, Pane parentPane) {
        super(parentPane, false);
        this.subject = subject;
        modulePanelsHBox = new HBox(SPACING);
        modulePanels = new ArrayList<>();

        Text title = new Text(subject.getSubjectName());
        title.getStyleClass().setAll("h4");
        BorderPane.setMargin(title, new Insets(5));

        numOfModules = new Text();
        numOfModules.getStyleClass().setAll("h5", "italics");
        numOfModules.setFill(Color.GRAY);

        HBox textHBox = new HBox(2);
        textHBox.setAlignment(Pos.CENTER_LEFT);
        textHBox.getChildren().addAll(title, numOfModules);
        BorderPane.setMargin(textHBox, new Insets(5));

        Region backgroundRegion = new Region();
        backgroundRegion.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        StackPane stackPane = new StackPane(backgroundRegion, modulePanelsHBox);

        centerScroll = new ScrollPane();
        centerScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        centerScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        centerScroll.getStyleClass().add("edge-to-edge");
        centerScroll.setContent(stackPane);
        centerScroll.widthProperty().addListener((observable, oldValue, newValue) -> backgroundRegion.setPrefWidth(newValue.doubleValue()));
        centerScroll.heightProperty().addListener((observable, oldValue, newValue) -> backgroundRegion.setPrefHeight(newValue.doubleValue()));

        arrowOverlay = new BorderPane();
        arrowOverlay.setOpacity(0);
        arrowOverlay.setPickOnBounds(false);
        rootPane = new StackPane(centerScroll, arrowOverlay);

        ImageView leftButton = new ImageView(new Image("file:projectResources/icons/arrow-left.png", SCROLL_BUTTON_SIZE, SCROLL_BUTTON_SIZE, true, true));
        ImageView rightButton = new ImageView(new Image("file:projectResources/icons/arrow-right.png", SCROLL_BUTTON_SIZE, SCROLL_BUTTON_SIZE, true, true));
        leftStackPane = setupScrollButton(leftButton, -1);
        rightStackPane = setupScrollButton(rightButton, 1);
        arrowOverlay.setRight(rightStackPane);
        arrowOverlay.setLeft(leftStackPane);

        modulePanelsHBox.getChildren().addListener((ListChangeListener<? super Node>) observable -> {
            if(modulePanelsHBox.getChildren().size() == 0) {
                this.setHidden(true);
            } else {
                this.setHidden(false);
            }
        });

        this.addEventFilter(MouseEvent.ANY, event -> {
            if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {
                double scrollWidth = centerScroll.getWidth();
                double totalWidth = getTotalModulesWidth();
                if(scrollWidth != 0 && scrollWidth < totalWidth) {
                    FadeTransition ft0 = new FadeTransition(Duration.millis(500), arrowOverlay);
                    ft0.setFromValue(arrowOverlay.getOpacity());
                    ft0.setToValue(1.0);
                    ft0.play();
                }
            } else if (event.getEventType().equals(MouseEvent.MOUSE_EXITED)) {
                FadeTransition ft0 = new FadeTransition(Duration.millis(500), arrowOverlay);
                ft0.setFromValue(arrowOverlay.getOpacity());
                ft0.setToValue(0.0);
                ft0.play();
            }
        });

        getDisplayPanel().setTop(textHBox);
        getDisplayPanel().setCenter(rootPane);
    }

    private StackPane setupScrollButton(ImageView icon, int direction) {
        StackPane scrollButton = new StackPane(icon);
        scrollButton.setMaxWidth(SCROLL_BUTTON_SIZE);
        scrollButton.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(3), null)));
        scrollButton.addEventFilter(MouseEvent.ANY, event -> {
            if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
                double relativeIncrement = SCROLL_INCREMENT_PX/modulePanelsHBox.getWidth();
                scrollTimer = new Timer(true);
                scrollTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        centerScroll.setHvalue(centerScroll.getHvalue() + Math.signum(direction) * relativeIncrement);
                    }
                }, 0, SCROLL_RATE);
            } else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
                scrollTimer.cancel();
            }
            event.consume();
        });

        return scrollButton;
    }

    public Subject getSubject() { return subject; }

    public HBox getModulePanelsHBox() { return modulePanelsHBox; }

    @Override
    public ArrayList<String> getSearchableTerms() {
        ArrayList<String> searchableTerms = new ArrayList<>();
        searchableTerms.add(getSubject().getSubjectName());

        return searchableTerms;
    }

    public static SubjectPanel findInArray(String subjectName, ArrayList<SubjectPanel> arrayList) {
        for (SubjectPanel panel : arrayList) {
            if (panel.getSubject().getSubjectName().equals(subjectName)) {
                return panel;
            }
        }

        return null;
    }

    public void addModulePanel(ModulePanel panel) {
        modulePanels.add(panel);
        panel.setSubjectPanel(this);
        numOfModules.setText(" (" + modulePanels.size() + " modules)");
    }

    public ArrayList<ModulePanel> getModulePanels() { return modulePanels; }
    private double getTotalModulesWidth() {
        double width = 0;
        for (int i = 0; i < modulePanels.size(); i++) {
            width += ModulePanel.WIDTH;
            width += SPACING;
        }

        if(modulePanels.size() != 0) {
            width -= SPACING;
        }

        return width;
    }
}
