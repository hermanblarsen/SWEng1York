package com.i2lp.edi.client.login;

import com.i2lp.edi.client.Constants;
import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.server.SocketClient;
import com.i2lp.edi.server.packets.User;
import com.i2lp.edi.server.packets.UserAuth;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.i2lp.edi.client.Constants.BASE_PATH;
import static com.i2lp.edi.client.Constants.remoteServerAddress;


public class Login extends Application {
    protected Logger logger = LoggerFactory.getLogger(Login.class);

    private Stage loginStage;
    private GridPane gridPane;
    protected EdiManager ediManager;
    protected SocketClient mySocketClient;
    protected TextField usernameField;
    protected PasswordField passwordField;
    protected Button loginButton;
    protected Button forgotPasswordButton;

    protected boolean loginSuccessful = false;
    protected boolean offline = false;

    @Override
    public void start(Stage loginStage) {
        this.loginStage = loginStage;
        loginStage.setTitle("Edi Login Dialog");
        Image ediLogoSmall = new Image("file:projectResources/logos/ediLogo32x32.png");
        loginStage.getIcons().add(ediLogoSmall);

        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER_RIGHT);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        ArrayList<BackgroundImage> backgroundImageList = new ArrayList<>();
        Image ediLogo = new Image("file:projectResources/logos/ediLogo400x400.png");

        backgroundImageList.add(new BackgroundImage(ediLogo,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                new BackgroundPosition(Side.LEFT, -0.05, true,
                        Side.TOP, 0.5, true),
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO,
                        false, false,
                        true, false)));
        ArrayList<BackgroundFill> backgroundFillList = new ArrayList<>();
        backgroundFillList.add(new BackgroundFill(Color.GHOSTWHITE, CornerRadii.EMPTY, Insets.EMPTY));
        gridPane.setBackground(new Background(backgroundFillList, backgroundImageList));

        Scene scene = new Scene(gridPane, 550, 275); //TODO originally: 350, 275

        //sets the stylesheet to https://github.com/aalmiray/bootstrapfx, giving us access to various premade CSS styles.
        //More available on https://docs.oracle.com/javase/8/javafx/api/javafx/scene/doc-files/cssref.html
        //IMprove looks: https://docs.oracle.com/javase/8/javafx/visual-effects-tutorial/effect-types.htm
        //Another styling alternative, java internal: setUserAgentStylesheet(STYLESHEET_MODENA);
        scene.getStylesheets().add("bootstrapfx.css");
        loginStage.setScene(scene);
        loginStage.setResizable(false); //Do after setting scene

        //Print useful development information to terminal
        logger.info("EDI Client " + Constants.BUILD_STRING);
        logger.info("Scratch Directory: " + BASE_PATH);

        populateGUI();
        loginStage.show();

        //TODO: Mode switching code depending on online/offline functionality
        if (!offline) {
            serverConnect();
        } else {
            //TODO do something while offline!
        }
    }

    private void populateGUI() {
        DropShadow titleDropShadow = new DropShadow();
        titleDropShadow.setRadius(5.0);
        titleDropShadow.setOffsetX(3.0);
        titleDropShadow.setOffsetY(3.0);
        titleDropShadow.setColor(Color.AZURE);

        Text sceneTitle = new Text("Login");
        sceneTitle.getStyleClass().setAll("h1", "text-primary");
        sceneTitle.setEffect(titleDropShadow);
        gridPane.add(sceneTitle, 2, 0, 3, 1);

        Background labelBackground = new Background(new BackgroundFill(Color.LIGHTGRAY,
               new CornerRadii(3, false), new Insets(-6,-5,-6,-115)));

        usernameField = new TextField();
        usernameField.setText("Teacher");
        usernameField.setBackground(labelBackground);
        gridPane.add(usernameField, 2, 2, 3, 1);

        passwordField = new PasswordField();
        passwordField.setText("password");
        passwordField.setBackground(labelBackground);
        gridPane.add(passwordField, 2, 3, 3, 1);

        Label username = new Label("User Name:");
        username.getStyleClass().setAll("h4");
//        username.setBackground(labelBackground);
        gridPane.add(username, 0, 2, 2, 1);

        Label password = new Label("Password:");
        password.getStyleClass().setAll("h4");
//        password.setBackground(labelBackground);
        gridPane.add(password, 0, 3, 2, 1);

        forgotPasswordButton = new Button("Forgot password?");
        forgotPasswordButton.getStyleClass().setAll("btn");
        gridPane.add(forgotPasswordButton, 1, 4, 2, 1);

        loginButton = new Button("Login");
        loginButton.getStyleClass().setAll("btn", "btn-primary");
        loginButton.setDefaultButton(true); //ties enter button to Login button

        loginButton.setOnAction((ActionEvent event) -> {
            if (!offline) login();
            else ;//TODO decide what to do if offline
        });
        gridPane.add(loginButton, 2, 5, 2, 1);
    }

    private void login() {
        User userData;

        if(Constants.developerOffline){
           userData = new User(0, "Development", "User", "ediDev@i2lp.com","User", Constants.DEVELOPMENT_MODE);
           logger.info("Bypassing Server");
        } else {
            userData = this.verifyLogin(new UserAuth(usernameField.getCharacters().toString(), passwordField.getCharacters().toString()));
        }

        //Run different dashboards based on user type returned from DB
        boolean isTeacher = false;
        loginSuccessful = false;

        switch (userData.getUserType()) {
            case "admin":
                loginSuccessful = true;
                break;
            case "teacher":
                isTeacher = true;
                loginSuccessful = true;
                break;
            case "student":
                loginSuccessful = true;
                break;
            case "auth_fail":
                loginSuccessful = false;
                break;
        }

        if (loginSuccessful) {
            //If login is successful, notify ediManager to close login stage and open dashboard.
            loginSuccessful = true;
            try {
                this.stop();
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn("Closing of Login Dialog unsuccessful!");
            }
            finally {
                ediManager.loginSucceded(isTeacher, userData); //Do this after shutting down the login window.
            }
        } else {
            logger.info("Login unsuccessful");
            //TODO add colour events and stuff here to notify user of unsuccessful login.
        }
    }


    public void serverConnect() {
        if (!offline) {
            //Connect to com.i2lp.edi.server
            mySocketClient = new SocketClient(remoteServerAddress, 8080);
            ediManager.setClient(mySocketClient);
        }
        else {
            //TODO figure out what to do when offline
        }
    }

    public User verifyLogin(UserAuth userToAuth) {
        return mySocketClient.userAuth(userToAuth);
    }

    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    @Override
    public void stop() {
        logger.info("Login Stage Closed");
        loginStage.close();
        if (!loginSuccessful) {
            try {
                ediManager.stop();
                logger.info("EdiManager Closed");
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn("Exception when closing EdiManager");
            }
        }
    }
}