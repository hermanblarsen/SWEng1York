package server;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import com.impossibl.postgres.jdbc.PGDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.packets.User;
import server.packets.UserAuth;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import static server.Crypto.calculateHash;


/**
 * Created by amriksadhra on 20/03/2017.
 */
@SuppressWarnings("Convert2Lambda")
public class socketServer {
    private Logger logger = LoggerFactory.getLogger(socketServer.class);
    private int serverPort;

    //PostgreSQL database connection
    PGDataSource dataSource;

    ArrayList<SocketIOClient> myClients = new ArrayList<>();

    public static void main(String[] args) {
        new socketServer("db.amriksadhra.com",8080);
    }

    public socketServer(String dbHostName, int serverPort) {
        this.serverPort = serverPort;

        connectToLocalDB(dbHostName);
        startSocket();


    }

    public void startSocket() {
        //Startup Socket.IO Server so can pump update events to clients
        logger.info("Attempting to start up Socket.IO server on port " + serverPort);

        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(serverPort);

        final SocketIOServer server = new SocketIOServer(config);
        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient socketIOClient) {
                myClients.add(socketIOClient);
                logger.info("Connection from client UUID: " + socketIOClient.getSessionId());
            }
        });

        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient socketIOClient) {
                logger.info("Disonnection from " + socketIOClient.getSessionId());
                myClients.remove(socketIOClient);
            }
        });

        server.addEventListener("AddUser", User.class, new DataListener<User>() {
            @Override
            public void onData(final SocketIOClient client, User data, final AckRequest ackRequest) {
                //AddUser code goes here
                String passwordSalt = Crypto.bytetoString(Crypto.generateSalt());
                String passwordHash = calculateHash(data.getPassword(), passwordSalt);

                //Attempt to add a user
                try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
                    Statement statement = connection.createStatement();

                    ResultSet presentUser = statement.executeQuery("SELECT login_name FROM USERS where login_name like '" + data.getLoginName() + "';");
                    if (presentUser.next()) {
                        logger.error("Tried to add user that was already in database");
                        //Let client know whether their operation was successful
                        client.sendEvent("AddUser", false);
                        return;
                    }

                    StringBuilder sb = new StringBuilder();

                    //TODO: Create Stored Procedure on PostgreSQL
                    sb.append("INSERT INTO public.users (login_name, first_name, second_name, password_hash, password_salt, is_teacher, classes_class_id) VALUES ('");
                    sb.append(data.getLoginName()).append("', '");
                    sb.append(data.getFirstName()).append("', '");
                    sb.append(data.getSecondName()).append("', '");
                    sb.append(passwordHash).append("', '");
                    sb.append(passwordSalt).append("', ");
                    sb.append(data.getTeacher()).append(", ");
                    sb.append(1 + ");");

                    logger.info("Adding user to database using SQL: " + sb.toString());
                    /*TODO: This statement does return a boolean, but it seems to be false even if we've successfully added to db
                        I set the success boolean to true anyway, but we may be losing some information here.  */
                    statement.execute(sb.toString());
                    //Let client know whether their operation was successful
                    client.sendEvent("AddUser", true);

                    statement.close();
                } catch (Exception e) {
                    logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
                }


            }
        });

        server.addEventListener("AuthUser", UserAuth.class, new DataListener<UserAuth>() {
            @Override
            public void onData(final SocketIOClient client, UserAuth data, final AckRequest ackRequest) {
                boolean authSuccess = false;

                try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
                    Statement statement = connection.createStatement();

                    String userToLogin = data.getUserToLogin();
                    String enteredPassword = data.getPassword();

                    //Auth Test
                    ResultSet userList = statement.executeQuery("SELECT password_hash, password_salt  FROM USERS where login_name like '" + userToLogin + "';");

                    while (userList.next()) {
                        String password_hash = userList.getString("password_hash");
                        String password_salt = userList.getString("password_salt");

                        if (calculateHash(enteredPassword, password_salt).equals(password_hash.toLowerCase())) {
                            authSuccess = true;
                            logger.info("User " + userToLogin + " successfully logged in! Time to let the client know.");
                        }
                    }
                    userList.close();
                    statement.close();
                } catch (Exception e) {
                    logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
                }

                //Alert client to whether their user auth was a success or fail
                client.sendEvent("AuthUser", authSuccess);
            }
        });

        server.start();
    }

    /**
     * Connects to Local PostgreSQL instance and registers the event listener that fires whenever the
     * database is modified. Requires the connection to remain active, and so has an infinite while loop.
     * It is threaded to account for this.
     * @author Amrik Sadhra
     */
    public void connectToLocalDB(String dbHostName) {
        Thread dbPoller = new Thread(() -> {
            //Connect to PostgreSQL Instance
            dataSource = new PGDataSource();
            dataSource.setHost(dbHostName);
            dataSource.setPort(5432);
            dataSource.setDatabase("edi");
            dataSource.setUser("iilp");
            dataSource.setPassword("group1SWENG");

            PGNotificationListener listener = (int processId, String channelName, String payload) -> {
                for (SocketIOClient myClient : myClients) {
                    myClient.sendEvent("DB_Update", payload);
                }
            };

            try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
                Statement statement = connection.createStatement();
                statement.execute("LISTEN db_update");
                statement.close();
                connection.addNotificationListener(listener);
                logger.info("Successful connection to PostgreSQL database instance");
                while (true) {
                }
            } catch (Exception e) {
                logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
            }
        });
        dbPoller.start();
    }

}
