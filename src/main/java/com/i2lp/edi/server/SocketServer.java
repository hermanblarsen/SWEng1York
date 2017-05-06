package com.i2lp.edi.server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.i2lp.edi.client.Constants;
import com.i2lp.edi.client.utilities.Utils;
import com.i2lp.edi.server.packets.User;
import com.i2lp.edi.server.packets.UserAuth;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import com.impossibl.postgres.jdbc.PGDataSource;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import static com.i2lp.edi.client.Constants.*;
import static com.i2lp.edi.server.SocketClient.PASSWORD_INVALID;


/**
 * Created by amriksadhra on 20/03/2017.
 */
@SuppressWarnings("Convert2Lambda")
public class SocketServer {
    private Logger logger = LoggerFactory.getLogger(SocketServer.class);
    SocketIOServer server;
    Thread dbPoller;

    private boolean keepAlive = true;

    //PostgreSQL database connection
    PGDataSource dataSource;

    ArrayList<SocketIOClient> myClients = new ArrayList<>();

    public static void main(String[] args) {
        new SocketServer(remoteServerAddress, 8080);
    }

    public SocketServer(String hostName, int serverPort) {
        logger.info("EDI Server " + Constants.BUILD_STRING);

        if (localServer) {
            connectToLocalDB(remoteServerAddress);
            startSocket(localServerAddress, serverPort);
        } else {
            connectToLocalDB(hostName);
            startSocket(hostName, serverPort);
        }
    }

    public void startSocket(String socketHostName, int serverPort) {
        //Startup Socket.IO Server so can pump update events to clients
        logger.info("Attempting to start up Socket.IO server on port " + Utils.buildIPAddress(socketHostName, serverPort));

        Configuration config = new Configuration();
        config.setHostname(socketHostName);
        config.setPort(serverPort);

        server = new SocketIOServer(config);
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
                logger.info("Disconnection from " + socketIOClient.getSessionId());
                myClients.remove(socketIOClient);
            }
        });

        server.addEventListener("AddUser", User.class, new DataListener<User>() {
            @Override
            public void onData(final SocketIOClient client, User data, final AckRequest ackRequest) {
                String generatedLoginName = "user_add_failed";

                //Attempt to add a user using stored procedure
                try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
                    PreparedStatement statement = connection.prepareStatement("SELECT edi.public.sp_adduser(?, ?, ?, ?, ?);");

                    //Fill prepared statements to avoid SQL injection
                    statement.setString(1, data.getFirstName());
                    statement.setString(2, data.getSecondName());
                    statement.setString(3, data.getEmailAddress());
                    statement.setString(4, data.getPassword());
                    statement.setString(5, data.getUserType());

                    //Call stored procedure on database
                    ResultSet rs = statement.executeQuery();

                    while (rs.next()) {
                        generatedLoginName = rs.getString("sp_adduser");
                        logger.info("Generated login name from SQL database: " + generatedLoginName);
                    }

                    statement.close();
                } catch (Exception e) {
                    logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
                }

                //Let com.i2lp.edi.client know the login name that was generated at addition time
                client.sendEvent("AddUser", generatedLoginName);
            }
        });

        server.addEventListener("AuthUser", UserAuth.class, new DataListener<UserAuth>() {
            @SuppressWarnings("SqlResolve")
            @Override
            public void onData(final SocketIOClient client, UserAuth data, final AckRequest ackRequest) {
                //Generate empty User for client to parse and detect login failed
                User userDataSend = new User(PASSWORD_INVALID, "", "", "", "");

                try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
                    PreparedStatement statement = connection.prepareStatement("SELECT user_id, user_type, username, first_name, last_name, email_address FROM edi.public.sp_authuser(?, ?);");

                    //Fill prepared statements to avoid SQL injection
                    statement.setString(1, data.getUserToLogin());
                    statement.setString(2, data.getPassword());

                    //Call stored procedure on database
                    ResultSet userDataQuery = statement.executeQuery();

                    while (userDataQuery.next()) {
                        if (userDataQuery.getString("user_type") != null) {
                            logger.info("User " + data.getUserToLogin() + " successfully logged in! Time to let the client know.");

                            userDataSend.setUserID(userDataQuery.getInt("user_id"));
                            userDataSend.setFirstName(userDataQuery.getString("first_name"));
                            userDataSend.setSecondName(userDataQuery.getString("last_name"));
                            userDataSend.setEmailAddress(userDataQuery.getString("email_address"));
                            userDataSend.setUserType(userDataQuery.getString("user_type"));
                        }
                    }
                    userDataQuery.close();
                    statement.close();
                } catch (Exception e) {
                    logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
                }

                //Alert com.i2lp.edi.client to whether their user auth was a success or fail by returning user data or empty packet
                client.sendEvent("AuthUser", userDataSend.getUserID(), userDataSend.getFirstName(), userDataSend.getSecondName(), userDataSend.getEmailAddress(), userDataSend.getUserType());
            }
        });

        server.addEventListener("NewUpload", String.class, new DataListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
                String presentationName = data.substring(0, data.lastIndexOf(" "));
                int moduleID = Integer.parseInt(data.substring(data.lastIndexOf(" ")+1, data.length()));

                logger.info("New presentation detected for processing: " + presentationName + " ModuleID: " + moduleID);

                //Move Zip directly to /var/www/html/Edi/
                try {
                    FileUtils.moveFile(new File("/home/bscftp/Uploads/" + presentationName + ".zip"), new File("/var/www/html/Edi/" + presentationName + ".zip"));
                    //Run SQL statement
                    //Insert some shit into the db
                } catch (IOException e) {
                    logger.error("Unable to move Uploaded presentation " + data + " + to host directory.", e);
                }
            }
        });
        server.start();
    }

    /**
     * Connects to Local PostgreSQL instance and registers the event listener that fires whenever the
     * database is modified. Requires the connection to remain active, and so has an infinite while loop.
     * It is threaded to account for this.
     *
     * @author Amrik Sadhra
     */
    @SuppressWarnings({"InfiniteLoopStatement", "StatementWithEmptyBody"})
    public void connectToLocalDB(String dbHostName) {
        dbPoller = new Thread(() -> {
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
                while (keepAlive) {
                    //Keep connection active in order to maintain listen/notify events
                }
            } catch (Exception e) {
                logger.error("Unable to connect to PostgreSQL on port 5432. PJDBC dump:", e);
            }
        });
        dbPoller.start();
    }
}
