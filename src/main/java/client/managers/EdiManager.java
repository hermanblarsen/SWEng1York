package client.managers;


import client.dashboard.StudentDashboard;
import client.dashboard.TeacherDashboard;
import client.login.Login;


import javafx.application.Application;
import javafx.stage.Stage;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by habl on 11/03/2017.
 */
public class EdiManager extends Application {
    Logger logger = LoggerFactory.getLogger(EdiManager.class);
    private Login loginDialog;

    public static void main(String [] args) {
        //Instantiate the ediManager, which will automatically call init() and start(Stage)
        launch(args);
    }

    //Initialising Edi, possibly gathering information about the system and storing that locally
    //No stages or scenes in this method.
    @Override
    public void init() throws Exception {
        super.init();
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        //TODO make a client.login dialog, then wait for calling of loginSucceded(boolean isTeacher);
        loginDialog = new Login();
        Stage loginStage = new Stage();
        loginDialog.setEdiManager(this);
        loginDialog.start(loginStage);
    }


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
}
