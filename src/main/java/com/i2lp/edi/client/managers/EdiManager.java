package com.i2lp.edi.client.managers;


import com.i2lp.edi.client.dashboard.Dashboard;
import com.i2lp.edi.client.dashboard.PresDownloadLoadingScreen;
import com.i2lp.edi.client.dashboard.StudentDashboard;
import com.i2lp.edi.client.dashboard.TeacherDashboard;
import com.i2lp.edi.client.login.Login;
import com.i2lp.edi.server.SocketClient;
import com.i2lp.edi.server.packets.User;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by habl on 11/03/2017.
 */
public class EdiManager extends Application {
    Logger logger = LoggerFactory.getLogger(EdiManager.class);


    protected Login loginDialog;

    private PresentationManager presentationManager;
    private PresentationLibraryManager presentationLibraryManager;
    private Dashboard dashboard;
    protected SocketClient socketClient;
    protected User userData; //Store currently logged in users data
    private PresDownloadLoadingScreen loadingScreen;
    private boolean offline = false;
    private boolean loggedIn = false;

    public static void main(String[] args) {
        //Instantiate the ediManager, which will automatically call init() and start(Stage)
        launch(args);
    }

    //Temporary, so that edimanager can close the ports and prevent port-in-use errors on next execution
    public void setClient(SocketClient mySocketClient) {
        this.socketClient = mySocketClient;
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
        //TODO put recurrent connections tries on timer? -Herman
        verifyInternetAccess();

        loginDialog = new Login();
        Stage loginStage = new Stage();
        loginDialog.setEdiManager(this);
        loginDialog.setOffline(offline);
        loginDialog.start(loginStage);
    }

    private void verifyInternetAccess() {
        Socket testSocket = new Socket();
        InetSocketAddress address = new InetSocketAddress("google.com", 80);
        try {
            testSocket.connect(address, 2000);
        } catch (IOException e) {
            logger.info("You are not connected to the WWW. Offline Mode activated");
            offline = true;
        } finally {
            try {
                if (!testSocket.isClosed()) testSocket.close();
            } catch (IOException e) {
                logger.warn("IOException when closing socket for internet access check");
            }
        }
    }

    /**
     * Custom logic when user is logged in should go here. Currently displaying First name of user.
     *
     * @param userToSet User data received from Edi server
     * @author Amrik Sadhra
     */
    public void setUserData(User userToSet) {
        this.userData = userToSet;
        logger.info("Welcome " + userData.getFirstName() + " " + userData.getSecondName() + "!");
    }

    //This is called from loginWindow when the user has input valid credentials
    public void loginSucceeded(boolean isTeacher, User userData) {
        logger.debug("Login succeeded");
        loggedIn = true;

        loadingScreen = new PresDownloadLoadingScreen(  );
        Stage downloadStage = new Stage();
        //downloadStage.initOwner(dashboardStage); //TODO: this could prevent a symbol appearing on the taskbar
        loadingScreen.start(downloadStage);

        setUserData(userData); //User data is now available throughout Edi
        this.presentationLibraryManager = new PresentationLibraryManager(this);

        Stage dashboardStage = new Stage();

        //Additional com.i2lp.edi.client.login stuff
        if (isTeacher) {
            logger.info("Teacher Dashboard Opened");
            dashboard = new TeacherDashboard();
            dashboard.setEdiManager(this);
            dashboard.start(dashboardStage);
        } else {
            logger.info("Student Dashboard Opened");
            dashboard = new StudentDashboard();
            dashboard.setEdiManager(this);
            dashboard.start(dashboardStage);
        }
    }

    //Closing down Edi; shutting down sockets
    @Override
    public void stop() {
        //Shut down any live sessions
        if (presentationManager != null){
            presentationManager.close();
        }
        logger.info("Closing client-side networking ports.");
        if (socketClient != null) {
            socketClient.closeAll();
        }
    }

    public void setPresentationManager(PresentationManager presentationManager) {
        this.presentationManager = presentationManager;
    }

    public Login getLoginDialog() {
        return loginDialog;
    }


    public PresentationManager getPresentationManager() {
        return this.presentationManager;
    }

    public PresentationLibraryManager getPresentationLibraryManager() {
        return presentationLibraryManager;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public SocketClient getSocketClient() {
        return this.socketClient;
    }

    public User getUserData() {
        return userData;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public PresDownloadLoadingScreen getLoadingScreen() { return loadingScreen; }
}
