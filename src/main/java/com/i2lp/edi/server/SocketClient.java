package com.i2lp.edi.server;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.presentationElements.*;
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
import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.i2lp.edi.client.Constants.*;
import static java.lang.Thread.currentThread;

/**
 * Created by amriksadhra on 20/03/2017.
 */

/**
 * Class to connect with Socket Server on server side.
 */
public class SocketClient {
    /* Constant to indicate server has not responded */
    private static final int NO_RESPONSE = -1;
    public static final int PASSWORD_INVALID = 0;
    //Timeout times for user addition/authorisation asynchronous functions
    private static final int LOGIN_TIMEOUT = 5;
    private static final int ADDITION_TIMEOUT = 5;

    private Logger logger = LoggerFactory.getLogger(SocketClient.class);

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

    /**
     * Sets the Edi Manager
     * @param ediManager the edi manager
     */
    public void setEdiManager(EdiManager ediManager) {
        this.ediManager = ediManager;
    }

    /**
     * Connects to the database with dbHostName
     * @param dbHostName the database host name
     */
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

    /**
     * Connects to remote socket using socketHostName and serverPort
     * @param socketHostName the socket host name
     * @param serverPort the server port
     */
    public void connectToRemoteSocket(String socketHostName, int serverPort) {
        String serverIPAddress = Utilities.buildIPAddress(socketHostName, serverPort);

        //Alert tester that connection is being attempted
        logger.info("Client: Attempting Connection to " + serverIPAddress);


        try {
            socket = IO.socket(serverIPAddress);
            socket.io().reconnectionAttempts(10);
        } catch (URISyntaxException e) {
            logger.error("Couldn't create client port: May be in use by other program!");
        }

        socket.on(Socket.EVENT_CONNECT, args -> {
            Platform.runLater(() -> {
                ediManager.getLoginDialog().changeGuiPostConnection();
            });
            logger.info("Client successfully connected to Edi Server");
        });


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
        //If the user has logged in
        if (ediManager.isLoggedIn()) {
            //SocketIO will pass a generic object. But we know its a string because that's what DB_notify returns from com.i2lp.edi.server side
            switch ((String) tableToUpdate) {
                case "interactions":
                    if (ediManager.getPresentationManager() != null) {//If in a presentation
                        if (ediManager.getPresentationManager().getTeacherSession() != null) {//That a teacher is holding that is live
                            ediManager.getPresentationManager().getTeacherSession().setInteractionsForPresentation(getInteractionsForPresentation(ediManager.getPresentationManager().getPresentationElement().getPresentationMetadata().getPresentationID()));
                        }
                    }
                    break;

                case "interactive_elements":
                    if (ediManager.getPresentationManager() != null) { //If in a presentation
                        if (ediManager.getPresentationManager().getStudentSession() != null) {
                            ediManager.getPresentationManager().getStudentSession().setInteractiveElementsToRespondRecord(ediManager.getSocketClient().getInteractiveElementsForPresentation(ediManager.getPresentationManager().getPresentationElement().getPresentationMetadata().getPresentationID()));
                        }
                    }
                    break;

                case "users":
                    if (ediManager.getPresentationManager() != null) {
                        if (ediManager.getPresentationManager().getTeacherSession() != null) {//If in a live session as a teacher
                            logger.info("Updating active user list for live presentation.");
                            ediManager.getPresentationManager().getTeacherSession().setActiveUsers(getPresentationActiveUsers(ediManager.getPresentationManager().getPresentationElement().getPresentationMetadata().getPresentationID()));//Update list of active users in that presentation
                        }
                    }
                    break;

                case "presentations":
                    //Update presentation list if no presentation manager is open
                    if (ediManager.getPresentationManager() == null) {
                        if (ediManager.getPresentationLibraryManager() != null) {
                            ediManager.getPresentationLibraryManager().updatePresentations(); //Update presentation information
                        }
                    } else if ((ediManager.getPresentationManager().getTeacherSession() == null)) {
                        if (ediManager.getUserData().getUserType().equals("teacher")) {
                            if (ediManager.getPresentationLibraryManager() != null) {
                                ediManager.getPresentationLibraryManager().updatePresentations(); //Update presentation information
                            }
                        }
                    } else if ((ediManager.getPresentationManager().getStudentSession() == null)) {
                        if (ediManager.getUserData().getUserType().equals("student")) {
                            if (ediManager.getPresentationLibraryManager() != null) {
                                ediManager.getPresentationLibraryManager().updatePresentations(); //Update presentation information
                            }
                        }
                    }
                    if (ediManager.getPresentationManager() != null) {
                        if (ediManager.getPresentationManager().getStudentSession() != null) {//that is live and am a student
                            if (ediManager.getPresentationManager().getStudentSession().isLinked()) {
                                ediManager.getPresentationManager().getStudentSession().synchroniseWithTeacher();
                            }
                        }
                    }
                    break;

                case "jnct_users_modules":
                    logger.info("Modules user is registered for may have changed!");
                    ediManager.getPresentationLibraryManager().updatePresentations(); //Update presentation information
                    break;


                case "modules":
                    if (ediManager.getPresentationManager() == null) {
                        if (ediManager.getPresentationLibraryManager() != null) {
                            ediManager.getPresentationLibraryManager().updatePresentations(); //Update presentation information
                        }
                    }
                    break;

                case "questions":
                    if (ediManager.getPresentationManager() != null) {//If in a presentationforeign
                        if (ediManager.getPresentationManager().getTeacherSession() != null) { //a teacher in a live presentation
                            ediManager.getPresentationManager().getTeacherSession().setQuestionQueue(ediManager.getSocketClient().getQuestionsForPresentation(ediManager.getPresentationManager().getPresentationElement().getPresentationMetadata().getPresentationID(), false)); //Update the question queue in the session

                        }
                    }
                    break;
            }
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
            logger.info("Attempting login of user with username: " + toAuth.getUserToLogin());
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

    public ArrayList<User> getStudentsForModule(int moduleId) {
        ArrayList<User> associatedUsers = new ArrayList<>();

        // First get find what module a presentation belongs to.
        // Then find all the users which have access to that module.
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement getUsersForModuleStatement = connection.prepareStatement("SELECT * FROM jnct_users_modules WHERE module_id = ?;");

            getUsersForModuleStatement.setInt(1, moduleId);

            //Query the database
            ResultSet rs = getUsersForModuleStatement.executeQuery();

            //Handle results
            while (rs.next()) {
                associatedUsers.add(getUser(rs.getInt("user_id")));
            }

            if (associatedUsers.size() == 0) {
                logger.warn("Module " + moduleId + "Didn't return any users.  Does it exist?");
            }

            getUsersForModuleStatement.close();
        } catch (SQLException e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return associatedUsers;
    }


    /**
     * Gets most of a User entry (Doesn't get password or salt)
     * @param userId the user ID
     * @return the user with the user ID matching
     */
    public User getUser(int userId) {
        User retrievedUser = null;

        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT user_id,user_type,username,first_name,last_name,email_address FROM users WHERE user_id=?;");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, userId);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                retrievedUser = new User(rs.getInt("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email_address"),
                        rs.getString("user_type")
                );
                logger.info("Retrieved User " + userId);
            } else {
                logger.error("Failed to retrieve user with Id " + userId);
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return retrievedUser;
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
                    Platform.runLater(() -> ediManager.getLoginDialog().messageLabel.setText("Incorrect username/password."));
                    logger.error("Incorrect username/password for login.");
                }
            });
        } catch (JSONException e) {
            logger.error("Unable to generate JSON object for passing user authentication details. ", e);
        }

        //Spinlock method until the server has responded, and changed the value of the success variable (user id not equal to 0)
        while (loginSuccessFinal.get().getUserID() == NO_RESPONSE) {
            logger.trace("JVM optimises out empty while loops, hence this.. Waiting for server response.");
            if (currentThread().isInterrupted())
                break; //This thread was left dangling. Check for interruption from JVM Shutdown and use to escape while loop
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

    /**
     * Closes all sockets and connections
     */
    public void closeAll() {
        try {
            dataSource.getConnection().close();
        } catch (SQLException e) {
            logger.info("Failed to close com.i2lp.edi.client connection to DB. Non-fatal, still terminating.");
        }
        socket.close();
    }

    /**
     * Gets presentations of which the user has access
     * @param userID the user ID
     * @return
     */
    public ArrayList<PresentationMetadata> getPresentationsForUser(int userID) {
        ArrayList<PresentationMetadata> presentationsForUser = new ArrayList<>();

        //Attempt to add a user using stored procedure
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM edi.public.sp_get_presentations_for_user(?);");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, userID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                presentationsForUser.add(new PresentationMetadata(rs.getInt("presentation_id"), rs.getInt("module_id"), rs.getInt("current_slide_number"), rs.getString("xml_url"), rs.getBoolean("live"), rs.getTimestamp("go_live_timestamp")));
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }
        return presentationsForUser;
    }

    /**
     * Get modules of which the user is part of
     * @param userID the user ID
     * @return modules of which the user is part of
     */
    public ArrayList<Module> getModulesForUser(int userID) {
        ArrayList<Module> modulesForUser = new ArrayList<>();

        //Attempt to add a user using stored procedure
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM edi.public.sp_get_modules_for_user(?);");

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
    public void alertServerToUpload(String presentationName, int moduleID, Presentation presentation) {
        socket.emit("NewUpload", presentationName + " " + moduleID);
        socket.on("NewUploadStatus", objects -> {
            int presentationId = Integer.parseInt((String) objects[0]);
            logger.info("Added " + presentationName + " presentation with the following ID " + objects[0]);
            if (Integer.parseInt((String) objects[0]) != -1) {
                logger.info("Adding interactive elements to DB");
                if (presentationId != -1) {
                    logger.info("Adding interactive elements to DB");
                    sendInteractiveElementsToServer(presentation, Integer.parseInt((String) objects[0]));//Update the interactive_elements table for the new presentation
                    ediManager.getPresentationLibraryManager().updatePresentations(); //Go and download the presentation from the server
                } else {
                    logger.warn("Failed to add presentation to the presentations database");
                }
            }
        });
    }

    /**
     * Set a presentation go live date
     * @param presentationId the presentation ID
     * @param goLiveDate The go live date
     * @return true if successfull
     */
    public boolean setPresentationGoLive(int presentationId, String goLiveDate) {
        boolean statementSuccess = false;

        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE presentations SET go_live_timestamp=? WHERE presentation_id=?");

            if (goLiveDate.equals("0")) {
                statement.setNull(1, 0);
            } else {
                statement.setString(1, goLiveDate);
            }
            statement.setInt(2, presentationId);

            //Call Query on database
            statementSuccess = statement.execute();

            if (statementSuccess) {
                logger.error("Unable to set goLive date for presentation:  " + presentationId);
            } else {
                logger.info("Added goLive date for presentation " + presentationId + " as " + goLiveDate);
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return statementSuccess;
    }

    /**
     * Send the interactive element to server
     * @param presentation the presentation element
     * @param presentationId the presentation ID
     */
    private void sendInteractiveElementsToServer(Presentation presentation, int presentationId) {
        //Presentation successfully uploaded, send the details of the interactive elements to the DB.
        for (Slide slides : presentation.getSlideList()) {
            if (!slides.getInteractiveElementList().isEmpty()) {
                setInteractiveElementsForPresentation(
                        slides.getInteractiveElementList(),
                        presentationId,//Pres ID
                        slides.getSlideID()//Slide Number
                );
                logger.info("Adding interactive elements from slide " + slides.getSlideID() + " Presentation: " + presentationId);
            }
        }
        logger.info("Finished uploading interactive elements");
    }

    /**
     * Delete the requested presentation from the respective module,
     * deleting the data base table reference.
     * @param presentationID Presentation ID to delete
     * @return contains success if successful
     */
    public String removePresentationFromModule(int presentationID) {
        String return_status_removal = "";
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            //When the xml for the presentation is retrieved, then remove it for the respective module
            PreparedStatement statementRemovePresentation = connection.prepareStatement("SELECT * FROM edi.public.sp_remove_presentation(?);");
            //Fill prepared statements to avoid SQL injection
            statementRemovePresentation.setInt(1, presentationID);

            //Call stored procedure on database
            ResultSet removalStatus = statementRemovePresentation.executeQuery();

            while (removalStatus.next()) {
                return_status_removal = removalStatus.getString(1);
            }

            if (return_status_removal.contains("success")) {
                logger.info("Presentation with ID: " + presentationID + " successfully removed");
            } else {
                logger.error("Unable to remove presentation: " + return_status_removal);
            }

            statementRemovePresentation.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }
        return return_status_removal;
    }

    /**
     * Get the interactions for a interactive element
     * @param interactiveElementID the interactive element ID
     * @return returns the interactions for the interactive element
     */
    public ArrayList<InteractionRecord> getInteractionsForInteractiveElement(int interactiveElementID) {
        ArrayList<InteractionRecord> interactionsForElement = new ArrayList<>();

        //Attempt to add a user using stored procedure
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM public.sp_get_interactions_for_interactiveelement(?);");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, interactiveElementID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                interactionsForElement.add(new InteractionRecord(rs.getInt("interaction_id"), rs.getInt("user_id"), rs.getInt("interactive_element_id"), rs.getString("interaction_data"), rs.getTimestamp("time_created")));
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return interactionsForElement;
    }

    /**
     * Sets a presentation live
     * @param presentationID the presentation ID
     * @param live true if the presentation is live
     * @return true if success
     */
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

        return !statementSuccess;
    }

    /**
     * Gets the active users in a presentation
     * @param presentationID the presentation ID
     * @return an array of users active in the presentation
     */
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

    /**
     * Set the current slide and sequence for a presentation
     * @param presentationID the presentation ID
     * @param currentSlide the current slide number
     * @param currentSequence the current sequence number
     */
    public void setCurrentSlideAndSequenceForPresentation(int presentationID, int currentSlide, int currentSequence) {
        Thread statementThread = new Thread(() -> {
            boolean statementSuccess = false;
            try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE presentations SET current_slide_number = ?, current_sequence_number = ? WHERE presentation_id = ?;");


                if ((currentSlide == 0) && (currentSequence == 0)) {
                    statement.setNull(1, 0);
                    statement.setNull(2, 0);
                } else {
                    //Fill prepared statements to avoid SQL injection
                    statement.setInt(1, currentSlide);
                    statement.setInt(2, currentSequence);
                }
                statement.setInt(3, presentationID);

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
        });
        statementThread.start();
    }

    /**
     * Get the current slide for a presentation
     * @param presentationID the presentation ID
     * @return an integer array with [0]-currentSlide and [1]-currentSequenceNumber
     */
    public Integer[] getCurrentSlideForPresentation(int presentationID) {
        Integer[] toReturn = new Integer[2];

        Integer currentSlide = 0;
        Integer currentSequenceNumber = 0;

        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT current_slide_number, current_sequence_number FROM presentations WHERE presentation_id = ?;");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, presentationID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {

                currentSlide = rs.getInt("current_slide_number");
                if (rs.wasNull()) currentSlide = -1;
                currentSequenceNumber = rs.getInt("current_sequence_number");
                if (rs.wasNull()) currentSequenceNumber = -1;
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        toReturn[0] = currentSlide;
        toReturn[1] = currentSequenceNumber;

        return toReturn;
    }

    /**
     * set Interactive elements for presentation
     * @param elements interactive elements
     * @param presentationID presentation ID
     * @param slideNumber slide number
     * @return true if successful
     */
    public boolean setInteractiveElementsForPresentation(List<InteractiveElement> elements, int presentationID, int slideNumber) {
        boolean statementSuccess = false;
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            for (InteractiveElement element : elements) {
                PreparedStatement statement =
                        connection.prepareStatement("SELECT * FROM public.sp_add_and_replace_interactive_element(?,?,?,?,?,?)");

                statement.setInt(1, element.getElementID());
                statement.setInt(2, presentationID);
                statement.setString(3, "");//Data

                if (element instanceof WordCloudElement) {
                    statement.setString(4, InteractiveElement.WORD_CLOUD);
                } else if (element instanceof PollElement) {
                    statement.setString(4, InteractiveElement.POLL);
                } else {
                    logger.error("Unknown Interactive element type: " + element.toString());
                }

                statement.setString(5, "");//Interval
                statement.setInt(6, slideNumber);

                //Call stored procedure on database
                ResultSet rs = statement.executeQuery();

                String status = "failure";

                while (rs.next()) {
                    status = rs.getString(1);
                }

                if (status.equals("success")) {
                    statementSuccess = statementSuccess && true;//Ensure we know if any of them have failed.
                    logger.info("Successfully added interactive element");
                } else logger.error("Unable to add element " + status);


            }
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }
        return true;
    }

    /**
     * Set user active in presentation
     * @param presentationID presentation ID
     * @param userID user ID
     * @return true if successful
     */
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

    /**
     * add question to question que for specific presentaion and slide
     * @param userID user ID
     * @param presentationID presentation ID
     * @param questionData question
     * @param slideNumber slidenumber question was asked at
     * @return true if successful
     */
    public boolean addQuestionToQuestionQueue(int userID, int presentationID, String questionData, int slideNumber) {
        boolean statementSuccess = false;

        //Attempt to add a user using stored procedure
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM public.sp_add_question_to_questionqueue(?, ?, ?, ?);");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, userID);
            statement.setInt(2, presentationID);
            statement.setString(3, questionData);
            statement.setInt(4, slideNumber);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            String status = "failure";

            while (rs.next()) {
                status = rs.getString(1);
            }

            if (status.equals("success")) {
                statementSuccess = true;
                logger.info("Successfully added question to question queue.");
            } else logger.error("Unable to add question: " + status);

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return statementSuccess;
    }

    /**
     * Asnwer question in question que
     * @param questionID question ID to be answered
     * @return true if successful
     */
    public boolean answerQuestionInQuestionQueue(int questionID) {
        boolean statementSuccess = false;

        //Attempt to add a user using stored procedure
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM  public.sp_answer_question_in_questionqueue(?);");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, questionID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            String status = "failure";

            while (rs.next()) {
                status = rs.getString(1);
            }

            if (status.equals("success")) {
                statementSuccess = true;
                logger.info("Successfully answered question: " + questionID);
            } else logger.error("Unable to answer question: " + status);

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return statementSuccess;
    }

    /**
     * Get the questions for a presentation
     * @param presentationID presentation ID
     * @param retrieveAnswered boolean to choose if you want all questions or only unanswered ones.
     * @return a question array
     */
    public ArrayList<Question> getQuestionsForPresentation(int presentationID, boolean retrieveAnswered) {
        ArrayList<Question> activeQuestions = new ArrayList<>();

        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement;
            if (retrieveAnswered) {
                statement = connection.prepareStatement("SELECT * FROM QUESTIONS WHERE presentation_id = ?");
            } else {
                statement = connection.prepareStatement("SELECT * FROM QUESTIONS WHERE presentation_id = ? AND time_answered IS NULL");
            }

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, presentationID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            int size = 0;

            while (rs.next()) {
                activeQuestions.add(new Question(rs.getInt("question_id"), rs.getInt("user_id"), rs.getInt("presentation_id"), rs.getTimestamp("time_created"), rs.getTimestamp("time_answered"), rs.getString("question_data"), rs.getInt("slide_number")));
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

    /**
     * Set interactive element live
     * @param presentationID presentaion ID
     * @param interactiveElementID interactive element ID
     * @param isLive true if element is live
     * @return true if successful
     */
    public boolean setInteractiveElementLive(int presentationID, int interactiveElementID, boolean isLive) {
        boolean statementSuccess = false;

        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = null;
            if (isLive) {
                statement =
                        connection.prepareStatement("UPDATE interactive_elements " +
                                "SET live = ?, local_start_time = ? " +
                                "WHERE interactive_element_id = ? AND presentation_id = ?;");

                //Fill prepared statements to avoid SQL injection
                statement.setBoolean(1, isLive);
                statement.setTime(2, new Time(Instant.now().toEpochMilli()));
                statement.setInt(3, interactiveElementID);
                statement.setInt(4, presentationID);
            } else {
                statement =
                        connection.prepareStatement("UPDATE interactive_elements " +
                                "SET live = ? WHERE interactive_element_id = ? AND presentation_id = ?;");

                //Fill prepared statements to avoid SQL injection
                statement.setBoolean(1, isLive);
                statement.setInt(2, interactiveElementID);
                statement.setInt(3, presentationID);
            }

            //Call stored procedure on database
            statementSuccess = statement.execute();

            if (statementSuccess) {
                logger.error("Unable to set interactive element: " + interactiveElementID + " live status to: " + isLive);
            } else {
                logger.error("Interactive Element: " + interactiveElementID + " live status change to: " + isLive);
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return statementSuccess;
    }

    /**
     *  Get interactive elements form presentation
     * @param presentationID presentation ID
     * @return arrau of interactive elements
     */
    public ArrayList<InteractiveElementRecord> getInteractiveElementsForPresentation(int presentationID) {
        ArrayList<InteractiveElementRecord> interactiveElementRecords = new ArrayList<>();

        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM edi.public.sp_get_interactiveelements_for_presentation(?);");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, presentationID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            int size = 0;

            while (rs.next()) {
                interactiveElementRecords.add(new InteractiveElementRecord(
                        rs.getInt("interactive_element_id"),
                        rs.getInt("presentation_id"),
                        rs.getString("interactive_element_data"),
                        rs.getString("type"),
                        rs.getBoolean("live"),
                        rs.getTime("local_start_time"),
                        rs.getInt("xml_slide_id"),
                        rs.getInt("xml_element_id")
                ));
                size++;
            }

            if (size == 0) {
                logger.warn("No interactive elements for presentationID: " + presentationID);
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return interactiveElementRecords;
    }

    /**
     * Add interaction to interactive element
     * @param userID user ID
     * @param interactiveElementID interactive element ID
     * @param interactionData interaction data
     * @return
     */
    public boolean addInteractionToInteractiveElement(int userID, int interactiveElementID, String interactionData) {
        boolean statementSuccess = false;

        //Attempt to add a user using stored procedure
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM public.sp_add_interaction_to_interactiveelement(?,?,?);");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, userID);
            statement.setInt(2, interactiveElementID);
            statement.setString(3, interactionData);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            String status = "failure";

            while (rs.next()) {
                status = rs.getString(1);
            }

            if (status.equals("success")) {
                statementSuccess = true;
                logger.info("Successfully added interaction to interactive element.");
            } else logger.error("Unable to add interaction: " + status);

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return statementSuccess;
    }

    /**
     * Get interactions for presentation
     * @param presentationID presentation ID
     * @return interaction array
     */
    public ArrayList<InteractionRecord> getInteractionsForPresentation(int presentationID) {
        ArrayList<InteractionRecord> interactionsForPresentation = new ArrayList<>();

        //Attempt to add a user using stored procedure
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM public.sp_get_interactions_for_presentation(?);");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, presentationID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                interactionsForPresentation.add(new InteractionRecord(rs.getInt("interaction_id"), rs.getInt("user_id"), rs.getInt("interactive_element_id"), rs.getString("interaction_data"), rs.getTimestamp("time_created")));
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return interactionsForPresentation;
    }

    /**
     * Send presentation statistics for presentation
     * @param presentationId presentation ID
     * @param userId user ID
     * @param slideTimes CSV separated seconds on each slide
     * @return true if successful
     */
    public boolean sendPresentationStatistics(int presentationId, int userId, ArrayList<Duration> slideTimes) {

        //Concatinate the slide times for storage
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < slideTimes.size(); i++) {
            builder.append(i + 1);
            builder.append(",");
            builder.append(slideTimes.get(i).getSeconds());
            builder.append("\n");
        }
        String slideTimesString = builder.toString();

        boolean statementSuccess = false;

        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM sp_add_statistics_to_presentation(?, ?, ?)");

            statement.setInt(1, userId);
            statement.setInt(2, presentationId);
            statement.setString(3, slideTimesString);

            //Call Query on database
            String status = "Failure";
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                status = rs.getString(1);
            }

            if (status.equals("success") || status.equals("success: user-presentation pair updated")) {
                statementSuccess = true;
                logger.info("Presentation statistics added for presentation id " + presentationId);
            } else {
                logger.error("Failed to set the presentation statistics for presentation id:" + presentationId);
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return statementSuccess;
    }

    /**
     * Get Presentation Statistics for presentation
     * @param presentationID presentation ID
     * @return return presentation statistics array
     */
    public ArrayList<PresentationStatisticsRecord> getPresentationStatistics(int presentationID) {
        ArrayList<PresentationStatisticsRecord> statisticEntries = new ArrayList<>();

        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM jnct_users_presentations_statistics WHERE presentation_id = ?;");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, presentationID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                statisticEntries.add(
                        new PresentationStatisticsRecord(
                                rs.getInt("presentation_id"),
                                rs.getInt("user_id"),
                                rs.getString("time_per_slide")
                        )
                );
            }

            if (statisticEntries.size() == 0) {
                logger.warn("No statistics for presentation " + presentationID);
            }

            statement.close();
        } catch (SQLException e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }

        return statisticEntries;
    }

    /**
     * Resets all free components tied to an interactive element, setting the
     * interactive elements non-live, and deleting all interactions linked
     * to the element
     *
     * @param interactiveElementID the PK of presentation
     * @return true successful
     */
    public boolean resetInteractionsForInteractiveElement(int interactiveElementID) {
        boolean statementSuccess = false;

        //Attempt to add a user using stored procedure
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM public.sp_reset_interactions_for_interactiveelement(?);");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, interactiveElementID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            String status = "failure";

            while (rs.next()) {
                status = rs.getString(1);
            }

            if (status.contains("success")) {
                statementSuccess = true;
                logger.info("Successfully removed interactions from interactive element.");
            } else
                logger.error("Unable to reset interactions for interactive element with ID: " + interactiveElementID);

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }
        return statementSuccess;
    }

    /**
     * Resets all free components tied to a presentation, setting the presentation
     * and any interactive elements non-live, and deleting all interactions with
     * the presentation.
     *
     * @param presentationID the PK of presentation
     * @return true successful
     */
    public boolean resetInteractionsForPresentation(int presentationID) {
        boolean statementSuccess = false;

        //Attempt to add a user using stored procedure
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM public.sp_reset_interactiveelements_for_presentation(?);");

            //Fill prepared statements to avoid SQL injection
            statement.setInt(1, presentationID);

            //Call stored procedure on database
            ResultSet rs = statement.executeQuery();

            String status = "failure";

            while (rs.next()) {
                status = rs.getString(1);
            }

            if (status.contains("success")) {
                statementSuccess = true;
                logger.info(status);
            } else
                logger.error("Unable to reset interactive elements' interactions for presentation with ID: " + presentationID + " " + status);

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
        }
        return statementSuccess;
    }
}
