package com.i2lp.edi.client.managers;

import com.i2lp.edi.server.SocketClient;
import com.i2lp.edi.server.packets.PresentationMetadata;
import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
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

    private ArrayList<String> localPresentationList; //Stores locally available DocumentIDs
    private ArrayList<String> remotePresentationListString; //Stores server DocumentIDs
    private ArrayList<PresentationMetadata> remotePresentationList; //Stores PresentationMetadata on server for current user

    public PresentationManager(EdiManager ediManager) {
        /* Get ability to talk to server and main program */
        this.ediManager = ediManager;
        this.socketClient = ediManager.mySocketClient;

        localPresentationList = getLocalPresentationList();
        remotePresentationList = getRemotePresentationList();
        remotePresentationListString = getRemotePresentationStringList(remotePresentationList); //Get strings of documentIds to work out missing presentations

        // Use Apache commons library to get difference between server thumbnails and client thumbnails
        List difference = ListUtils.subtract(Arrays.asList(remotePresentationListString), Arrays.asList(localPresentationList));

        // If no difference between client and server, don't download anything
        if (difference.size() == 0) {
            return;
        } else {
            ArrayList<String> missingPresentationDocumentIDs = (ArrayList<String>) difference.get(0);
            ArrayList<PresentationMetadata> downloadList = new ArrayList<>();

            int i = 0;
            for(String missingPresentationDocumentID : missingPresentationDocumentIDs) {
                if(remotePresentationList.get(i).getDocumentID().equals(missingPresentationDocumentID)){
                    downloadList.add(remotePresentationList.get(i)); //Add missing presentation to download list
                }
                i++;
          }
          downloadMissingPresentations(downloadList);
        }
    }

    private void downloadMissingPresentations(ArrayList<PresentationMetadata> downloadList){
        logger.info("Edi is missing " + downloadList.size() + " presentations. Attempting to download.");
        URL url = null;

            for(PresentationMetadata toDownload : downloadList){
                logger.info("Downloading presentation from " + toDownload.getXml_url());

                //TODO: Ensure Temp folder exists before download operation
                try {
                    url = new URL(toDownload.getXml_url());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    InputStream in = connection.getInputStream();
                    FileOutputStream out = new FileOutputStream(TEMP_DIR_PATH + "/Temp/" + toDownload.getDocumentID() + ".zip");
                    copy(in, out, 1024);
                    out.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }


    public static void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int n = input.read(buf);
        while (n >= 0) {
            output.write(buf, 0, n);
            n = input.read(buf);
        }
        output.flush();
    }

    private ArrayList<String> getLocalPresentationList() {
        //Assume folder names are DocumentID for now, else have to fire up parser to get them
        return getFilesInFolder(TEMP_DIR_PATH + "Presentations/");
    }

    private ArrayList<PresentationMetadata> getRemotePresentationList() {
        return socketClient.getPresentationsForUser(ediManager.userData.getUserID());
    }

    private ArrayList<String> getRemotePresentationStringList(ArrayList<PresentationMetadata> remotePresentationList) {
        ArrayList<String> remotePresentationDocumentIDs = new ArrayList<>();

        for (PresentationMetadata toParse : remotePresentationList) {
            remotePresentationDocumentIDs.add(toParse.getDocumentID());
        }

        return remotePresentationDocumentIDs;
    }


}
