package com.i2lp.edi.server;

import com.i2lp.edi.client.utilities.Utils;
import com.i2lp.edi.server.packets.User;
import com.i2lp.edi.server.packets.UserAuth;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.jdbc.PGDataSource;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.i2lp.edi.client.Constants.TEMP_DIR_PATH;
import static com.i2lp.edi.client.utilities.Utils.getFilesInFolder;

/**
 * Created by amriksadhra on 20/03/2017.
 */
public class SocketClient {
    //Timeout times for user addition/authorisation asynchronous functions
    private static final int LOGIN_TIMEOUT = 5;
    private static final int ADDITION_TIMEOUT = 5;

    private Logger logger = LoggerFactory.getLogger(SocketClient.class);
    private String serverIPAddress;

    //TODO: These will be filled by actual values, for now they are temp and meaningless
    private int current_presentation_id = 1;
    private int current_question_id = 1;

    //PostgreSQL database connection
    PGDataSource dataSource;
    Socket socket;

    public static void main(String[] args) {
        new SocketClient("db.amriksadhra.com", 8080);
    }

    public SocketClient(String serverIP, int serverPort) {
        serverIPAddress = Utils.buildIPAddress(serverIP, serverPort);

        connectToRemoteSocket();
        connectToRemoteDB();
    }

    public void connectToRemoteDB() {
        //Connect to PostgreSQL Instance
        dataSource = new PGDataSource();
        dataSource.setHost("db.amriksadhra.com");
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

    public void connectToRemoteSocket() {
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
            case "users":
                logger.info("Users database changed!");
                //TODO: Update Local User information
                break;

            case "presentation_library":
                logger.info("Presentation library database changed!");
                //TODO: Update local presentation Information
                break;

            case "classes":
                logger.info("Classes database changed!");
                //TODO: Update class list
                break;

            case "responses":
                logger.info("New responses to act upon registered on com.i2lp.edi.server!");
                updateResponses(current_presentation_id, current_question_id);
                break;
        }
    }

    private void updateResponses(int presentation_id, int question_id) {
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            StringBuilder sb = new StringBuilder();

            //TODO: Create Stored Procedure on PostgreSQL
            sb.append("SELECT * from public.responses where presentation_id = ").append(presentation_id).append(" and question_id = ").append(question_id).append(";");
            logger.info("Adding response to database using SQL: " + sb.toString());
            ResultSet rs = statement.executeQuery(sb.toString());

            while (rs.next()) {
                logger.info(rs.getString("data"));
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to execute update response procedure, PDJBC dump: ", e);
        }
    }

    /**
     * Calls userAuthAsync function but with a LOGIN_TIMEOUT second timeout. If we hit timeout, return false, else wait for com.i2lp.edi.server
     * to respond with response.
     *
     * @param toAuth User details to authenticate
     * @return String containing user_type of authenticated user.
     * @author Amrik Sadhra
     */
    public String userAuth(UserAuth toAuth) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new UserAuthTask(toAuth));

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
        return "false";
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

    class UserAuthTask implements Callable<String> {
        UserAuth toAuth;

        public UserAuthTask(UserAuth toAuth) {
            this.toAuth = toAuth;
        }

        @Override
        public String call() throws Exception {
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
     * @author Amrik Sadhra
     */
    public String userAuthAsync(UserAuth toAuth) {
        //Ensure atomic write to variable, bypassing Lambda final restriction
        AtomicReference<String> loginSuccessFinal = new AtomicReference<>("no_response");

        JSONObject obj = new JSONObject();
        try {
            obj.put("userToLogin", toAuth.getUserToLogin());
            obj.put("password", toAuth.getPassword());
            socket.emit("AuthUser", obj);
            socket.on("AuthUser", objects -> {
                if (!(objects[0]).equals("auth_fail")) {
                    logger.info("User " + toAuth.getUserToLogin() + " has successfully logged in");
                } else {
                    logger.error("Incorrect username/password for login.");
                }

                loginSuccessFinal.set((String) objects[0]);
            });
        } catch (JSONException e) {
            logger.error("Unable to generate JSON object for passing user authentication details. ", e);
        }

        //Spinlock method until the com.i2lp.edi.server has responded, and changed the value of the success variable
        while (loginSuccessFinal.get().equals("no_response")) {
            logger.trace("JVM optimises out empty while loops, hence this.. Waiting for server response.");
        }

        //Return the string holding the user_type
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


    public void listUsers() {
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            //List Users
            ResultSet userList = statement.executeQuery("SELECT * FROM USERS;");
            while (userList.next()) {
                int user_id = userList.getInt("user_id");
                String first_name = userList.getString("first_name");
                String second_name = userList.getString("second_name");
                String login_name = userList.getString("login_name");
                String userType = userList.getString("user_type");
                int class_id = userList.getInt("classes_class_id");

                System.out.println("user_ID: " + user_id + " | login_name: " + login_name + " | fn: " + first_name + " | sn: " + second_name + " | userType: " + userType + " | class_id: " + class_id);
            }
            userList.close();
            statement.close();
        } catch (Exception e) {
            logger.error("Unable to execute update response procedure, PDJBC dump: ", e);
        }
    }

    public void closeAll() {
        try {
            dataSource.getConnection().close();
        } catch (SQLException e) {
            logger.info("Failed to close com.i2lp.edi.client connection to DB. Non-fatal, still terminating.");
        }
        socket.close();
    }

    public void sendLocalThumbnailList(){
        //Get presentation names from Server through SQL query
        //Generate the directory names and build a tree
        getFilesInFolder(TEMP_DIR_PATH);
        // Use Apache commons library to get difference between server thumbnails and client thumbnails
       /* List difference = ListUtils.subtract(Arrays.asList(filesOnServer), Arrays.asList(clientFiles));

        // If no difference between client and server, dont send request data packet
        if (difference.size() == 0) {
            return;
        } else {
            socket.emit("ClientThumbnails");
        }*/
    }
}
