package com.i2lp.edi.client.login;

import com.i2lp.edi.client.managers.EdiManager;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;


import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;


import java.util.concurrent.TimeoutException;

import org.junit.*;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.*;

/**
 * Created by habl on 15/04/2017.
 * Project: SWEng1York - Package: com.i2lp.edi.client.login
// */
public class LoginTest extends ApplicationTest{

    //Elements to be tested on the GUI:
    private TextField userNameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button forgotPasswordButton;


    private EdiManager ediManager;
    private static Login myLogin;

    //This operation comes from ApplicationTest and loads the GUI to test.
    @Override
    public void start(Stage stage) throws Exception {
        if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }
        ediManager = new EdiManager();
        //myLogin.setEdiManager(ediManager);
        ediManager.start(stage);
        // Put the GUI in front of windows, else the robots may interact with undesirable windows
        stage.toFront();
    }

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);

        myLogin = ediManager.getLoginDialog();
        userNameField = myLogin.usernameField;
        passwordField = myLogin.passwordField;
        forgotPasswordButton = myLogin.forgotPasswordButton;
        loginButton = myLogin.loginButton;
    }

    @Test
    public void testLogin() {
        doubleClickOn(userNameField).write("Teacher");
        doubleClickOn(passwordField).write("password");
        sleep(2000);

        clickOn(loginButton);
        sleep(2000);
        assertTrue(ediManager.isLoggedIn());
    }

    @Test
    public void testUsername() {
        doubleClickOn(userNameField).write("123");
        assertEquals("123", userNameField.getText());
    }

    @Test
    public void testPassword() {
        doubleClickOn(passwordField).write("123");
        assertEquals("123", passwordField.getText());
    }


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