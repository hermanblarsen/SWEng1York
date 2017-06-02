package com.i2lp.edi.client.presentationViewerElements;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.*;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.TimeoutException;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by Luke on 31/05/2017.
 */
public class DrawPaneTest extends ApplicationTest {
    private DrawPane myDrawPane;

    private Color brushColor;
    private Double brushWidth;
    private Boolean isEraserMode;
    private Double eraserSize;
    private WritableImage slideDrawing;

    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

        StackPane stackPane = new StackPane();
        myDrawPane = new DrawPane(stackPane);
        Scene scene = new Scene(stackPane,600,600);

        stage.setTitle("Draw Pane Test");
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);

        isEraserMode = myDrawPane.isEraserMode();
    }

    @Test
    public void testCreation() {
        assertTrue(myDrawPane.graphicsContext.getCanvas().isVisible());
        assertFalse(isEraserMode);
    }

    @Test
    public void testBrushEdit() {
        myDrawPane.setBrushColor(Color.BLUE);
        assertEquals(Color.BLUE, myDrawPane.getBrushColor());

        myDrawPane.setBrushWidth(5.0);
        assertEquals(5.0, myDrawPane.getBrushWidth(), 0);
    }

    @Test
    public void testEraserEdit() {
        myDrawPane.setEraserMode(true);
        assertTrue(myDrawPane.isEraserMode());

        myDrawPane.setEraserSize(5.0);
        assertEquals(5.0, myDrawPane.getEraserSize(), 0);
    }

    @Test
    public void testSaveDrawing() {
        WritableImage newDrawing = new WritableImage((int) myDrawPane.canvas.getWidth(), (int) myDrawPane.canvas.getHeight());
        SnapshotParameters newSnapshotParameters = new SnapshotParameters();
        newSnapshotParameters.setFill(Color.TRANSPARENT);
        Canvas newCanvas = new Canvas(myDrawPane.canvas.getWidth(), myDrawPane.canvas.getHeight());
        newCanvas.getGraphicsContext2D().fillOval(100,100,100,100);
        Platform.runLater(new Runnable() {
            @Override public void run() {
                newCanvas.snapshot(newSnapshotParameters, newDrawing);
            }
        });
        sleep(500);

        myDrawPane.setSlideDrawing(newDrawing);

        assertEquals(newDrawing, myDrawPane.getSlideDrawing());
    }

    @Ignore //TODO @Luke
    @Test
    public void testRedraw() {
        WritableImage newDrawing = new WritableImage((int) myDrawPane.canvas.getWidth(), (int) myDrawPane.canvas.getHeight());
        SnapshotParameters newSnapshotParameters = new SnapshotParameters();
        newSnapshotParameters.setFill(Color.TRANSPARENT);
        Canvas newCanvas = new Canvas(myDrawPane.canvas.getWidth(), myDrawPane.canvas.getHeight());
        newCanvas.getGraphicsContext2D().fillOval(100,100,100,100);
        Platform.runLater(new Runnable() {
            @Override public void run() {
                newCanvas.snapshot(newSnapshotParameters, newDrawing);
            }
        });
        sleep(500);

        WritableImage blankDrawing = new WritableImage((int) myDrawPane.canvas.getWidth(), (int) myDrawPane.canvas.getHeight());
        SnapshotParameters blankSnapshotParameters = new SnapshotParameters();
        blankSnapshotParameters.setFill(Color.TRANSPARENT);
        Canvas blankCanvas = new Canvas(myDrawPane.canvas.getWidth(), myDrawPane.canvas.getHeight());
        blankCanvas.getGraphicsContext2D().clearRect(0, 0, blankCanvas.getWidth(), blankCanvas.getHeight());
        Platform.runLater(new Runnable() {
            @Override public void run() {
                blankCanvas.snapshot(blankSnapshotParameters, blankDrawing);
            }
        });
        sleep(500);

        myDrawPane.setSlideDrawing(newDrawing);

        assertEquals(newDrawing, myDrawPane.getSlideDrawing());

        Platform.runLater(new Runnable() {
            @Override public void run() {
                myDrawPane.clear();
            }
        });
        sleep(500);

        assertEquals(blankDrawing, myDrawPane.getSlideDrawing());
    }

    @After
    public void tearDown() {
        try {
            FxToolkit.hideStage();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }
}
