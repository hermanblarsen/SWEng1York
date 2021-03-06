package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.ModuleFolder;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.utilities.ParserXML;
import com.i2lp.edi.client.utilities.Utilities;
import com.i2lp.edi.client.utilities.ZipUtils;
import com.i2lp.edi.server.SocketClient;
import com.i2lp.edi.server.packets.Module;
import com.i2lp.edi.server.packets.PresentationMetadata;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.maven.shared.utils.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

import static com.i2lp.edi.client.Constants.*;
import static com.i2lp.edi.client.utilities.Utilities.getFilesInFolder;


/**
 * Created by amriksadhra on 03/05/2017.
 */

/**
 * Presentation Library Manager, managing the presentations the user has access to
 * in his/her library.
 */
public class PresentationLibraryManager {
    private SocketClient socketClient;
    private EdiManager ediManager;

    private Logger logger = LoggerFactory.getLogger(PresentationLibraryManager.class);

    private ArrayList<ModuleFolder> localPresentationListModuleFolder; //Stores locally available DocumentIDs
    private ArrayList<ModuleFolder> remotePresentationListModuleFolder; //Stores server DocumentIDs
    private ArrayList<Module> userModuleList;
    private ArrayList<PresentationMetadata> localPresentationList; //Stores PresentationMetadata locally for current user
    private ArrayList<PresentationMetadata> remotePresentationList; //Stores PresentationMetadata on server for current user

    @SuppressWarnings("unchecked")
    public PresentationLibraryManager(EdiManager ediManager) {
        /* Get ability to talk to server and main program */
        this.ediManager = ediManager;
        this.socketClient = ediManager.getSocketClient();

        updatePresentations();
    }


    /**
     * Get list of modules that has been retrieved by server
     *
     * @return List of modules
     */
    public ArrayList<Module> getUserModuleList() {
        logger.info("--- User Registered Modules ---");
        for (Module module : userModuleList) {
            logger.info("ID: " + module.getModule_id() + " Subject: " + module.getSubjectName());
        }
        return userModuleList;
    }

    /**
     * Update local presentation list with all available on server. Called whenever a presentation is added or goes live.
     */
    @SuppressWarnings("unchecked")
    public void updatePresentations() {
        //Update list of modules for User for UI
        userModuleList = ediManager.getSocketClient().getModulesForUser(ediManager.getUserData().getUserID());
        //Work out what we presentations are available locally, what are available remotely.
        localPresentationListModuleFolder = getLocalPresentationListModuleFolder();
        remotePresentationList = getRemotePresentationList();
        remotePresentationListModuleFolder = getRemotePresentationStringList(remotePresentationList); //Get strings of documentIds to work out missing presentations

        //Get difference between server thumbnails and client thumbnails
        ArrayList<ModuleFolder> difference = new ArrayList<>();

        boolean exists = false;

        for (ModuleFolder moduleFolderRemote : remotePresentationListModuleFolder) {
            for (ModuleFolder moduleFolderLocal : localPresentationListModuleFolder) {
                if (moduleFolderLocal.getModuleName().equals(moduleFolderRemote.getModuleName())) { //If we have the module folder
                    exists = true;
                    ArrayList<String> differenceInModule = moduleFolderRemote.getPresentations();
                    differenceInModule.removeAll(moduleFolderLocal.getPresentations());
                    if (!differenceInModule.isEmpty()) {
                        difference.add(new ModuleFolder(moduleFolderLocal.getModuleName(), differenceInModule));
                    }
                } else {
                    exists = false;
                }
            }
            if (!exists) {
                difference.add(moduleFolderRemote);
            }
        }

        ArrayList<PresentationMetadata> downloadList = new ArrayList<>();

        for (ModuleFolder moduleFolder : difference) {
            for (String presentation : moduleFolder.getPresentations()) {
                for (PresentationMetadata remotePresentation : remotePresentationList) {//Search remote list for missing documentID
                    if ((remotePresentation.getDocumentID().equals(presentation)) && (remotePresentation.getModuleName().equals(moduleFolder.getModuleName()))) {
                        downloadList.add(remotePresentation); //Add missing presentation to download list
                    }
                }
            }
        }

        // If no difference between client and server, don't download anything
        if (downloadList.size() == 0) {
            updateLocalData();
            return;
        } else {
            downloadMissingPresentations(downloadList);
            updateLocalPresentationList(remotePresentationList);//Metadata now matches that available on server after download, so set.

            updateLocalData();
        }
    }

