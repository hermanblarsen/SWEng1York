package com.i2lp.edi.client;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.presentationElements.InteractiveElement;
import com.i2lp.edi.client.presentationElements.PollElement;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.presentationElements.WordCloudElement;
import com.i2lp.edi.server.packets.InteractiveElementRecord;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

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
    private ArrayList<InteractiveElement> interactiveElementsInPresentation; //Needed to link records to InteractiveElements in presentation
    private Date startDate, endDate;

    public StudentSession(EdiManager ediManager) {
        this.ediManager = ediManager;
        this.activePresentation = ediManager.getPresentationManager().getPresentationElement();
        this.interactiveElementsInPresentation = ediManager.getPresentationManager().getInteractiveElementList();

        logger.info("Connected to live presentation  at " + (startDate = new Date()).toString());

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
                teacherLeft();
            } else if ((ediManager.getPresentationManager().getCurrentSlideNumber() != current_slide_states[0]) || (ediManager.getPresentationManager().getPresentationElement().getSlide(current_slide_states[0]).getCurrentSequenceNumber() != current_slide_states[1])) {
                Platform.runLater(() -> {
                    //If the current slide number or sequence number has changed, move to it
                    ediManager.getPresentationManager().goToSlideElement(current_slide_states);
                });
            }
        }
    }

    //TODO: Do other session termination stuff
    public void endSession() {
        sendUserStatistics();
        //Set active presentation for user to null (no active presentation)
        ediManager.getSocketClient().setUserActivePresentation(0, ediManager.getUserData().getUserID());

        endDate = new Date();
        logger.info("Live Presentation session ending. Presentation lasted " + (int) ((endDate.getTime() - startDate.getTime()) / 1000) + " seconds.");
    }

    public void setPresentationLink(boolean setLink) {
        if (setLink) {
            isLinked = true;
            Image lockIcon = new Image("file:projectResources/icons/lock.png", 30, 30, true, true);
            ediManager.getPresentationManager().linkButton.setImage(lockIcon);
            synchroniseWithTeacher();
        } else {
            isLinked = false;
            Image unlockIcon = new Image("file:projectResources/icons/unlock.png", 30, 30, true, true);
            ediManager.getPresentationManager().linkButton.setImage(unlockIcon);
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
            if (interactiveElementRecord.isLive()) {
                for (InteractiveElement interactiveElement : interactiveElementsInPresentation) {
                    if (interactiveElement.getElementID() == interactiveElementRecord.getInteractive_pres_id()) {
                        logger.info("Interactive Element: " + interactiveElement.getElementID() + " of type: " + interactiveElementRecord.getType() + " is now live." + "You have " + interactiveElement.getTimeLimit() + " seconds to respond.");
                        if(interactiveElement instanceof WordCloudElement){
                            ((WordCloudElement) interactiveElement).setUpWordCloudData();
                        }
                        //Send test response
                        ediManager.getSocketClient().addInteractionToInteractiveElement(ediManager.getUserData().getUserID(), interactiveElementRecord.getInteractive_element_id(), generateString(new Random(), "TestStuffHere", 12));
                    }
                }
            }
        }
    }

    public static String generateString(Random rng, String characters, int length)
    {
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }

    public boolean isLinked() {
        return isLinked;
    }

    public void teacherLeft() {
        logger.info("Teacher has left the session.");
    }


    //TODO: Go through slide time data and add as interactions to database
    public void sendUserStatistics() {

    }
}

