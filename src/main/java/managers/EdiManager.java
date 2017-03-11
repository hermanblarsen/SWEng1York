package managers;


import dashboard.StudentDashboard;
import dashboard.TeacherDashboard;
import login.Login;
import dashboard.Dashboard;


import javafx.application.Application;
import javafx.stage.Stage;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by habl on 11/03/2017.
 */
public class EdiManager extends Application {
    //TODO overall manager of program, to make login and open appropriate dashboard when login is accepted
    private boolean applicationRunning;
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
        logger.info("Edi is initialising");
        this.applicationRunning  = true;
        super.init();
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        //TODO make a login dialog, then wait for calling of loginSucceded(boolean isTeacher);
        loginDialog = new Login();
        Stage loginStage = new Stage();
        loginDialog.setEdiManager(this);
        loginDialog.start(loginStage);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public boolean isApplicationRunning() {
        return applicationRunning;
    }

    public void setApplicationRunning(boolean applicationRunning) {
        this.applicationRunning = applicationRunning;
    }


    public void loginSucceded(boolean isTeacher) {
        logger.info("Login succeeded");
        Stage dashboardStage = new Stage();

        //Additional login stuff
        if (isTeacher) {
            TeacherDashboard teacherDashboard = new TeacherDashboard();
            teacherDashboard.setEdiManager(this);
            teacherDashboard.start(dashboardStage);
        } else {
            StudentDashboard studentDashboard = new StudentDashboard();
            studentDashboard.setEdiManager(this);
            studentDashboard.start(dashboardStage);
        }
    }
}
