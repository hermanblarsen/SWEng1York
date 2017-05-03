package com.i2lp.edi.client.managers;

import com.i2lp.edi.server.SocketClient;
import com.i2lp.edi.server.packets.PresentationMetadata;

import java.util.ArrayList;

import static com.i2lp.edi.client.Constants.TEMP_DIR_PATH;
import static com.i2lp.edi.client.utilities.Utils.getFilesInFolder;

/**
 * Created by amriksadhra on 03/05/2017.
 */
public class PresentationManager {
    SocketClient socketClient;

    public PresentationManager(SocketClient socketClient){
        /* Get ability to talk to server */
        this.socketClient = socketClient;

        getLocalPresentationList();
        getRemotePresentationList();

    }

    private ArrayList<String> getLocalPresentationList(){
        //File baseFolder = new File();
        //File[] listOfFiles = baseFolder.listFiles();

        return getFilesInFolder(TEMP_DIR_PATH + "Presentations/");
    }

    private ArrayList<PresentationMetadata> getRemotePresentationList(){
        return new ArrayList<>();
    }




}
