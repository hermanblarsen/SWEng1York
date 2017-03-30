package server;

import client.utilities.FinalWrapper;
import client.utilities.Utils;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.jdbc.PGDataSource;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.packets.User;
import server.packets.UserAuth;

import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.*;

/**
 * Created by amriksadhra on 20/03/2017.
 */
public class socketClient {
    private static final int LOGIN_TIMEOUT = 5;
    private Logger logger = LoggerFactory.getLogger(socketClient.class);
    private String serverIPAddress;

    //TODO: These will be filled by actual values, for now they are temp and meaningless
    private int current_presentation_id = 1;;
    private int current_question_id = 1;

    //PostgreSQL database connection
    PGDataSource dataSource;

    Socket socket;

    public static void main(String[] args) {
        new socketClient("127.0.0.1", 8080);
    }

    public socketClient(String serverIP, int serverPort) {
        if (!Utils.validate(serverIP)) {
            //TODO: Throw exception if invalid IP?
            logger.error("Invalid Server IP address");
            return;
        }
        serverIPAddress = Utils.buildIPAddress(serverIP, serverPort);

        //connectToRemoteSocket();
        connectToRemoteDB();
        //addResponse(1, 2, "lol");
        updateResponses(1, 2);
    }

    public void connectToRemoteDB(){
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

    public void addResponse(int presentation_id, int question_id, String data){
        //Attempt to add a user
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            StringBuilder sb = new StringBuilder();

            //TODO: Create Stored Procedure on PostgreSQL
            sb.append("INSERT INTO public.responses (presentation_id, question_id, data) VALUES ('");
            sb.append(presentation_id).append("', '");
            sb.append(question_id).append("', '");
            sb.append(data + "');");

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
            logger.error("Couldn't create client port");
        }

        socket.on(Socket.EVENT_CONNECT, args -> {
            logger.info("Client connected! Spitting bars.");
        }).on("DB_Update", args -> {
            logger.info("Client knows DB has updated:  " + args[0]);
            updateLocalTables(args[0]);
            //Pull fresh table
        }).on(Socket.EVENT_DISCONNECT, args -> {

        });

        //Attempt Socket connection
        socket.connect();
    }

