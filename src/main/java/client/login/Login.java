package client.login;

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
import client.managers.EdiManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.packets.UserAuth;
import server.socketClient;
import server.socketServer;


public class Login extends Application {
    private EdiManager ediManager;
    private Logger logger = LoggerFactory.getLogger(Login.class);
    socketClient mySocketClient;

    //----------- IF YOU'RE NOT ON THE DATABASE TEAM, SET THIS VARIABLE TO FALSE TO BYPASS THE SERVER STUFF -----------------
    private static final boolean AM_I_ON_DB_TEAM = false;


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
        primaryStage.setScene(scene);

        Text scenetitle = new Text("Login");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label userType = new Label("I'm a:");
        grid.add(userType, 0, 1);

        ToggleButton studentButton = new ToggleButton("Student");
        studentButton.getStyleClass().setAll("btn", "btn-default");
        grid.add(studentButton, 1, 1);

        ToggleButton teacherButton = new ToggleButton("Teacher");
        teacherButton.getStyleClass().setAll("btn", "btn-default");
        grid.add(teacherButton, 2, 1);

        ToggleGroup userTypeGroup = new ToggleGroup();
        studentButton.setToggleGroup(userTypeGroup);
        teacherButton.setToggleGroup(userTypeGroup);

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
        loginButton.getStyleClass().setAll("btn","btn-danger");
        grid.add(loginButton, 1, 5, 1, 1);
        loginButton.setOnAction((ActionEvent event) ->{
            //TODO: Store this userauth object instead of keeping anonymous.
            boolean loginSuccessful = this.verifyLogin(new UserAuth(userTextField.getCharacters().toString(), passwordField.getCharacters().toString()));

            //Run different dashboards based on userType button
            boolean isTeacher = false;

            if (teacherButton.isSelected()) isTeacher = true;

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
                //TODO add colour events and stuff here to notify user of unsuccessful client.login.
            }
        });

        primaryStage.show();
    }

    public void serverConnect(){
        if(AM_I_ON_DB_TEAM) {
            //TODO: For now, start server here. Should be compiled to separate JAR and run independently
            socketServer mySocketServer = new socketServer("db.amriksadhra.com", 8080);
            //Connect to server
            mySocketClient = new socketClient("127.0.0.1", 8080);

        }
    }

    public boolean verifyLogin (UserAuth userToAuth) {
        if(AM_I_ON_DB_TEAM){
            return mySocketClient.userAuth(userToAuth);
        } else return true;
    }

    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }
}