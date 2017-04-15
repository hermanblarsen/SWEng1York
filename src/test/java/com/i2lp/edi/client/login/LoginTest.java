package com.i2lp.edi.client.login;

import com.i2lp.edi.client.managers.EdiManager;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Parent;


import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;


import java.util.concurrent.TimeoutException;

import org.junit.*;
import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.verifyThat;

/**
 * Created by habl on 15/04/2017.
 * Project: SWEng1York - Package: com.i2lp.edi.client.login
 */

public class LoginTest extends ApplicationTest{ //With TestFX environment

//–—public class LoginTest extends Application{ //With JavaFXThreadingRule environment

//@RunWith( JavaFXTestRunner.class ) //https://gist.github.com/flasheater/a2703bfce0ca842d0e38#file-jfxtestrunner-java-L72
//public class LoginTest{

    //Test class for JavaFX GUI elements TODO remove this text when tested
    /*
    *
    * INfo on fxml: https://blogs.oracle.com/jmxetc/entry/connecting_scenebuilder_edited_fxml_to
    *
    * */

    //Elements on the GUI:
    final String LOGIN = "Login";

    private TextField userNameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Parent moduleUnderTest;
    private EdiManager ediManager;
    private static Login myLogin;
    private static Pane pane;


    /*
    @Rule public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();
    @BeforeClass
    public static void initialiseJavaFX() {
        Thread javaFxThread = new Thread("JavaFX Init Thread") {
            @Override
            public void run() {
                Login.main(new String[0]);
            }
        };
        javaFxThread.setDaemon(true);
        javaFxThread.start();

        try {
            Thread.sleep(15000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/


    // With JavaFXThreadingRule ... https://gist.github.com/andytill/3835914#file-javafxthreadingrule-java
    /* //@Rule
    //public JavaFXThreadingRule javafxRule = new JavaFXThreadingRule();
    @BeforeClass
    public static void initialiseJavaFX() {
        Thread javaFxThread = new Thread("JavaFX Init Thread") {
            @Override
            public void run() {
                Application.launch(LoginTest.class, new String[0]);
            }
        };
        javaFxThread.setDaemon(true);
        javaFxThread.start();
    }
    @Override
    public void start(Stage stage) throws Exception {
    }*/

    //With testFX environment
    //This operation comes from ApplicationTest and loads the GUI to test.
    @Override
    public void start(Stage stage) throws Exception {
        ediManager = new EdiManager();
        myLogin = new Login();
        myLogin.setEdiManager(ediManager);
        myLogin.start(stage);
        // Put the GUI in front of windows, else the robots may interact with undesirable windows
        stage.toFront();
    }

    // A shortcut to retrieve widgets in the GUI.
    public <T extends Node> T find(final String query) {
        // TestFX provides many operations to retrieve elements from the loaded GUI.
        return lookup(query).query();
    }


    @Before
    public void setUp() {
        userNameField = find("usernameField");
//        userNameField = find(".usernameField");
//        userNameField = find("#usernameField");
    }

    @Test //(expected = FxRobotException.class)
    public void testTest() {
        clickOn("usernameField").write("123");
        assertEquals(123, find("usernameField").getAccessibleText());
    }

    /*
    @Test
    public void serverConnect() {

    }

    @Test
    public void verifyLogin() {
    }

    @Test
    public void setEdiManager() {

    }
    */

    @After
    public void tearDown()  {
        try {
            FxToolkit.hideStage();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

}