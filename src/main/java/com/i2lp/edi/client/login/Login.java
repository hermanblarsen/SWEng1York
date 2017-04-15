package com.i2lp.edi.client.login;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.i2lp.edi.client.managers.EdiManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.i2lp.edi.server.packets.UserAuth;
import com.i2lp.edi.server.SocketClient;


public class Login extends Application {
    private EdiManager ediManager;
    private Logger logger = LoggerFactory.getLogger(Login.class);
    SocketClient mySocketClient;

    //----------- IF YOU'RE NOT ON THE DATABASE TEAM, SET THIS VARIABLE TO FALSE TO BYPASS THE SERVER STUFF -----------------
    private static final boolean AM_I_ON_DB_TEAM = true;


    @Override
    public void start(Stage primaryStage) {
        //TODO: Mode switching code depending on online/offline functionality
        serverConnect();
        primaryStage.setTitle("I^2LP");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Scene scene = new Scene(grid, 350, 275);
        scene.getStylesheets().add("bootstrapfx.css");
        //setUserAgentStylesheet(STYLESHEET_MODENA);
        primaryStage.setScene(scene);

        Text scenetitle = new Text("Login");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label userName = new Label("User Name:");
        grid.add(userName, 0, 2);

        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 2, 2, 1);

        Label password = new Label("Password:");
        grid.add(password, 0, 3);

        PasswordField passwordField = new PasswordField();
        grid.add(passwordField, 1, 3, 2, 1);

        Button forgotPassword = new Button("Forgot password?");
        forgotPassword.getStyleClass().setAll("btn");
        grid.add(forgotPassword, 1, 4, 2, 1);

        //SQL Authentication wanted here
        Button loginButton = new Button("Login");
        loginButton.getStyleClass().setAll("btn", "btn-danger");
        grid.add(loginButton, 1, 5, 1, 1);
        loginButton.setOnAction((ActionEvent event) -> {
            //TODO: Store this userauth object instead of keeping anonymous.
            String userType = this.verifyLogin(new UserAuth(userTextField.getCharacters().toString(), passwordField.getCharacters().toString()));

            //Run different dashboards based on user type returned from DB
            boolean isTeacher = false;
            boolean loginSuccessful = false;

            switch (userType) {
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
                ediManager.loginSucceded(isTeacher);
                try {
                    this.stop();
                    primaryStage.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Closing of Login Dialog unsuccessful!");
                }
            } else {
                logger.info("Login unsuccessful");
                //TODO add colour events and stuff here to notify user of unsuccessful com.i2lp.edi.client.login.
            }
        });
        primaryStage.show();
    }

    public void serverConnect() {
        if (AM_I_ON_DB_TEAM) {
            //Connect to com.i2lp.edi.server
            mySocketClient = new SocketClient("db.amriksadhra.com", 8080);
            ediManager.setClient(mySocketClient, AM_I_ON_DB_TEAM);
        }
    }

    public String verifyLogin(UserAuth userToAuth) {
        if (AM_I_ON_DB_TEAM) {
            return mySocketClient.userAuth(userToAuth);
        } else return "teacher";
    }

    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }
}