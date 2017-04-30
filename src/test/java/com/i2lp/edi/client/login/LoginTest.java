package com.i2lp.edi.client.login;

import com.i2lp.edi.client.managers.EdiManager;

import com.sun.javafx.robot.FXRobot;
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
// */
@Ignore
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
        ediManager = new EdiManager();
        myLogin = new Login();
        myLogin.setEdiManager(ediManager);
        myLogin.start(stage);
        // Put the GUI in front of windows, else the robots may interact with undesirable windows
        stage.toFront();
    }

    @Before
    public void setUp() {
        userNameField = myLogin.usernameField;
        passwordField = myLogin.passwordField;
        forgotPasswordButton = myLogin.forgotPasswordButton;
        loginButton = myLogin.loginButton;
    }

    @Test
    public void usernameTest() {
        doubleClickOn(userNameField).write("123");
        assertEquals("123", userNameField.getText());
    }

    @Test
    public void passwordTest() {
        doubleClickOn(passwordField).write("123");
        assertEquals("123", passwordField.getText());
    }

    @Test
    public void loginTest() {
        doubleClickOn(userNameField).write("Teacher");
        doubleClickOn(passwordField).write("password");

        clickOn(loginButton);
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