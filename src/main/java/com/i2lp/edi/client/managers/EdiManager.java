package com.i2lp.edi.client.managers;


import com.i2lp.edi.client.dashboard.StudentDashboard;
import com.i2lp.edi.client.dashboard.TeacherDashboard;
import com.i2lp.edi.client.login.Login;


import javafx.application.Application;
import javafx.stage.Stage;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.i2lp.edi.server.SocketClient;

import java.io.IOException;
import java.net.*;

/**
 * Created by habl on 11/03/2017.
 */
public class EdiManager extends Application {
    Logger logger = LoggerFactory.getLogger(EdiManager.class);
    private Login loginDialog;
    private SocketClient mySocketClient;
    private boolean offline = false;
    private boolean loginSuccessful;

    public static void main(String [] args) {
        //Instantiate the ediManager, which will automatically call init() and start(Stage)
        launch(args);
    }

    //Temporary, so that edimanager can close the ports and prevent port-in-use errors on next execution
    public void setClient(SocketClient mySocketClient){
        this.mySocketClient = mySocketClient;
    }

    //Initialising Edi, possibly gathering information about the system and storing that locally
    //No stages or scenes in this method. Called by launch()
    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override //Called by launch()
    public void start(Stage primaryStage) throws Exception {
        //Checking for internet connection: //TODO maybe put on a timer and/or in a thread if not connected? Or maybe just bad idea...
        verifyInternetAccess();

        loginDialog = new Login();
        Stage loginStage = new Stage();
        loginDialog.setEdiManager(this);
        loginDialog.setOffline(offline);
        loginDialog.start(loginStage);
    }

    private void verifyInternetAccess() {
        Socket testSocket = new Socket();
        InetSocketAddress address = new InetSocketAddress("google.com",80);
        try {
            testSocket.connect(address,2000);
        } catch (IOException e) {
            logger.info("Offline Mode activated");
            offline = true;
        } finally {
            try {
                if (!testSocket.isClosed()) testSocket.close();
                logger.info("Socket for internet access check closed");
            } catch (IOException e) {
                logger.warn("IOException when closing socket for internet access check");
            }
        }


    }

    //This is called from loginWindow when the user has input valid credentials
    public void loginSucceded(boolean isTeacher) {
        logger.info("Login succeeded");
        Stage dashboardStage = new Stage();

        //Additional com.i2lp.edi.client.login stuff
        if (isTeacher) {
            logger.info("Teacher Dashboard Opened");
            TeacherDashboard teacherDashboard = new TeacherDashboard();
            teacherDashboard.setEdiManager(this);
            teacherDashboard.start(dashboardStage);
        } else {
            logger.info("Student Dashboard Opened");
            StudentDashboard studentDashboard = new StudentDashboard();
            studentDashboard.setEdiManager(this);
            studentDashboard.start(dashboardStage);
        }
    }

    //Closing down Edi; shutting down sockets
    @Override
    public void stop() {
        logger.info("Closing client-side networking ports.");
        if (!offline) mySocketClient.closeAll();
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }
}
