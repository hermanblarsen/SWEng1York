package com.i2lp.edi.client;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.presentationElements.InteractiveElement;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.presentationElements.WordCloudElement;
import com.i2lp.edi.server.packets.InteractionRecord;
import com.i2lp.edi.server.packets.InteractiveElementRecord;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

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
    private ArrayList<Duration> slideTimes;//Amount of time spent on each slide.
    private Instant slideStart;//The time at which the current slide started.

    public StudentSession(EdiManager ediManager) {
        this.ediManager = ediManager;
        this.activePresentation = ediManager.getPresentationManager().getPresentationElement();
        this.interactiveElementsInPresentation = ediManager.getPresentationManager().getInteractiveElementList();

        logger.info("Connected to live presentation  at " + (startDate = new Date()).toString());

        //Go active in the current presentation
        goActiveInPresentation();
        addSlideTimeListener();
        //Go to current slide of teacher
        setPresentationLink(true);
    }

    //Go active in the current presentation
    private void goActiveInPresentation() {
        ediManager.getSocketClient().setUserActivePresentation(ediManager.getPresentationManager().getPresentationElement().getPresentationMetadata().getPresentationID(), ediManager.getUserData().getUserID());
    }


    //Go to current slide of teacher
    public void synchroniseWithTeacher() {
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

    //TODO: Do other session termination stuff
    public void endSession() {
        sendUserStatistics();
        //Set active presentation for user to null (no active presentation)
        ediManager.getSocketClient().setUserActivePresentation(0, ediManager.getUserData().getUserID());

        endDate = new Date();
        logger.info("Live Presentation session ending. Presentation lasted " + (int) ((endDate.getTime() - startDate.getTime()) / 1000) + " seconds.");

        //Submit the slide times to the DB:
        ediManager.getSocketClient().sendPresentationStatistics(ediManager.getPresentationManager().getPresentationElement().getPresentationMetadata().getPresentationID(), ediManager.getUserData().getUserID(), slideTimes);
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

        //Event for modification here
        for (InteractiveElementRecord interactiveElementRecord : interactiveElementsToRespondRecord) {
            if (interactiveElementRecord.isLive()) {
                for (InteractiveElement interactiveElement : interactiveElementsInPresentation) {
                    if (interactiveElement.getElementID() == interactiveElementRecord.getXml_element_id()) {
                        //Move to the slide of the teacher
                        synchroniseWithTeacher();
                        logger.info("Interactive Element: " + interactiveElement.getElementID() + " of type: " + interactiveElementRecord.getType() + " is now live." + "You have " + interactiveElement.getTimeLimit() + " seconds to respond.");


                        if (interactiveElement instanceof WordCloudElement) {
                            ((WordCloudElement) interactiveElement).setUpWordCloudData();
                        }

                        //Start timer of response interval, in which to set Interactive element non live
                        Timer responseWindow = new Timer();
                        responseWindow.schedule(new TimerTask() {
                            @SuppressWarnings("Duplicates")
                            @Override
                            public void run() {
                                ArrayList<InteractionRecord> interactionsFromStudents = ediManager.getSocketClient().getInteractionsForPresentation(ediManager.getPresentationManager().getPresentationElement().getPresentationMetadata().getPresentationID());
                                ArrayList<String> elementInteractions = new ArrayList<>();

                                //Find Interactions that belong to this current interactive element
                                if (!interactionsFromStudents.isEmpty()) {
                                    for (InteractionRecord interactionRecord : interactionsFromStudents) {
                                        if (interactionRecord.getInteractive_element_id() == interactiveElementRecord.getInteractive_element_id()) {
                                            elementInteractions.add(interactionRecord.getInteraction_data());
                                        }
                                    }
                                    //If its a WordCloud, set the wordList
                                    if (interactiveElement instanceof WordCloudElement) {
                                        ((WordCloudElement) interactiveElement).setWordList(elementInteractions);
                                        Platform.runLater(() -> {
                                            ((WordCloudElement) interactiveElement).generateWordCloud();
                                        });
                                    }
                                } else {
                                    logger.error("No interactions received for Interactive Element: " + interactiveElement.getElementID());
                                }
                            }
                        }, interactiveElement.getTimeLimit() * 1000);
                    }
                }
            }
        }
    }

    public void sendResponse(InteractiveElement elementForResponse, String data) {
        //Search for interactive element to respond to
        for (InteractiveElementRecord interactiveElementRecord : interactiveElementsToRespondRecord) {
            if (elementForResponse.getElementID() == interactiveElementRecord.getXml_element_id()) {
                if (elementForResponse.getElementID() == interactiveElementRecord.getXml_element_id()) {
                    logger.info("Interactive Element: " + elementForResponse.getElementID() + " of type: " + interactiveElementRecord.getType() + " is now live." + "You have " + elementForResponse.getTimeLimit() + " seconds to respond.");
                    if (elementForResponse instanceof WordCloudElement) {
                        //Send test response
                        ediManager.getSocketClient().addInteractionToInteractiveElement(ediManager.getUserData().getUserID(), interactiveElementRecord.getInteractive_element_id(), data);
                    }
                }
            }
        }
    }

    public static String generateString(Random rng, String characters, int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
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


    private void addSlideTimeListener() {
        //Initialise the array with eough space for all of the slides. and set the time on ach slide to 0
        slideTimes = new ArrayList<>();
        int numberOfSlides = ediManager.getPresentationManager().getPresentationElement().getMaxSlideNumber();
        for (int i = 0; i <= numberOfSlides; i++) {
            slideTimes.add(Duration.ZERO);
        }

        ediManager.getPresentationManager().addSlideChangeListener((prevVal, newVal) -> {
            int previousSlideNumber = (int) prevVal;
            int newSlideNumber = (int) newVal;

            if (previousSlideNumber != -1) {//If not at the beginning of the presentation
                Duration timeOnPrevSlide = Duration.between(slideStart, Instant.now());
                slideStart = Instant.now();

                //Otherwise if we have been on this slide befre then add to the tiem already spent on it
                slideTimes.set(previousSlideNumber, slideTimes.get(previousSlideNumber).plus(timeOnPrevSlide));//Add the time on the previous slie to its stored value.
                logger.info("Time spent on slide " + (previousSlideNumber + 1) + ": " + timeOnPrevSlide.getSeconds() + "s Total time: " + slideTimes.get(previousSlideNumber).getSeconds() + "s");
            } else {
                slideStart = Instant.now();
                logger.info("First slide started at: " + slideStart.toString());
            }
        });
    }
}