    /**
     * Make local data match remote data after sync, and update UI.
     */
    private void updateLocalData() {
        updateLocalPresentationList(remotePresentationList); //Metadata matches that available on server, so set.
        //If dashboard created, update the dashboard UI
        if (ediManager.getDashboard() != null) {
            Platform.runLater(() -> ediManager.getDashboard().updateAvailablePresentations());
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
        ediManager.getLoadingScreen().goToPresDownloadingState();
        ediManager.getLoadingScreen().setNumOfMissingPres(downloadList.size());

        File tempDir = new File(TEMP_PATH);
        File presDir = new File(PRESENTATIONS_PATH);

        //Ensure temporary directory exists
        if (!tempDir.exists()) tempDir.mkdirs(); //Create directory structure if not present yet
        if (!presDir.exists()) presDir.mkdirs();

        int i = 1;
        for (PresentationMetadata toDownload : downloadList) {
            logger.info("Downloading presentation from " + toDownload.getXml_url());
            ediManager.getLoadingScreen().updateDownloadState(i);
            try {
                URL website = new URL(toDownload.getXml_url());
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(tempDir.getAbsolutePath() + File.separator + toDownload.getDocumentID() + ".zip");
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
                rbc.close();
            } catch (IOException e) {
                logger.error("Unable to download presentation " + toDownload.getDocumentID() + "!");
                return;
            }
            logger.info("Unzipping " + toDownload.getXml_url() + ", " + i + " / " + downloadList.size());

            ZipUtils.unzipPresentation(tempDir.getAbsolutePath() + File.separator + toDownload.getDocumentID() + ".zip", presDir.getAbsolutePath() + File.separator + toDownload.getModuleName() + File.separator + toDownload.getDocumentID());
            i++;
        }
        ediManager.getLoadingScreen().exitPresDownloadingState();
    }

    private String getModuleNameForPresentation(PresentationMetadata toRetrieveModuleName) {
        for (Module module : userModuleList) {
            if (toRetrieveModuleName.getModule_id() == module.getModule_id()) {
                return module.getModule_name();
            }
        }

        logger.error("Unable to find module name for Presentation with ID: " + toRetrieveModuleName.getPresentationID());
        return "MISSING";
    }

    private ArrayList<PresentationMetadata> getRemotePresentationList() {
        ArrayList<PresentationMetadata> remotePresentationList = socketClient.getPresentationsForUser(ediManager.getUserData().getUserID());
        for (PresentationMetadata presentation : remotePresentationList) {
            presentation.setModuleName(getModuleNameForPresentation(presentation));
        }

        return remotePresentationList;
    }

    private ArrayList<ModuleFolder> getLocalPresentationListModuleFolder() {
        ArrayList<ModuleFolder> localListOfPresentations = new ArrayList<>();

        //Get list of Modules
        ArrayList<String> moduleFolders = getFilesInFolder(PRESENTATIONS_PATH);

        //For every module, get presentations inside
        for (String moduleFolder : moduleFolders) {
            localListOfPresentations.add(new ModuleFolder(moduleFolder, getFilesInFolder(PRESENTATIONS_PATH + File.separator + moduleFolder)));
        }

        //Assume folder names are DocumentID for now, else have to fire up parser to get them
        return localListOfPresentations;
    }

    private ArrayList<ModuleFolder> getRemotePresentationStringList(ArrayList<PresentationMetadata> remotePresentationList) {
        ArrayList<ModuleFolder> remotePresentationDocumentIDs = new ArrayList<>();

        for (Module module : userModuleList) {

            if (!getRemotePresentationsWithModuleName(remotePresentationList, module).isEmpty()) {
                remotePresentationDocumentIDs.add(getRemotePresentationsWithModuleName(remotePresentationList, module));
            }
        }

        return remotePresentationDocumentIDs;
    }

    /**
     * Get module folder for remote presentations
     * @param remotePresentationList list of presentations
     * @param module module of presentation
     * @return module folder of presentations the user doesn't have
     */
    public ModuleFolder getRemotePresentationsWithModuleName(ArrayList<PresentationMetadata> remotePresentationList, Module module) {
        ModuleFolder remotePresentationsWithModuleName = new ModuleFolder(module.getModule_name());

        for (PresentationMetadata presentation : remotePresentationList) {
            if (presentation.getModuleName().equals(module.getModule_name()))
                remotePresentationsWithModuleName.addPresentation(presentation.getDocumentID());
        }

        return remotePresentationsWithModuleName;
    }

    /**
     * Remove Presentation from database
     * @param presentationID presentaton ID to remove
     * @return
     */
    public boolean removePresentation(int presentationID) {
        boolean status = false;
        String return_status;
        return_status = socketClient.removePresentationFromModule(presentationID);

        if (return_status.contains("success")) status = true;

        return status;
    }

    /**
     * Upload a presentation to the server tied to a specific module ID
     * @param fileToUpload String of file to upload
     * @param moduleID module ID to tie presentation to
     */
    public void uploadPresentation(String fileToUpload, int moduleID) {
        ParserXML parserXML = null;
        try {
            parserXML = new ParserXML(fileToUpload);
        } catch (FileNotFoundException e) {
            logger.error("XML file not found: " + fileToUpload);
        }

        Presentation presentation = parserXML.parsePresentation();
        //Generate thumbnails for Slides.
        ThumbnailGenerationManager.generateSlideThumbnails(presentation, false);

        try {
            FileUtils.copyFile(new File(fileToUpload), new File(TEMP_PATH + presentation.getDocumentID() + File.separator + presentation.getDocumentID() + ".xml"));
        } catch (IOException e) {
            logger.error("Unable to copy XML file into local temporary directory.", e);
        }
        final String zipPath = TEMP_PATH + presentation.getDocumentID() + ".zip";

        //Create zip after thumbnail and CSS generation are done-
        Task zipCreationTask = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    Thread.sleep(10000); //Wait for thumbnails to be generated TODO: replace with checker for numFiles (y)
                    new ZipUtils(TEMP_PATH + presentation.getDocumentID(), zipPath);
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

                String remoteFile = "Uploads/" + presentation.getDocumentID() + ".zip";
                InputStream inputStream = new FileInputStream(localFile);
                logger.info("Start uploading " + presentation.getDocumentID() + " data");

                boolean done = ftpClient.storeFile(remoteFile, inputStream);
                inputStream.close();
                if (done) {
                    logger.info("The presentation has uploaded successfully. Awaiting server-side processing.");
                    socketClient.alertServerToUpload(presentation.getDocumentID(), moduleID, presentation); //Tell server a new file has arrived
                    new File(zipPath).delete(); //Clean up zip after upload
                    Utilities.deleteDirectory(new File(TEMP_PATH + presentation.getDocumentID()));
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
        zipCreationTask.setOnSucceeded(event -> uploadThread.start());
    }
}
