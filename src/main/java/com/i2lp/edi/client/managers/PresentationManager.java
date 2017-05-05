package com.i2lp.edi.client.managers;

import com.i2lp.edi.server.SocketClient;
import com.i2lp.edi.server.packets.PresentationMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.i2lp.edi.client.Constants.*;
import static com.i2lp.edi.client.utilities.Utils.getFilesInFolder;

/**
 * Created by amriksadhra on 03/05/2017.
 */
public class PresentationManager {
    private SocketClient socketClient;
    private EdiManager ediManager;

    private Logger logger = LoggerFactory.getLogger(PresentationManager.class);

    private ArrayList<String> localPresentationListString; //Stores locally available DocumentIDs
    private ArrayList<String> remotePresentationListString; //Stores server DocumentIDs
    private ArrayList<PresentationMetadata> localPresentationList; //Stores PresentationMetadata locally for current user
    private ArrayList<PresentationMetadata> remotePresentationList; //Stores PresentationMetadata on server for current user

    @SuppressWarnings("unchecked")
    public PresentationManager(EdiManager ediManager) {
        /* Get ability to talk to server and main program */
        this.ediManager = ediManager;
        this.socketClient = ediManager.mySocketClient;

        //Work out what we presentations are available locally, what are available remotely.
        localPresentationListString = getLocalPresentationListString();
        remotePresentationList = getRemotePresentationList();
        remotePresentationListString = getRemotePresentationStringList(remotePresentationList); //Get strings of documentIds to work out missing presentations

        //Get difference between server thumbnails and client thumbnails
        List difference = new ArrayList(remotePresentationListString);
        difference.removeAll(localPresentationListString);

        // If no difference between client and server, don't download anything
        if (difference.size() == 0) {
            updateLocalPresentationList(remotePresentationList); //Metadata matches that available on server, so set.
            return;
        } else {
            ArrayList<PresentationMetadata> downloadList = new ArrayList<>();


            for (String missingPresentationDocumentID : (ArrayList<String>) difference) {
                for (int i = 0; i < remotePresentationList.size(); i++) {//Search remote list for missing documentID
                    if (remotePresentationList.get(i).getDocumentID().equals(missingPresentationDocumentID)) {
                        downloadList.add(remotePresentationList.get(i)); //Add missing presentation to download list
                    }
                }
            }
            downloadMissingPresentations(downloadList);
            updateLocalPresentationList(remotePresentationList);//Metadata now matches that available on server after download, so set.
        }
    }

    /**
     * Helper method to allow UI code/Presentation management code to update local view of presentations
     *
     * @return List of locally available presentations, with all metadata
     * @author Amrik Sadhra
     */
    public ArrayList<PresentationMetadata> getLocalPresentationList() {
        return localPresentationList;
    }

    /**
     * Helper method to update local list of presentation metadata to remote list contents.
     *
     * @param remoteList Server side presentation metadata list
     */
    private void updateLocalPresentationList(ArrayList<PresentationMetadata> remoteList) {
        this.localPresentationList = remoteList;
    }

    /**
     * Downloads presentation Zip files from Edi Server and stores to Temporary directory for extraction and processing
     *
     * @param downloadList Presentations to download
     * @author Amrik Sadhra
     */
    private void downloadMissingPresentations(ArrayList<PresentationMetadata> downloadList) {
        logger.info("Edi is missing " + downloadList.size() + " presentations. Attempting to download.");

        File tempDir = new File(TEMP_PATH);
        File presDir = new File(PRESENTATIONS_PATH);

        //Ensure temporary directory exists
        if (!tempDir.exists()) tempDir.mkdirs(); //Create directory structure if not present yet
        if (!presDir.exists()) presDir.mkdirs();

        for (PresentationMetadata toDownload : downloadList) {
            logger.info("Downloading presentation from " + toDownload.getXml_url());
            try {
                URL website = new URL(toDownload.getXml_url());
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(tempDir.getAbsolutePath() + File.separator + toDownload.getDocumentID() + ".zip");
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (IOException e) {
                logger.error("Unable to download presentation " + toDownload.getDocumentID() + "!");
                return;
            }
            unzipPresentation(tempDir.getAbsolutePath() + File.separator + toDownload.getDocumentID() + ".zip", presDir.getAbsolutePath() + File.separator + toDownload.getDocumentID());
        }
    }

    /**
     * Unzip downloaded zip file to Presentations folder, and then delete the downloaded zip
     *
     * @param zipFile      Input zip file to extract
     * @param outputFolder Output location of files
     * @author Amrik Sadhra, https://www.mkyong.com/
     */
    public void unzipPresentation(String zipFile, String outputFolder) {
        byte[] buffer = new byte[1024];
        try {
            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                if(!newFile.toString().contains(".")){
                    newFile.mkdir();
                    ze = zis.getNextEntry();
                    continue;
                }

                logger.info("Unzipping " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //Cleanup downloaded zip file by deleting
        new File(zipFile).delete();
    }

    private ArrayList<String> getLocalPresentationListString() {
        //Assume folder names are DocumentID for now, else have to fire up parser to get them
        return getFilesInFolder(BASE_PATH + "Presentations/");
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
