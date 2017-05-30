package com.i2lp.edi.client;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.server.packets.InteractiveElement;
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

    private ArrayList<InteractiveElement> interactiveElementsToRespond;
    private Date startDate, endDate;

    public StudentSession(EdiManager ediManager){
        this.ediManager = ediManager;
        this.activePresentation = ediManager.getPresentationManager().getPresentationElement();

        logger.info("Connected to live presentation at " + (startDate = new Date()).toString());

        //Go active in the current presentation
        goActiveInPresentation();
        //Go to current slide of teacher
        synchroniseWithTeacher();
    }

    //Go active in the current presentation
    private void goActiveInPresentation(){
        ediManager.getSocketClient().setUserActivePresentation(ediManager.getPresentationManager().getPresentationElement().getPresentationMetadata().getPresentationID(), ediManager.getUserData().getUserID());
    }


    //Go to current slide of teacher
    private void synchroniseWithTeacher(){
        Integer[] current_slide_states = ediManager.getSocketClient().getCurrentSlideForPresentation(ediManager.getPresentationManager().getPresentationElement().getPresentationMetadata().getPresentationID());
        ediManager.getPresentationManager().goToSlideElement(current_slide_states);
    }

    //TODO: Do other session termination stuff
    public void endSession(){
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

        } else {
            isLinked = true;
            Image lockIcon = new Image("file:projectResources/icons/lock.png", 30, 30, true, true);
            ediManager.getPresentationManager().linkButton.setImage(lockIcon);
            synchroniseWithTeacher();
        }
    }

    public void addQuestionToQueue(String question){
        if(ediManager.getSocketClient().addQuestionToQuestionQueue(ediManager.getUserData().getUserID(), activePresentation.getPresentationMetadata().getPresentationID(), question, ediManager.getPresentationManager().getCurrentSlideNumber())){
            logger.info("Question successfully submitted");
        } else {
            logger.error("Question did not submit successfully");
        }
    }

    public void setInteractiveElementsToRespond(ArrayList<InteractiveElement> interactiveElementsToRespond) {
        this.interactiveElementsToRespond = interactiveElementsToRespond;
        for(InteractiveElement interactiveElement : interactiveElementsToRespond){
            if(interactiveElement.isLive()) logger.info("Interactive Element: " + interactiveElement.getInteractive_element_id() + " of type: " + interactiveElement.getType() + " is now live." + "You have " + interactiveElement.getResponse_interval() + " to respond.");
        }
    }

    public boolean isLinked() {
        return isLinked;
    }
}
