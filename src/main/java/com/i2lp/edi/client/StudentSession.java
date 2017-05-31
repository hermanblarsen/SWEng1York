package com.i2lp.edi.client;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.server.packets.InteractiveElementRecord;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by amriksadhra on 30/05/2017.
 */
public class StudentSession {
    private Logger logger = LoggerFactory.getLogger(StudentSession.class);
    private EdiManager ediManager;
    private Presentation activePresentation;

    //Configuration variables for live session
    private boolean isLinked = false;

    private ArrayList<InteractiveElementRecord> interactiveElementsToRespondRecord;
    private Date startDate, endDate;

    public StudentSession(EdiManager ediManager) {
        this.ediManager = ediManager;
        this.activePresentation = ediManager.getPresentationManager().getPresentationElement();

        logger.info("Connected to live presentation at " + (startDate = new Date()).toString());

        //Go active in the current presentation
        goActiveInPresentation();
        //Go to current slide of teacher
        setPresentationLink(true);
    }

    //Go active in the current presentation
    private void goActiveInPresentation() {
        ediManager.getSocketClient().setUserActivePresentation(ediManager.getPresentationManager().getPresentationElement().getPresentationMetadata().getPresentationID(), ediManager.getUserData().getUserID());
    }


    //Go to current slide of teacher
    public void synchroniseWithTeacher() {
        if (isLinked) {
            Integer[] current_slide_states = ediManager.getSocketClient().getCurrentSlideForPresentation(ediManager.getPresentationManager().getPresentationElement().getPresentationMetadata().getPresentationID());
            if ((current_slide_states[0] == -1) && (current_slide_states[1] == -1)) {
                ediManager.getPresentationManager().getStudentSession().teacherLeft();
            } else if ((ediManager.getPresentationManager().getCurrentSlideNumber() != current_slide_states[0]) || (ediManager.getPresentationManager().getPresentationElement().getSlide(current_slide_states[0]).getCurrentSequenceNumber() != current_slide_states[1])) {
                Platform.runLater(() ->{
                    //If the current slide number or sequence number has changed, move to it
                    ediManager.getPresentationManager().goToSlideElement(current_slide_states);
                });
            }
        }
    }

    //TODO: Do other session termination stuff
    public void endSession() {
        //Set active presentation for user to null (no active presentation)
        ediManager.getSocketClient().setUserActivePresentation(0, ediManager.getUserData().getUserID());

        endDate = new Date();
        logger.info("Live Presentation session ending. Presentation lasted " + (int) ((endDate.getTime() - startDate.getTime()) / 1000) + " seconds.");
    }

    public void setPresentationLink(boolean setLink) {
        if (setLink) {
            isLinked = false;
            Image unlockIcon = new Image("file:projectResources/icons/unlock.png", 30, 30, true, true);
            ediManager.getPresentationManager().linkButton.setImage(unlockIcon);
            synchroniseWithTeacher();
        } else {
            isLinked = true;
            Image lockIcon = new Image("file:projectResources/icons/lock.png", 30, 30, true, true);
            ediManager.getPresentationManager().linkButton.setImage(lockIcon);
            synchroniseWithTeacher();
        }
    }

    public void addQuestionToQueue(String question) {
        if (ediManager.getSocketClient().addQuestionToQuestionQueue(ediManager.getUserData().getUserID(), activePresentation.getPresentationMetadata().getPresentationID(), question, ediManager.getPresentationManager().getCurrentSlideNumber())) {
            logger.info("Question successfully submitted");
        } else {
            logger.error("Question did not submit successfully");
        }
    }

    public void setInteractiveElementsToRespondRecord(ArrayList<InteractiveElementRecord> interactiveElementsToRespondRecord) {
        this.interactiveElementsToRespondRecord = interactiveElementsToRespondRecord;
        for (InteractiveElementRecord interactiveElementRecord : interactiveElementsToRespondRecord) {
            if (interactiveElementRecord.isLive())
                logger.info("Interactive Element: " + interactiveElementRecord.getInteractive_element_id() + " of type: " + interactiveElementRecord.getType() + " is now live." + "You have " + interactiveElementRecord.getResponse_interval() + " to respond.");
        }
    }

    public boolean isLinked() {
        return isLinked;
    }

    public void teacherLeft() {
        logger.info("Teacher has left the session.");
    }
}

