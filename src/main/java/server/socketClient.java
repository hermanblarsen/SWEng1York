package server;

import client.login.Login;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.jdbc.PGDataSource;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import client.utilities.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.Statement;

import static server.Crypto.calculateHash;

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
        addUser();
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

    public void connectToRemoteDB() {
        //Connect to remote PostgreSQL Instance
        dataSource = new PGDataSource();
        dataSource.setHost(serverIP);
        dataSource.setPort(5432);
        dataSource.setDatabase("edi");
        dataSource.setUser("postgres");
        dataSource.setPassword("password");


        //Move all of this AUTH server side, send packet to confirm login
        try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            String userToLogin = "PooperTrooper";
            String enteredPassword = "testpw";

            //Auth Test
            ResultSet userList = statement.executeQuery("SELECT password_hash, password_salt  FROM USERS where login_name like '" + userToLogin + "';");
            while (userList.next()) {
                String password_hash = userList.getString("password_hash");
                String password_salt = userList.getString("password_salt");

                if (calculateHash(enteredPassword, password_salt).equals(password_hash.toLowerCase())) {
                    //Login was successful
                    System.out.println("WE MADE IT IN");
                }
                ;
            }
            userList.close();
            statement.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }



    //TODO: Move this Server Side
    public void addUser() {
        User toAdd = new User("First", "Name", "LoginName", "password", false);
        socket.emit("AddUser", toAdd);
    }



    public void listUsers() {
            /*//List Users
            ResultSet userList = statement.executeQuery("SELECT * FROM USERS;");
            while (userList.next()) {
                int user_id = userList.getInt("user_id");
                String first_name = userList.getString("first_name");
                String second_name = userList.getString("second_name");
                String password = userList.getString("password_hash");
                boolean isTeacher = userList.getBoolean("is_teacher");
                int class_id = userList.getInt("classes_class_id");
                String login_name = userList.getString("login_name");

                System.out.println("user_ID: " + user_id + " | login_name: " +" | fn: " + first_name + " | sn: " + second_name + " | pw: " + password + " | isTeacher: " + isTeacher + " | class_id: " + class_id);
            }
            userList.close();
            statement.close();*/

    }
}
