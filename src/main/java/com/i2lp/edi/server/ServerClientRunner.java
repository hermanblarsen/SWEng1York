package com.i2lp.edi.server;

/**
 * Created by amriksadhra on 20/03/2017.
 */
public class ServerClientRunner {
    public static void main(String[] args){
        SocketServer mySocketServer = new SocketServer("db.amriksadhra.com",8080);

        SocketClient mySocketClient = new SocketClient("db.amriksadhra.com", 8080);
    }
}
