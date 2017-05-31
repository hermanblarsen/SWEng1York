package com.i2lp.edi.client;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.presentationElements.InteractiveElement;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.server.packets.InteractiveElementRecord;
import com.i2lp.edi.server.packets.Question;
import com.i2lp.edi.server.packets.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by amriksadhra on 27/05/2017.
 */
public class PresentationSession {
    private Logger logger = LoggerFactory.getLogger(PresentationSession.class);
    private EdiManager ediManager;
    private Presentation activePresentation;

    private PresentationStatistics presentationStatistics;

    private ArrayList<InteractiveElementRecord> interactiveElementRecords;
    private ArrayList<InteractiveElement> interactiveElementsInPresentation; //Needed to link records to InteractiveElements in presentation

    private ArrayList<Question> questionQueue;
    private ArrayList<User> activeUsers;

    //Configuration Variables for session


    private Date startDate;
    private Date endDate;

    public PresentationSession(EdiManager ediManager) {
        this.ediManager = ediManager;
        this.activePresentation = ediManager.getPresentationManager().getPresentationElement();
        this.interactiveElementsInPresentation = ediManager.getPresentationManager().getInteractiveElementList();

        //Update database with slide number and start sequence of 0
        ediManager.getSocketClient().setCurrentSlideAndSequenceForPresentation(activePresentation.getPresentationMetadata().getPresentationID(), 0, 0);
        //Get Interactive Elements
        interactiveElementRecords = ediManager.getSocketClient().getInteractiveElementsForPresentation(activePresentation.getPresentationMetadata().getPresentationID());
        //Update Question Queue
        questionQueue = ediManager.getSocketClient().getQuestionsForPresentation(activePresentation.getPresentationMetadata().getPresentationID());

        logger.info("Live Presentation Session beginning at " + (startDate = new Date()).toString());
    }

    public void beginInteraction(int interactiveElementID, boolean isLive) {
        InteractiveElementRecord liveElement = null;

        //Find interactive element with correct ID
        for (InteractiveElementRecord interactiveElementRecord : interactiveElementRecords) {
            if (interactiveElementRecord.getInteractive_element_id() == interactiveElementID) {
                liveElement = interactiveElementRecord;
                break;
            }
        }

        logger.info("Response time is: " + liveElement.getResponse_interval().getTime());
        ediManager.getSocketClient().setInteractiveElementLive(activePresentation.getPresentationMetadata().getPresentationID(), liveElement.getInteractive_element_id(), isLive);
        //Start timer of response interval, in which to set Interactive element non live
        Timer responseWindow = new Timer();
        InteractiveElementRecord finalLiveElement = liveElement;
        responseWindow.schedule(new TimerTask() {
            @Override
            public void run() {
                ediManager.getSocketClient().setInteractiveElementLive(activePresentation.getPresentationMetadata().getPresentationID(), finalLiveElement.getInteractive_element_id(), false);
                logger.info("Interactive element response window closed");
            }
        }, finalLiveElement.getResponse_interval().getTime());
    }

    public void synchroniseSlides() {
        ediManager.getSocketClient().setCurrentSlideAndSequenceForPresentation(activePresentation.getPresentationMetadata().getPresentationID(), ediManager.getPresentationManager().getCurrentSlideNumber(), activePresentation.getCurrentSlide().getCurrentSequenceNumber());
    }

    public void endSession() {
        gatherUserStatistics();

        endDate = new Date();
        logger.info("Live Presentation session ending. Presentation lasted " + (int) ((endDate.getTime() - startDate.getTime()) / 1000) + " seconds.");

        //Update Presentation record to offline
        ediManager.getSocketClient().setPresentationLive(activePresentation.getPresentationMetadata().getPresentationID(), false);
        ediManager.getSocketClient().setCurrentSlideAndSequenceForPresentation(activePresentation.getPresentationMetadata().getPresentationID(), 0, 0);
    }

    private void gatherUserStatistics(){
        //Perform a master query over the Interactions table for a specific users interactions
        //Count Number of responses
        //Generate ArrayList of UserStatistics

        //For Presentation

    }

    public ArrayList<User> getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(ArrayList<User> activeUsers) {
        this.activeUsers = activeUsers;

        logger.info(activeUsers.size() + " Currently Active Users: ");

        for (User activeUser : activeUsers) {
            logger.info(activeUser.getFirstName());
        }
    }

    public ArrayList<Question> getQuestionQueue() {
        return questionQueue;
    }

    public void setQuestionQueue(ArrayList<Question> activeQuestions) {
        this.questionQueue = activeQuestions;
    }

    public void setInteractionDataForInteractiveElements(){

    }
}
