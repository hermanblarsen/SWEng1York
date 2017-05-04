package com.i2lp.edi.client.managers;

import com.i2lp.edi.server.SocketClient;
import com.i2lp.edi.server.packets.PresentationMetadata;
import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.i2lp.edi.client.Constants.TEMP_DIR_PATH;
import static com.i2lp.edi.client.utilities.Utils.getFilesInFolder;

/**
 * Created by amriksadhra on 03/05/2017.
 */
public class PresentationManager {
    private SocketClient socketClient;
    private EdiManager ediManager;

    private Logger logger = LoggerFactory.getLogger(PresentationManager.class);

    private ArrayList<String> localPresentationList;
    private ArrayList<String> remotePresentationList;

    public PresentationManager(EdiManager ediManager) {
        /* Get ability to talk to server and main program */
        this.ediManager = ediManager;
        this.socketClient = ediManager.mySocketClient;

        localPresentationList = getLocalPresentationList();
        remotePresentationList = getRemotePresentationStringList();

        // Use Apache commons library to get difference between server thumbnails and client thumbnails
        List difference = ListUtils.subtract(Arrays.asList(remotePresentationList), Arrays.asList(localPresentationList));

        // If no difference between client and server, dont send request data packet
        if (difference.size() == 0) {
            return;
        } else {
            logger.info("User is missing: ");
            difference.forEach(System.out::println); //Do something useful with this data
        }
    }

    private ArrayList<String> getLocalPresentationList() {
        //Assume folder names are DocumentID for now, else have to fire up parser to get them
        return getFilesInFolder(TEMP_DIR_PATH + "Presentations/");
    }

    private ArrayList<PresentationMetadata> getRemotePresentationList() {
        return socketClient.getPresentationsForUser(ediManager.userData.getUserID());
    }

    private ArrayList<String> getRemotePresentationStringList() {
        ArrayList<String> remotePresentationDocumentIDs = new ArrayList<>();

        for (PresentationMetadata toParse : getRemotePresentationList()) {
            remotePresentationDocumentIDs.add(toParse.getDocumentID());
        }

        return remotePresentationDocumentIDs;
    }


}