    /**
     * The main controller for remote database updates. We can act appropriately based upon what has updated remotely.
     * e.g. Live responses to the current presentation can be used to update current graph object on slide.
     * @param tableToUpdate Table that has been updated on Server
     * @author Amrik Sadhra
     */
    public void updateLocalTables(Object tableToUpdate) {
        //SocketIO will pass a generic object. But we know its a string because that's what DB_notify returns from server side
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
                logger.info("New responses to act upon registered on server!");
                updateResponses(current_presentation_id, current_question_id);
                break;
        }
    }

    private void updateResponses(int presentation_id, int question_id) {
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            StringBuilder sb = new StringBuilder();

            //TODO: Create Stored Procedure on PostgreSQL
            sb.append("SELECT * from public.responses where presentation_id = " + presentation_id + " and question_id = " + question_id + ";");
            logger.info("Adding response to database using SQL: " + sb.toString());
            ResultSet rs = statement.executeQuery(sb.toString());

            while(rs.next()){
                logger.info(rs.getString("data"));
            }

            statement.close();
        } catch (Exception e) {
            logger.error("Unable to execute update response procedure, PDJBC dump: ", e);
        }
    }

    /**
     * Calls userAuthAsync function but with a LOGIN_TIMEOUT timeout. If we hit timeout, return false, else wait for server
     * to respond with response.
     * @param toAuth User details to authenticate
     * @return Boolean corrsponding to whether authentication was successful or not
     * @author Amrik Sadhra
     */
    public boolean userAuth(UserAuth toAuth) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(new UserAuthTask(toAuth));

        try {
            logger.info("Attempting login of User: " + toAuth.getUserToLogin());
            Boolean result = future.get(LOGIN_TIMEOUT, TimeUnit.SECONDS);
            return result;
        } catch (TimeoutException e) {
            future.cancel(true);
            logger.error("Connection to server timed out.");
        } catch (InterruptedException e) {
            logger.error("Connection to server was interrupted.");
        } catch (ExecutionException e) {
            logger.error("Connection to server failed. (tragically)");
        }
        //If we hit any of the catch statements
        executor.shutdownNow();
        return false;
    }

    class UserAuthTask implements Callable<Boolean> {
        UserAuth toAuth;

        public UserAuthTask(UserAuth toAuth){
            this.toAuth = toAuth;
        }

        @Override
        public Boolean call() throws Exception {
            return userAuthAsync(toAuth);
        }
    }

    /**
     * This function generates a JSON object that is passed using socket.IO to the server. It allows us to authenticate
     * a user against the database to determine if they have the correct username/password.
     *
     * @param toAuth User to add to the current users database located serverside
     * @author Amrik Sadhra
     */
    public Boolean userAuthAsync(UserAuth toAuth) {
        //Hack to bypass final requirement for Lambda anon methods
        final FinalWrapper loginSuccessFinal = new FinalWrapper("no_response");

        JSONObject obj = new JSONObject();
        try {
            obj.put("userToLogin", toAuth.getUserToLogin());
            obj.put("password", toAuth.getPassword());
            socket.emit("AuthUser", obj);
            socket.on("AuthUser", objects -> {
                if ((boolean) objects[0]) {
                    logger.info("User " + toAuth.getUserToLogin() + " has successfully logged in");
                    loginSuccessFinal.setNonFinal("true");
                } else {
                    logger.error("Incorrect username/password for login.");
                    loginSuccessFinal.setNonFinal("false");
                }
            });
        } catch (JSONException e) {
            logger.error("Unable to generate JSON object for passing user authentication details. ", e);
        }

        //Spinlock method until our final fake boolean has been modified
        while(loginSuccessFinal.getNonFinal().equals("no_response")){
            logger.debug("JVM optimises out empty while loops. Waiting for server response.");
        };

        //Convert the string we used to store 3 data types in (no resp/true/false) down to boolean now that no resp has been removed as possibility
        return Boolean.valueOf((String) loginSuccessFinal.getNonFinal());
    }

    /**
     * This function generates a JSON object that is passed using socket.IO to the server. It allows us to add users to
     * the SQL Database
     *
     * @param toAdd User to add to the current users database located serverside
     * @author Amrik Sadhra
     */
    public void userAdd(User toAdd) {
        //TODO: Add timeout to this task, such as with userAuth

        //When we send data as a custom class, we need to wrap it in JSON with fields named after the variables in our class
        JSONObject obj = new JSONObject();
        try {
            obj.put("firstName", toAdd.getFirstName());
            obj.put("secondName", toAdd.getSecondName());
            obj.put("loginName", toAdd.getLoginName());
            obj.put("password", toAdd.getPassword());
            obj.put("teacherStatus", toAdd.teacherStatus);
            //TODO: If failed, throw custom userAdd exception
            socket.emit("AddUser", obj);
            socket.on("AddUser", objects -> {
                if ((boolean) objects[0]) {
                    logger.info("User " + toAdd.getLoginName() + " was successfully added to database.");
                } else {
                    logger.error("User already present in database. Could not be added");
                }
            });
        } catch (JSONException e) {

            logger.error("Unable to generate JSON object for passing new user details. ", e);
        }
    }


    public void listUsers() {
            /*//List Users
            ResultSet userList = statement.executeQuery("SELECT * FROM USERS;");
            while (userList.next()) {
                int user_id = userList.getInt("user_id");
                String first_name = userList.getString("first_name");
                String second_name = userList.getString("second_name");
                String password = userList.getString("password_hash");
                boolean teacherStatus = userList.getBoolean("is_teacher");
                int class_id = userList.getInt("classes_class_id");
                String login_name = userList.getString("login_name");

                System.out.println("user_ID: " + user_id + " | login_name: " +" | fn: " + first_name + " | sn: " + second_name + " | pw: " + password + " | teacherStatus: " + teacherStatus + " | class_id: " + class_id);
            }
            userList.close();
            statement.close();*/

    }
}
