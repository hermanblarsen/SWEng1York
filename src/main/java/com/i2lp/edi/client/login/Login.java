package com.i2lp.edi.client.login;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
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
        loginStage.setTitle("I2LP");

        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        Scene scene = new Scene(gridPane, 350, 275);
        scene.getStylesheets().add("bootstrapfx.css");
        //setUserAgentStylesheet(STYLESHEET_MODENA); //TODO remove? -herman
        loginStage.setScene(scene);

        populateGUI();
        addActionEvents();
        loginStage.show();

        //TODO: Mode switching code depending on online/offline functionality
        if (!offline) {
            serverConnect();
        } else {
            //TODO do something while offline!
        }
    }

    private void populateGUI() {
        Text sceneTitle = new Text("Login");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        gridPane.add(sceneTitle, 0, 0, 2, 1);

        Label username = new Label("User Name:");
        gridPane.add(username, 0, 2);

        usernameField = new TextField();
        usernameField.setText("Teacher");
        usernameField.setOnAction(event -> login(loginStage));
        gridPane.add(usernameField, 1, 2, 2, 1);

        Label password = new Label("Password:");
        gridPane.add(password, 0, 3);

        passwordField = new PasswordField();
        passwordField.setText("password");
        passwordField.setOnAction(event -> login(loginStage));
        gridPane.add(passwordField, 1, 3, 2, 1);

        forgotPasswordButton = new Button("Forgot password?");
        forgotPasswordButton.getStyleClass().setAll("btn");
        gridPane.add(forgotPasswordButton, 1, 4, 2, 1);

        loginButton = new Button("Login");
        loginButton.getStyleClass().setAll("btn", "btn-danger");
        loginButton.setDefaultButton(true); //ties enter button to Login button
        gridPane.add(loginButton, 1, 5, 1, 1);
    }

    private void addActionEvents() {
        loginButton.setOnAction((ActionEvent event) -> {
            //TODO: Store this userauth object instead of keeping anonymous.
            String userType = this.verifyLogin(new UserAuth(usernameField.getCharacters().toString(),
                                                            passwordField.getCharacters().toString()));

        //Run different dashboards based on user type returned from DB
        boolean isTeacher = false;
        loginSuccessful = false;

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
                //If login is successfull, notify ediManager to close login stage and open dashboard.
                loginSuccessful = true;
                try {
                    this.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn("Closing of Login Dialog unsuccessful!");
                }
                finally {
                    ediManager.loginSucceded(isTeacher); //Do this after shutting down the login window.
                }
            } else {
                logger.info("Login unsuccessful");
                //TODO add colour events and stuff here to notify user of unsuccessful com.i2lp.edi.client.login.
            }
        });
    }

    public void serverConnect() {
        if (!offline) {
            //Connect to com.i2lp.edi.server
            mySocketClient = new SocketClient("db.amriksadhra.com", 8080);
            ediManager.setClient(mySocketClient);
        }
        else {
            //TODO figure out what to do when offline
        }
    }

    public String verifyLogin(UserAuth userToAuth) {
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