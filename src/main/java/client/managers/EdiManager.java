package client.managers;


import client.dashboard.StudentDashboard;
import client.dashboard.TeacherDashboard;
import client.login.Login;


import javafx.application.Application;
import javafx.stage.Stage;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.SocketClient;

/**
 * Created by habl on 11/03/2017.
 */
public class EdiManager extends Application {
    Logger logger = LoggerFactory.getLogger(EdiManager.class);
    private Login loginDialog;
    private SocketClient mySocketClient;
    private boolean isDBTeam;

    public static void main(String [] args) {
        //Instantiate the ediManager, which will automatically call init() and start(Stage)
        launch(args);
    }

    //Temporary, so that edimanager can close the ports and prevent port-in-use errors on next execution
    public void setClient(SocketClient mySocketClient, boolean isDBTeam){
        this.mySocketClient = mySocketClient;
        this.isDBTeam = isDBTeam;
    }

    //Initialising Edi, possibly gathering information about the system and storing that locally
    //No stages or scenes in this method. Called by launch()
    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override //Called by launch()
    public void start(Stage primaryStage) throws Exception {
        loginDialog = new Login();
        Stage loginStage = new Stage();
        loginDialog.setEdiManager(this);
        loginDialog.start(loginStage);
    }

    //This is called from login when the user has input valid credentials
    public void loginSucceded(boolean isTeacher) {
        logger.info("Login succeeded");
        Stage dashboardStage = new Stage();

        //Additional client.login stuff
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
    public void stop() throws Exception {
        if(isDBTeam) {
            logger.info("Closing client-side networking ports.");
            mySocketClient.closeAll();
        }
    }
}
