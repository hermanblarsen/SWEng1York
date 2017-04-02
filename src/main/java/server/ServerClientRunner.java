package server;

import server.packets.User;

/**
 * Created by amriksadhra on 20/03/2017.
 */
public class ServerClientRunner {
    public static void main(String[] args){
        socketServer mySocketServer = new socketServer("db.amriksadhra.com",8080);

        socketClient mySocketClient = new socketClient("db.amriksadhra.com", 8080);
    }
}
