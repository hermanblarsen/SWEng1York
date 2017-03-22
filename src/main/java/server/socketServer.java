package server;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import com.impossibl.postgres.jdbc.PGDataSource;

import java.sql.Statement;
import java.util.ArrayList;


/**
 * Created by amriksadhra on 20/03/2017.
 */
@SuppressWarnings("Convert2Lambda")
public class socketServer {
    private int serverPort;

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

        server.addEventListener("Foo", String.class, new DataListener<String>() {
            @Override
            public void onData(final SocketIOClient client, String data, final AckRequest ackRequest) {
                System.out.println("Got data from client: " + data);

                // check if ack requested by client,
                if (ackRequest.isAckRequested()) {
                    // send ack response with data to client
                    ackRequest.sendAckData("client message was delivered to server!", "yeah!");
                }

                // send message back to client with ack callback WITH data
                String nigga = "Lol";
                client.sendEvent("ackevent2", new AckCallback<String>(String.class) {
                    @Override
                    public void onSuccess(String result) {
                        System.out.println("ack from client: " + client.getSessionId() + " data: " + result);
                    }
                }, nigga);
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
