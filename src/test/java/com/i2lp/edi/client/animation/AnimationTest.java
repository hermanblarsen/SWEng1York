package com.i2lp.edi.client.animation;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.*;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.TimeoutException;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by Luke on 03/06/2017.
 */
public class AnimationTest extends ApplicationTest {
    private TranslationAnimation myTranslationAnimation;
    private ScaleAnimation myScaleAnimation;
    private OpacityAnimation myOpacityAnimation;
    private PathAnimation myPathAnimation;
    private Boolean isFinished = false;

    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }

        // Setup the animations to test
        myTranslationAnimation = new TranslationAnimation(100,100,300,300, 1000);
        myTranslationAnimation.setScaleFactor(1,1);

        myScaleAnimation = new ScaleAnimation(0, 1, 1000);

        myOpacityAnimation = new OpacityAnimation( 0, 1, 1000);

        myPathAnimation = new PathAnimation("M100 100, s-500 1200 500 800, c-500 -10 170 -800 170 -800 Z", 3000);
        myPathAnimation.setScaleFactor(1,1);

        // Setup test node
        Rectangle testRect = new Rectangle(50, 50);
        testRect.setTranslateX(300);
        testRect.setTranslateY(300);

        // Bind the animations to the test node
        myTranslationAnimation.setCoreNodeToAnimate(testRect);
        myScaleAnimation.setCoreNodeToAnimate(testRect);
        myOpacityAnimation.setCoreNodeToAnimate(testRect);
        myPathAnimation.setCoreNodeToAnimate(testRect);

        BorderPane root = new BorderPane();
        root.getChildren().add(testRect);
        Scene scene = new Scene(root, 600, 600);

        stage.setTitle("Simple Animations Test");
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);
    }

    @Test
    public void testTranslationCreation() {
        assertEquals(100, myTranslationAnimation.getStartX(),0);
        myTranslationAnimation.setStartX(200);
        assertEquals(200, myTranslationAnimation.getStartX(),0);

        assertEquals(100, myTranslationAnimation.getStartY(),0);
        myTranslationAnimation.setStartY(200);
        assertEquals(200, myTranslationAnimation.getStartY(),0);

        assertEquals(300, myTranslationAnimation.getEndX(),0);
        myTranslationAnimation.setEndX(500);
        assertEquals(500, myTranslationAnimation.getEndX(),0);

        assertEquals(300, myTranslationAnimation.getEndY(),0);
        myTranslationAnimation.setEndY(500);
        assertEquals(500, myTranslationAnimation.getEndY(),0);

        assertEquals(1000, myTranslationAnimation.getDuration().toMillis(),0);
        myTranslationAnimation.setDuration(Duration.millis(2000));
        assertEquals(2000, myTranslationAnimation.getDuration().toMillis(),0);
    }

    @Test
    public void testScaleCreation() {
        assertEquals(0, myScaleAnimation.getStartScale(), 0);
        myScaleAnimation.setStartScale(1);
        assertEquals(1, myScaleAnimation.getStartScale(),0);

        assertEquals(1, myScaleAnimation.getEndScale(), 0);
        myScaleAnimation.setEndScale(2);
        assertEquals(2, myScaleAnimation.getEndScale(),0);

        assertEquals(1000, myScaleAnimation.getDuration().toMillis(),0);
        myScaleAnimation.setDuration(Duration.millis(2000));
        assertEquals(2000, myScaleAnimation.getDuration().toMillis(),0);
    }

    @Test
    public void testOpacityCreation() {
        assertEquals(0, myOpacityAnimation.getStartOpacity(), 0);
        myOpacityAnimation.setStartOpacity(1);
        assertEquals(1, myOpacityAnimation.getStartOpacity(),0);

        assertEquals(1, myOpacityAnimation.getEndOpacity(), 0);
        myOpacityAnimation.setEndOpacity(2);
        assertEquals(2, myOpacityAnimation.getEndOpacity(),0);

        assertEquals(1000, myOpacityAnimation.getDuration().toMillis(),0);
        myOpacityAnimation.setDuration(Duration.millis(2000));
        assertEquals(2000, myOpacityAnimation.getDuration().toMillis(),0);
    }

    @Test
    public void testPathCreation() {
        assertEquals("M100 100, s-500 1200 500 800, c-500 -10 170 -800 170 -800 Z", myPathAnimation.getPathUnscaled().getContent());
        myPathAnimation.setPath("M200 200, s-500 1200 500 800, c-500 -10 170 -800 170 -800 Z");
        assertEquals("M200 200, s-500 1200 500 800, c-500 -10 170 -800 170 -800 Z", myPathAnimation.getPathUnscaled().getContent());

        assertEquals(3000, myPathAnimation.getDuration().toMillis(),0);
        myPathAnimation.setDuration(Duration.millis(4000));
        assertEquals(4000, myPathAnimation.getDuration().toMillis(),0);
    }

    @Test
    public void testTranslationAnimation() {
        isFinished = false;
        myTranslationAnimation.play();
        myTranslationAnimation.setOnFinish(event -> setFinished(true));

        sleep(50);

        assertFalse(isFinished);
        assertEquals(myTranslationAnimation.getStartX(), myTranslationAnimation.getCoreNodeToAnimate().getTranslateX(), 10);
        assertEquals(myTranslationAnimation.getStartY(), myTranslationAnimation.getCoreNodeToAnimate().getTranslateY(), 10);

        sleep((long) (myTranslationAnimation.getDuration().toMillis() + 100));

        assertTrue(isFinished);
        assertEquals(myTranslationAnimation.getEndX(), myTranslationAnimation.getCoreNodeToAnimate().getTranslateX(), 1);
        assertEquals(myTranslationAnimation.getEndY(), myTranslationAnimation.getCoreNodeToAnimate().getTranslateY(), 1);
    }

    @Test
    public void testScaleAnimation() {
        isFinished = false;
        myScaleAnimation.play();
        myScaleAnimation.setOnFinish(event -> setFinished(true));

        sleep(50);

        assertFalse(isFinished);
        assertEquals(myScaleAnimation.getStartScale(), myScaleAnimation.getCoreNodeToAnimate().getScaleX(), 0.01);
        assertEquals(myScaleAnimation.getStartScale(), myScaleAnimation.getCoreNodeToAnimate().getScaleY(), 0.01);

        sleep((long) (myScaleAnimation.getDuration().toMillis() + 100));

        assertTrue(isFinished);
        assertEquals(myScaleAnimation.getEndScale(), myScaleAnimation.getCoreNodeToAnimate().getScaleX(), 0.01);
        assertEquals(myScaleAnimation.getEndScale(), myScaleAnimation.getCoreNodeToAnimate().getScaleY(), 0.01);
    }

    @Test
    public void testOpacityAnimation() {
        isFinished = false;
        myOpacityAnimation.play();
        myOpacityAnimation.setOnFinish(event -> setFinished(true));

        sleep(50);

        assertFalse(isFinished);
        assertEquals(myOpacityAnimation.getStartOpacity(), myScaleAnimation.getCoreNodeToAnimate().getOpacity(), 0.01);

        sleep((long) (myOpacityAnimation.getDuration().toMillis() + 100));

        assertTrue(isFinished);
        assertEquals(myOpacityAnimation.getEndOpacity(), myScaleAnimation.getCoreNodeToAnimate().getOpacity(), 0.01);
    }

    @Ignore
    @Test
    public void testPathAnimation() {
        isFinished = false;
        myPathAnimation.play();
        myPathAnimation.setOnFinish(event -> setFinished(true));

        sleep(50);

        assertFalse(isFinished);
        //TODO @Luke find way to test path animation

        sleep((long) (myPathAnimation.getDuration().toMillis() + 100));

        assertTrue(isFinished);

    }

    public void setFinished(Boolean isFinished) {
        this.isFinished = isFinished;
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
