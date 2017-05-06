package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.utilities.ZipUtils;
import com.i2lp.edi.server.SocketClient;
import com.i2lp.edi.server.packets.PresentationMetadata;
import javafx.concurrent.Task;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.i2lp.edi.client.Constants.*;
import static com.i2lp.edi.client.utilities.Utils.getFilesInFolder;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


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
            ZipUtils.unzipPresentation(tempDir.getAbsolutePath() + File.separator + toDownload.getDocumentID() + ".zip", presDir.getAbsolutePath() + File.separator + toDownload.getDocumentID());
        }
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


    public void uploadPresentation(String fileToUpload, String filename, int moduleID) {
        //Generate thumbnails for Slides.
        ThumbnailGenerationController.generateSlideThumbnails(fileToUpload);
        try {
            Files.copy(Paths.get(fileToUpload), Paths.get(PRESENTATIONS_PATH + filename + File.separator + filename + ".xml"), REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Unable to copy XML file into local presentation library.");
        }
        final String zipPath = TEMP_PATH + filename + ".zip";

        //Create zip after thumbnail and CSS generation are done
        Task zipCreationTask = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    Thread.sleep(1000); //Wait for thumbnails to be generated TODO: replace with checker for numFiles
                    new ZipUtils(PRESENTATIONS_PATH + filename, zipPath);
                    return null;
                } catch (InterruptedException e) {
                    logger.error("Unable to sleep on Zip generation thread.");
                }
                return 1;
            }
        };
        Thread zipThread = new Thread(zipCreationTask);
        zipThread.start();

        Thread uploadThread = new Thread(() -> { //Make upload async to avoid blocking main thread
            FTPClient ftpClient = new FTPClient();
            try {
                ftpClient.connect("ftp.amriksadhra.com", 21);
                ftpClient.login(FTP_USER, FTP_PASS);
                ftpClient.enterLocalPassiveMode();

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                //Upload file using an InputStream
                File localFile = new File(zipPath);

                String remoteFile = "Uploads/" + filename + ".zip";
                InputStream inputStream = new FileInputStream(localFile);
                logger.info("Start uploading " + filename + " data");

                boolean done = ftpClient.storeFile(remoteFile, inputStream);
                inputStream.close();
                if (done) {
                    logger.info("The presentation has uploaded successfully. Awaiting server-side processing.");
                    socketClient.alertServerToUpload(filename, moduleID);
                    new File(zipPath).delete(); //Clean up zip after upload
                }
            } catch (IOException e) {
                logger.error("Error uploading presentation data to Edi Server! ", e);
            } finally {
                try {
                    if (ftpClient.isConnected()) {
                        ftpClient.logout();
                        ftpClient.disconnect();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        zipCreationTask.setOnSucceeded(event -> {
            uploadThread.start();
        });
    }
}
