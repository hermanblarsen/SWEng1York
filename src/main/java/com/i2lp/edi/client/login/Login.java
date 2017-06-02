package com.i2lp.edi.client.login;

import com.i2lp.edi.client.Constants;
import com.i2lp.edi.client.dashboard.StudentDashboard;
import com.i2lp.edi.client.dashboard.TeacherDashboard;
import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.managers.PresentationManagerStudent;
import com.i2lp.edi.client.managers.PresentationManagerTeacher;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.utilities.ParserXML;
import com.i2lp.edi.server.SocketClient;
import com.i2lp.edi.server.packets.User;
import com.i2lp.edi.server.packets.UserAuth;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;

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
    private Label messageLabel;
    private ImageView loadingImage;
    private HBox rootBox;

    protected boolean attemptingLogin = false;
    protected boolean loginSuccessful = false;

    protected boolean offline = false;


    @Override
    public void start(Stage loginStage) {
        this.loginStage = loginStage;
        loginStage.setTitle("Edi Login Dialog");
        Image ediLogoSmall = new Image("file:projectResources/logos/ediLogo32x32.png");
        loginStage.getIcons().add(ediLogoSmall);

        rootBox = new HBox(5);
        rootBox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(rootBox, 550, 275);

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

        populateGuiPreConnection();
        loginStage.show();

        //TODO: Mode switching code depending on online/offline functionality
        if (!offline) {
            serverConnect();
        } else {
            //TODO do something while offline!
        }
    }

    private void populateGuiPreConnection() {
        Image ediLogo = new Image("file:projectResources/logos/ediLogo400x400.png", 275, 275, true, true);
        ImageView logoImageView = new ImageView(ediLogo);
        rootBox.getChildren().add(logoImageView);

        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));
        rootBox.getChildren().add(gridPane);

        DropShadow titleDropShadow = new DropShadow();
        titleDropShadow.setRadius(5.0);
        titleDropShadow.setOffsetX(3.0);
        titleDropShadow.setOffsetY(3.0);
        titleDropShadow.setColor(Color.AZURE);

        Text sceneTitle = new Text("Login");
        sceneTitle.getStyleClass().setAll("h1", "text-primary");
        sceneTitle.setEffect(titleDropShadow);
        gridPane.add(sceneTitle, 0, 0, 2, 1);
        GridPane.setHalignment(sceneTitle, HPos.CENTER);

        usernameField = new TextField();
        usernameField.setText("Teacher");
        gridPane.add(usernameField, 1, 2);
        GridPane.setHalignment(usernameField, HPos.RIGHT);

        passwordField = new PasswordField();
        passwordField.setText("password");
        gridPane.add(passwordField, 1, 3);
        GridPane.setHalignment(passwordField, HPos.RIGHT);

        Label username = new Label("User Name:");
        username.getStyleClass().setAll("h4");
        username.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        gridPane.add(username, 0, 2);
        GridPane.setHalignment(username, HPos.RIGHT);

        Label password = new Label("Password:");
        password.getStyleClass().setAll("h4");
        password.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        gridPane.add(password, 0, 3);
        GridPane.setHalignment(password, HPos.RIGHT);

        forgotPasswordButton = new Button("Forgot password?");
        forgotPasswordButton.getStyleClass().setAll("btn");
        gridPane.add(forgotPasswordButton, 0, 4, 2, 1);
        GridPane.setHalignment(forgotPasswordButton, HPos.CENTER);

        loginButton = new Button("Open in offline mode");
        loginButton.setAlignment(Pos.CENTER);
        loginButton.getStyleClass().setAll("btn", "btn-primary");
        loginButton.setDefaultButton(true); //ties enter button to Login button
        loginButton.disableProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(attemptingLogin);
        });
        loginButton.setOnAction((ActionEvent event) -> openPresInOfflineMode());
        gridPane.add(loginButton, 0, 5, 2, 1);

        GridPane.setHalignment(loginButton, HPos.CENTER);

        loadingImage = new ImageView(new Image("file:projectResources/preloaders/loader.gif"));
        gridPane.add(loadingImage, 0, 6, 2, 1);
        GridPane.setHalignment(loadingImage, HPos.CENTER);

        messageLabel = new Label("Connecting to server...");
        gridPane.add(messageLabel, 0, 7, 2, 1);
        GridPane.setHalignment(messageLabel, HPos.CENTER);
    }

    public void changeGuiPostConnection() {
        loginButton.setText("Login");
        loginButton.setOnAction((ActionEvent event) -> {
            changeGuiLoggingIn();
            if (!offline) login();
            else ;//TODO decide what to do if offline
        });
        gridPane.getChildren().remove(loadingImage);

        messageLabel.setText("");
    }

    private void changeGuiLoggingIn() {
        if (!gridPane.getChildren().contains(loadingImage)) {
            gridPane.add(loadingImage, 0, 6, 2, 1);
        }
        messageLabel.setText("Logging in...");
    }

    private void changeGuiLoginFailed() {
        gridPane.getChildren().remove(loadingImage);
        messageLabel.setText("Login unsuccessful");
    }

    private void login() {
        attemptingLogin = true;
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
            case "noresponse":
                //TODO try again to connect
                break;
            default:
                logger.warn("User type: " + userData.getUserType() + " not recognised.");
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
                ediManager.loginSucceeded(isTeacher, userData); //Do this after shutting down the login window.
            }
        } else {
            changeGuiLoginFailed();
            logger.info("Login unsuccessful");
            attemptingLogin = false;
            //TODO add colour events and stuff here to notify user of unsuccessful login.
        }
    }
    
    public void serverConnect() {
        if (!offline) {
            //Connect to edi server
            mySocketClient = new SocketClient(remoteServerAddress, 8080);
            mySocketClient.setEdiManager(ediManager);
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

    private void openPresInOfflineMode() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter xmlExtensionFilter =
                new FileChooser.ExtensionFilter("XML Presentations (*.XML)", "*.xml", "*.XML");
        fileChooser.getExtensionFilters().add(xmlExtensionFilter);
        fileChooser.setSelectedExtensionFilter(xmlExtensionFilter);
        fileChooser.setInitialDirectory(new File("projectResources/sampleFiles/xml"));
        //fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))); //TODO reinstate when tested
        fileChooser.setTitle("Open Presentation");

        File file = fileChooser.showOpenDialog(loginStage);
        if (file != null) {
            ParserXML parserXML = null;
            try {
                parserXML = new ParserXML(file.getPath());
            } catch (FileNotFoundException e) {
                logger.error("XML file not found: " + file.getPath());
            }
            Presentation presentation = parserXML.parsePresentation();

            PresentationManagerStudent presentationManager = new PresentationManagerStudent(ediManager);

            ediManager.setPresentationManager(presentationManager);
            presentationManager.openPresentation(presentation, false);
        } else logger.info("No presentation was selected");
    }
}