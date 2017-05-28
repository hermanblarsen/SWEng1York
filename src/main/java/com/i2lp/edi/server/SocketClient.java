package com.i2lp.edi.server;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.utilities.Utilities;
import com.i2lp.edi.server.packets.*;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.jdbc.PGDataSource;
import io.socket.client.IO;
import io.socket.client.Socket;
import javafx.application.Platform;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.i2lp.edi.client.Constants.*;

/**
 * Created by amriksadhra on 20/03/2017.
 */
public class SocketClient {
    /* Constant to indicate server has not responded */
    private static final int NO_RESPONSE = -1;
    public static final int PASSWORD_INVALID = 0;
    //Timeout times for user addition/authorisation asynchronous functions
    private static final int LOGIN_TIMEOUT = 5;
    private static final int ADDITION_TIMEOUT = 5;

    private Logger logger = LoggerFactory.getLogger(SocketClient.class);

    //TODO: These will be filled by actual values, for now they are temp and meaningless
    private int current_presentation_id = 1;
    private int current_question_id = 1;

    //Network connections
    private PGDataSource dataSource;
    private Socket socket;
    //EdiManager
    private EdiManager ediManager;

    public static void main(String[] args) {
        //new SocketClient(remoteServerAddress, 8080);
    }

    public SocketClient(String serverIP, int serverPort) {
        connectToRemoteDB(remoteServerAddress);

        if (localServer) {
            connectToRemoteSocket(localServerAddress, 8080);
        } else {
            connectToRemoteSocket(serverIP, serverPort);
        }
    }

    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }

    public void connectToRemoteDB(String dbHostName) {
        //Connect to PostgreSQL Instance
        dataSource = new PGDataSource();
        dataSource.setHost(dbHostName);
        dataSource.setPort(5432);
        dataSource.setDatabase("edi");
        dataSource.setUser("iilp");
        dataSource.setPassword("group1SWENG");

        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            logger.info("Successful connection from client to PostgreSQL database instance");
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }
    }

    public void addResponse(int presentation_id, int question_id, String data) {
        //Attempt to add a user
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            StringBuilder sb = new StringBuilder();

            //TODO: Create Stored Procedure on PostgreSQL
            sb.append("INSERT INTO public.responses (presentation_id, question_id, data) VALUES ('");
            sb.append(presentation_id).append("', '");
            sb.append(question_id).append("', '");
            sb.append(data).append("');");

            logger.info("Adding response to database using SQL: " + sb.toString());
            statement.execute(sb.toString());
            //Let client know whether their operation was successful
            //TODO: Do what the comment above says
            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }
    }

    public void connectToRemoteSocket(String socketHostName, int serverPort) {
        String serverIPAddress = Utilities.buildIPAddress(socketHostName, serverPort);

        //Alert tester that connection is being attempted
        logger.info("Client: Attempting Connection to " + serverIPAddress);

        try {
            socket = IO.socket(serverIPAddress);
        } catch (URISyntaxException e) {
            logger.error("Couldn't create client port: May be in use by other program!");
        }

        socket.on(Socket.EVENT_CONNECT, args -> logger.info("Client successfully connected to Edi Server"));

        socket.on("DB_Update", args -> {
            logger.info("Client knows DB has updated:  " + args[0]);
            //Pull fresh table
            updateLocalTables(args[0]);
        }).on(Socket.EVENT_DISCONNECT, args -> {

        });

        //Attempt Socket connection
        socket.connect();
    }

    /**
     * The main controller for remote database updates. We can act appropriately based upon what has updated remotely.
     * e.g. Live responses to the current presentation can be used to update current graph object on slide.
     *
     * @param tableToUpdate Table that has been updated on Server
     * @author Amrik Sadhra
     */
    public void updateLocalTables(Object tableToUpdate) {
        //SocketIO will pass a generic object. But we know its a string because that's what DB_notify returns from com.i2lp.edi.server side
        switch ((String) tableToUpdate) {
            case "interactions":
                logger.info("New responses to act upon registered on edi server!");
                ArrayList<Interaction> Interactions = getInteractionsForInteractiveElement(1);
                break;

            case "interactive_elements":
                logger.info("New interactive elements present on edi server!");
                //updateResponses(current_presentation_id, current_question_id);
                break;

            case "users":
                if (ediManager.getUserData().getUserType().equals("teacher")) {//If we're a teacher
                    if (ediManager.getPresentationManager() != null) {//And in a presentation
                        if (ediManager.getPresentationManager().getPresentationElement().getServerSideDetails().getLive()) {//And that presentation is live
                            ediManager.getPresentationManager().getPresentationSession().setActiveUsers(getPresentationActiveUsers(ediManager.getPresentationManager().getPresentationElement().getServerSideDetails().getPresentationID()));//Update list of active users in that presentation
                        }
                    }
                }
                break;

            case "presentations":
                //Update presentation list whilst no presentation is live
                if (ediManager.getPresentationManager() == null) {
                    ediManager.getPresentationLibraryManager().updatePresentations(); //Update presentation information
                } else {
                    if (ediManager.getUserData().getUserType().equals("student")) {//If we're a student
                        if (ediManager.getPresentationManager().getPresentationElement().getServerSideDetails().getLive()) {//In a live presentation
                            int current_slide_number = getCurrentSlideForPresentation(ediManager.getPresentationManager().getPresentationElement().getServerSideDetails().getPresentationID());
                            //TODO: Enable undocking (unsync?) of Teacher/Student slide movement using UI toggle
                            if (ediManager.getPresentationManager().getCurrentSlideNumber() != current_slide_number) {
                                //If the current slide number has changed, move to it
                                logger.info("Slide change request from teacher received. Changing to target slide.");
                                Platform.runLater(() -> ediManager.getPresentationManager().goToSlide(current_slide_number));
                            }
                        }
                    }
                }
                break;

            case "jnct_users_modules":
                logger.info("Modules user is registered for may have changed!");
                ediManager.getPresentationLibraryManager().updatePresentations(); //Update presentation information
                break;


            case "modules":
                logger.info("Modules database changed!");
                //TODO: Update class list
                break;

            case "questions":
                if (ediManager.getUserData().getUserType().equals("teacher")) {//If we're a teacher
                    if (ediManager.getPresentationManager().getPresentationElement().getServerSideDetails().getLive()) {//In a live presentation
                        ediManager.getPresentationManager().getPresentationSession().setQuestionQueue(ediManager.getSocketClient().getQuestionsForPresentation(ediManager.getPresentationManager().getPresentationElement().getServerSideDetails().getPresentationID())); //Update the question queue in the session
                    }
                }
                break;
        }
    }

    /**
     * Calls userAuthAsync function but with a LOGIN_TIMEOUT second timeout. If we hit timeout, return empty user data. Else return valid user data.
     *
     * @param toAuth User details to authenticate
     * @return User containing user_type of authenticated user.
     * @author Amrik Sadhra
     */
    public User userAuth(UserAuth toAuth) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<User> future = executor.submit(new UserAuthTask(toAuth));

        try {
            logger.info("Attempting login of User: " + toAuth.getUserToLogin());
            return future.get(LOGIN_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            logger.error("Connection to server timed out."); //TODO if timed out, maybe look for possible fixes (assuming its the socket..?)
        } catch (InterruptedException e) {
            logger.error("Connection to server was interrupted.");
        } catch (ExecutionException e) {
            logger.error("Connection to server failed. (tragically)");
        }
        //If we hit any of the catch statements
        executor.shutdownNow();
        return new User(NO_RESPONSE, "", "", "", "", "noresponse");
    }

    /**
     * Calls userAddAsync function but with a ADDITION_TIMEOUT second timeout. If we hit timeout, return false, else wait for com.i2lp.edi.server
     * to respond with response.
     *
     * @param toAdd User details to add
     * @return Generated login name for users supplied details
     * @author Amrik Sadhra
     */
    public String userAdd(User toAdd) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new UserAddTask(toAdd));

        try {
            logger.info("Attempting add of User: " + toAdd.getFirstName() + " " + toAdd.getSecondName());
            return future.get(ADDITION_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            logger.error("Connection to com.i2lp.edi.server timed out.");
        } catch (InterruptedException e) {
            logger.error("Connection to com.i2lp.edi.server was interrupted.");
        } catch (ExecutionException e) {
            logger.error("Connection to com.i2lp.edi.server failed. (tragically)");
        }
        //If we hit any of the catch statements
        executor.shutdownNow();
        return "USER_ADD_FAILED";
    }

    class UserAuthTask implements Callable<User> {
        UserAuth toAuth;

        public UserAuthTask(UserAuth toAuth) {
            this.toAuth = toAuth;
        }

        @Override
        public User call() throws Exception {
            return userAuthAsync(toAuth);
        }
    }

    class UserAddTask implements Callable<String> {
        User toAdd;

        public UserAddTask(User toAdd) {
            this.toAdd = toAdd;
        }

        @Override
        public String call() throws Exception {
            return userAddAsync(toAdd);
        }
    }

    /**
     * This function generates a JSON object that is passed using socket.IO to the com.i2lp.edi.server. It allows us to authenticate
     * a user against the database to determine if they have the correct username/password.
     *
     * @param toAuth User to add to the current users database located serverside
     * @return User data for user if authenticated.
     * @author Amrik Sadhra
     */
    public User userAuthAsync(UserAuth toAuth) {
        //Ensure atomic write to variable, bypassing Lambda final restriction
        AtomicReference<User> loginSuccessFinal = new AtomicReference<>(new User(NO_RESPONSE, "", "", "", "", ""));

        JSONObject obj = new JSONObject();
        try {
            obj.put("userToLogin", toAuth.getUserToLogin());
            obj.put("password", toAuth.getPassword());
            socket.emit("AuthUser", obj);

            socket.on("AuthUser", objects -> {
                //If user id not equal to 0 (password invalid)
                if (!(objects[0]).equals(PASSWORD_INVALID)) {
                    logger.info("User " + toAuth.getUserToLogin() + " has successfully logged in");
                    logger.debug("Parsing user packet data");

                    //Parse user data
                    loginSuccessFinal.set(new User((Integer) objects[0], (String) objects[1], (String) objects[2], (String) objects[3], (String) objects[4]));
                } else {
                    logger.error("Incorrect username/password for login.");
                }
            });
        } catch (JSONException e) {
            logger.error("Unable to generate JSON object for passing user authentication details. ", e);
        }

        //Spinlock method until the server has responded, and changed the value of the success variable (user id not equal to 0)
        while (loginSuccessFinal.get().getUserID() == NO_RESPONSE) {
            logger.trace("JVM optimises out empty while loops, hence this.. Waiting for server response.");
        }

        //Return the UserData
        return loginSuccessFinal.get();
    }

    /**
     * This function generates a JSON object that is passed using socket.IO to the com.i2lp.edi.server. It allows us to add users to
     * the SQL Database
     *
     * @param toAdd User to add to the current users database located serverside
     * @author Amrik Sadhra
     */
    public String userAddAsync(User toAdd) {
        //Ensure atomic write to variable, bypassing Lambda final restriction
        AtomicReference<String> additionSuccessFinal = new AtomicReference<>("no_response");

        //When we send data as a custom class, we need to wrap it in JSON with fields named after the variables in our class
        JSONObject obj = new JSONObject();
        try {
            obj.put("firstName", toAdd.getFirstName());
            obj.put("secondName", toAdd.getSecondName());
            obj.put("emailAddress", toAdd.getEmailAddress());
            obj.put("password", toAdd.getPassword());
            obj.put("userType", toAdd.getUserType());

            //TODO: If failed, throw custom userAdd exception
            socket.emit("AddUser", obj);
            socket.on("AddUser", objects -> {
                if (!(objects[0]).equals("user_add_failed")) {
                    logger.info("User " + objects[0] + " was successfully added to database.");
                    additionSuccessFinal.set((String) objects[0]);
                } else {
                    logger.error("Error adding user to database");
                    additionSuccessFinal.set((String) objects[0]);
                }
            });
        } catch (JSONException e) {
            logger.error("Unable to generate JSON object for passing new user details. ", e);
        }

        //Spinlock method until the com.i2lp.edi.server has responded, and changed the value of the success variable
        while (additionSuccessFinal.get().equals("no_response")) {
            logger.trace("JVM optimises out empty while loops. Waiting for com.i2lp.edi.server response.");
        }

        //Return the login name generated com.i2lp.edi.server side
        return additionSuccessFinal.get();
    }

    public void closeAll() {
        try {
            dataSource.getConnection().close();
        } catch (SQLException e) {
            logger.info("Failed to close com.i2lp.edi.client connection to DB. Non-fatal, still terminating.");
        }
        socket.close();
    }

    public ArrayList<PresentationMetadata> getPresentationsForUser(int userID) {
        ArrayList<PresentationMetadata> presentationsForUser = new ArrayList<>();

        //Attempt to add a user using stored procedure
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM edi.public.sp_getpresentationsforuser(?);");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, userID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                presentationsForUser.add(new PresentationMetadata(rs.getInt("presentation_id"), rs.getInt("module_id"), rs.getInt("current_slide_number"), rs.getString("xml_url"), rs.getBoolean(("live"))));
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }
        return presentationsForUser;
    }

    public ArrayList<Module> getModulesForUser(int userID) {
        ArrayList<Module> modulesForUser = new ArrayList<>();

        //Attempt to add a user using stored procedure
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM edi.public.sp_getmodulesforuser(?);");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, userID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                modulesForUser.add(new Module(rs.getInt("module_id"), rs.getString("description"), rs.getString("subject"), rs.getTime("time_last_updated"), rs.getTimestamp("time_created"), rs.getString("module_name")));
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }
        return modulesForUser;
    }

    /**
     * Send packet to server Socket to alert it that a new presentation is available for
     * integration into the Edi database.
     */
    public void alertServerToUpload(String presentationName, int moduleID) {
        socket.emit("NewUpload", presentationName + " " + moduleID);
        socket.on("NewUploadStatus", objects -> {
            logger.info("Addition of " + presentationName + " presentation had the following status: " + objects[0]);
            if (((String) objects[0]).contains("Success")) {
                ediManager.getPresentationLibraryManager().updatePresentations(); //Go and download the presentation from the server
            }
        });
    }

    /**
     * Delete the requested presentation from the respective module, deleting the data base table reference and the server zip contents
     *
     * @param presentationID
     * @param moduleID
     * @return
     */
    public String removePresentationFromModule(int presentationID, int moduleID) { //TODO @Amrik make delete zip stuff. and maybe my logic
        String return_status_removal = "";
        String xml_path = "";
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statementXml = connection.prepareStatement("SELECT * FROM edi.public.sp_getpresentationsformodule(?);");
            //Fill prepared statements to avoid SQL injection
            statementXml.setInt(1, moduleID);

            //Call stored procedure on database
            ResultSet getXmlUrlResultSet = statementXml.executeQuery();

            while (getXmlUrlResultSet.next()) {
                int presentationIdTemp = getXmlUrlResultSet.getInt("presentation_id");
                if (presentationIdTemp == presentationID) xml_path = getXmlUrlResultSet.getString("xml_url");
            }
            statementXml.close();

            //When the xml for the presentation is retrieved, then remove it for the respective module
            PreparedStatement statementRemovePresentation = connection.prepareStatement("SELECT edi.public.sp_removepresentationfrommodule(?, ?);");
            //Fill prepared statements to avoid SQL injection
            statementRemovePresentation.setInt(1, moduleID);
            statementRemovePresentation.setString(2, xml_path);

            //Call stored procedure on database
            statementRemovePresentation.executeQuery();

            return_status_removal = statementRemovePresentation.getResultSet().toString(); //TODO is this right Amrik?

            statementRemovePresentation.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }
        return return_status_removal;
    }

    public ArrayList<Interaction> getInteractionsForInteractiveElement(int interactiveElementID) {
        ArrayList<Interaction> interactionsForElement = new ArrayList<>();

        //Attempt to add a user using stored procedure
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM public.sp_getinteractionsforinteractiveelement(?);");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, interactiveElementID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                interactionsForElement.add(new Interaction(rs.getInt("interaction_id"), rs.getInt("user_id"), rs.getInt("interactive_element_id"), rs.getString("interaction_data"), rs.getTimestamp("time_created")));
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return interactionsForElement;
    }

    public boolean setPresentationLive(int presentationID, boolean live) {
        boolean statementSuccess = false;
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE presentations SET live = ? WHERE presentation_id = ?;");

            //Fill prepared statements to avoid SQL injection
            statement.setBoolean(1, live);
            statement.setInt(2, presentationID);

            //Call stored procedure on database
            statementSuccess = statement.execute();

            if (statementSuccess) {
                logger.error("Unable to set presentation live. Connectivity issues may have been encountered.");
            } else {
                logger.info("Presentation successfully set to live.");
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return statementSuccess;
    }

    public ArrayList<User> getPresentationActiveUsers(int presentationID) {
        ArrayList<User> activeUsers = new ArrayList<>();

        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM USERS WHERE active_presentation_id = ?;");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, presentationID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            int size = 0;

            while (rs.next()) {
                activeUsers.add(new User(rs.getInt("user_id"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("email_address"), rs.getString("user_type")));
                size++;
            }

            if (size == 0) {
                logger.warn("No users active for presentationID: " + presentationID);
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return activeUsers;
    }

    public boolean setCurrentSlideForPresentation(int presentationID, int currentSlide) {
        boolean statementSuccess = false;
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE presentations SET current_slide_number = ? WHERE presentation_id = ?;");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, currentSlide);
            statement.setInt(2, presentationID);

            //Call stored procedure on database
            statementSuccess = statement.execute();

            if (statementSuccess) {
                logger.error("Unable to set current slide number.");
            } else {
                logger.info("Successfully changed current slide number.");
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return statementSuccess;
    }

    public int getCurrentSlideForPresentation(int presentationID) {
        int currentSlide = 0;

        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT current_slide_number FROM presentations WHERE presentation_id = ?;");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, presentationID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                currentSlide = rs.getInt("current_slide_number");
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return currentSlide;
    }

    public boolean setUserActivePresentation(int presentationID, int userID) {
        boolean statementSuccess = false;
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE users SET active_presentation_id = ? WHERE user_id = ?;");

            //If set to no presentation
            if (presentationID == 0) {
                statement.setNull(1, 0);
                statement.setInt(2, userID);
            } else {
                //Fill prepared statements to avoid SQL injection
                statement.setInt(1, presentationID);
                statement.setInt(2, userID);
            }

            //Call stored procedure on database
            statementSuccess = statement.execute();

            if (statementSuccess) {
                logger.error("Unable to set active presentation for user.");
            } else {
                logger.info("User: " + userID + " is now active in presentation: " + presentationID);
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return statementSuccess;
    }

    public ArrayList<Question> getQuestionsForPresentation(int presentationID) {
        ArrayList<Question> activeQuestions = new ArrayList<>();

        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM QUESTIONS WHERE presentation_id = ? AND time_answered IS NULL;");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, presentationID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            int size = 0;

            while (rs.next()) {
                activeQuestions.add(new Question(rs.getInt("question_id"), rs.getInt("user_id"), rs.getInt("presentation_id"), rs.getTimestamp("time_created"), rs.getTime("time_answered"), rs.getString("question_data"), rs.getInt("slide_number")));
                size++;
            }

            if (size == 0) {
                logger.warn("No questions for presentationID: " + presentationID);
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return activeQuestions;
    }
}
