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

import java.sql.Statement;
import java.util.ArrayList;


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
        new socketServer(8081);
    }

    public socketServer(int serverPort) {
        this.serverPort = serverPort;

        connectToLocalDB();
        startSocket();
    }

    public void startSocket() {
        //Startup Socket.IO Server so can pump update events to clients
        System.out.println("Attempting to startup server");

        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(serverPort);

        final SocketIOServer server = new SocketIOServer(config);
        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient socketIOClient) {
                myClients.add(socketIOClient);
                System.out.println("Connection from client UUID: " + socketIOClient.getSessionId());
            }
        });

        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient socketIOClient) {
                System.out.println("Disonnection from " + socketIOClient.getSessionId());
                myClients.remove(socketIOClient);
            }
        });

        server.addEventListener("AddUser", User.class, new DataListener<User>() {
            @Override
            public void onData(final SocketIOClient client, User data, final AckRequest ackRequest) {
                // check if ack requested by client,
                if (ackRequest.isAckRequested()) {
                    // send ack response with data to client
                    ackRequest.sendAckData("Client request for user addition received.");
                }

                //AddUser code goes here
                String passwordSalt = Crypto.bytetoString(Crypto.generateSalt());
                String passwordHash = Crypto.calculateHash(data.getPassword(), passwordSalt);
                boolean addSuccess = false;

                //Attempt to add a user
                try (PGConnection connection = (PGConnection) dataSource.getConnection()) {
                    Statement statement = connection.createStatement();

                    StringBuilder sb = new StringBuilder();

                    sb.append("INSERT INTO public.users (login_name, first_name, second_name, password_hash, password_salt, is_teacher, classes_class_id) VALUES ('");
                    sb.append(data.getLoginName()).append("', '");
                    sb.append(data.getFirstName()).append("', '");
                    sb.append(data.getSecondName()).append("', '");
                    sb.append(passwordHash).append("', '");
                    sb.append(passwordSalt).append("', ");
                    sb.append(data.getTeacher()).append(", ");
                    sb.append(1 + ");");

                    logger.info("Adding user to database using SQL: " + sb.toString());
                    addSuccess = statement.execute(sb.toString());

                    statement.close();
                } catch (Exception e) {
                    System.err.println(e);
                }

                // send message back to client with ack callback WITH data
                client.sendEvent("UserAdded", new AckCallback<String>(String.class) {
                    @Override
                    public void onSuccess(String result) {
                        System.out.println("ack from client: " + client.getSessionId() + " data: " + result);
                    }
                }, addSuccess);
            }
        });
        server.start();
    }

    public void connectToLocalDB() {
        Thread dbPoller = new Thread(() -> {
            //Connect to PostgreSQL Instance
            PGDataSource dataSource = new PGDataSource();
            dataSource.setHost("localhost");
            dataSource.setPort(5432);
            dataSource.setDatabase("edi");
            dataSource.setUser("postgres");
            dataSource.setPassword("password");

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
                System.out.println("Connected to DB");
                while (true) {
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        });
        dbPoller.start();
    }

}
