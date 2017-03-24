package server;

/**
 * Created by amriksadhra on 20/03/2017.
 */
public class ServerClientRunner {
    public static void main(String[] args){
        socketServer mySocketServer = new socketServer("db.amriksadhra.com",8080);

        socketClient mySocketClient = new socketClient("127.0.0.1", 8080);
    }
}
