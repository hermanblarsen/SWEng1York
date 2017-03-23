package server;

import com.corundumstudio.socketio.AckCallback;
import com.impossibl.postgres.jdbc.PGDataSource;
import io.socket.client.IO;
import io.socket.client.Socket;
import client.utilities.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.packets.User;
import server.packets.UserAuth;

import java.net.URISyntaxException;

/**
 * Created by amriksadhra on 20/03/2017.
 */
public class socketClient {
    private Logger logger = LoggerFactory.getLogger(socketClient.class);
    Socket socket;
    private String serverIPAddress;
    private String serverIP;
    PGDataSource dataSource;

    public static void main(String[] args) {
        new socketClient("127.0.0.1", 8081);
    }

    public socketClient(String serverIP, int serverPort) {
        if (!Utils.validate(serverIP)) System.out.println("Invalid IP"); //TODO: Log error in IILP, throw exception
        this.serverIP = serverIP;

        serverIPAddress = Utils.buildIPAddress(serverIP, serverPort);

        connectToRemoteDB();
        connectToRemoteSocket();

        //Test server side add of user
        User toAdd = new User("First", "Name", "LoginName", "password", false);
        userAdd(toAdd);

        UserAuth toAuth = new UserAuth("LoginName", "password");
        userAuth(toAuth);
    }

    public void connectToRemoteSocket() {
        //Alert tester that connection is being attempted
        System.out.println("Client: Attempting Connection to " + serverIPAddress);

        try {
            socket = IO.socket(serverIPAddress);
        } catch (URISyntaxException e) {
            System.out.println("Couldn't create client port");
        }

        socket.on(Socket.EVENT_CONNECT, args -> {
            System.out.println("Client connected! Spitting bars.");
            socket.emit("Foo", "hi");
        }).on("DB_Update", args -> {
            System.out.println("Client knows DB has updated:  " + args[0]);
            updateLocalTables(args[0]);
            //Pull fresh table
        }).on(Socket.EVENT_DISCONNECT, args -> {

        });

        //Attempt Socket connection
        socket.connect();
    }

    public void updateLocalTables(Object tableToUpdate) {
        //SocketIO will pass a generic object. But we know its a string because that's what DB_notify returns from server side
        switch ((String) tableToUpdate) {
            case "users":
                //TODO: Update Local User information
                break;

            case "presentation_library":
                //TODO: Update local presentation Information
                break;

            case "classes":
                //TODO: Update class list
                break;
        }
    }


    /**
     * This function generates a JSON object that is passed using socket.IO to the server. It allows us to authenticate
     * a user against the database to determine if they have the correct username/password
     * @param toAuth User to add to the current users database located serverside
     * @author Amrik Sadhra
     */
    public void userAuth(UserAuth toAuth){
        //TODO: Register AckRequest so we can determine if we Auth'd okay or not. Throw UserName/password not found exception so user can be alerted
        JSONObject obj = new JSONObject();
        try {
            obj.put("userToLogin", toAuth.getUserToLogin());
            obj.put("password", toAuth.getPassword());
            //TODO: Get Acknowledge, if failed, throw custom database exception
            socket.emit("AuthUser", obj, new AckCallback<Boolean>(Boolean.class) {
                @Override
                public void onSuccess(Boolean result) {
                    logger.info("Adding user was a " + result);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            logger.error("Unable to generate JSON object for passing user authentication details");
        }
    }


    public void connectToRemoteDB() {
        //Connect to remote PostgreSQL Instance
        dataSource = new PGDataSource();
        dataSource.setHost(serverIP);
        dataSource.setPort(5432);
        dataSource.setDatabase("edi");
        dataSource.setUser("postgres");
        dataSource.setPassword("password");
    }


    /**
     * This function generates a JSON object that is passed using socket.IO to the server. It allows us to add users to
     * the SQL Database
     * @param toAdd User to add to the current users database located serverside
     * @author Amrik Sadhra
     */
    public void userAdd(User toAdd) {
        //When we send data as a custom class, we need to wrap it in JSON with fields named after the variables in our class
        JSONObject obj = new JSONObject();
        try {
            obj.put("firstName", toAdd.getFirstName());
            obj.put("secondName", toAdd.getSecondName());
            obj.put("loginName", toAdd.getLoginName());
            obj.put("password", toAdd.getPassword());
            obj.put("teacherStatus", toAdd.teacherStatus);
            //TODO: Get Acknowledge, if failed, throw custom database exception
            socket.emit("AddUser", obj);
        } catch (JSONException e) {
            e.printStackTrace();
            logger.error("Unable to generate JSON object for passing new user details");
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
