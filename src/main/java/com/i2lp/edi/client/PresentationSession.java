package com.i2lp.edi.client;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.client.managers.PresentationManagerTeacher;
import com.i2lp.edi.client.presentationElements.InteractiveElement;
import com.i2lp.edi.client.presentationElements.PollElement;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.presentationElements.WordCloudElement;
import com.i2lp.edi.server.packets.InteractionRecord;
import com.i2lp.edi.server.packets.InteractiveElementRecord;
import com.i2lp.edi.server.packets.Question;
import com.i2lp.edi.server.packets.User;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
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

    private ArrayList<InteractiveElementRecord> interactiveElementRecords = new ArrayList<>();
    private ArrayList<InteractiveElement> interactiveElementsInPresentation = new ArrayList<>(); //Needed to link records to InteractiveElements in presentation
    private ArrayList<InteractionRecord> interactionsFromStudents = new ArrayList<>();

    private ArrayList<Question> questionQueue;
    private ArrayList<User> activeUsers;

    //Configuration Variables for session

    private Date startDate;
    private Date endDate;
    ArrayList<Duration> slideTimes;
    private Instant slideStart;//The time at which the current slide started.

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

        //Add the slide timers:
        addSlideTimeListener();
        logger.info("Added Slide Timers");
        logger.info("Live Presentation Session beginning at " + (startDate = new Date()).toString());
    }

    public void beginInteraction(InteractiveElement interactiveElement, boolean isLive) {
        InteractiveElementRecord liveElementRecord = null;
        ArrayList<String> elementInteractions = new ArrayList<>();

        //Find interactive elementRecord with correct ID so we can retrieve its interactive_element_id PK:
        //TODO: Modify stored procedures to only use PresentationID and interactive_pres_id in place of PK so can remove this loop
        for (InteractiveElementRecord interactiveElementRecord : interactiveElementRecords) {
            if (interactiveElementRecord.getXml_element_id() == interactiveElement.getElementID()) {
                liveElementRecord = interactiveElementRecord;
                break;
            }
        }

        //If couldn't find the interactive element in database that matches the XML interactive element, throw an error and return
        if (liveElementRecord == null) {
            logger.error("Couldn't find InteractiveElementRecord in database that matches InteractiveElement " + interactiveElement.getElementID());
            return;
        }

        //Use TimeLimit from local XML
        logger.info("Response time is: " + interactiveElement.getTimeLimit());
        ediManager.getSocketClient().setInteractiveElementLive(activePresentation.getPresentationMetadata().getPresentationID(), liveElementRecord.getInteractive_element_id(), isLive);
        //Start timer of response interval, in which to set Interactive element non live
        Timer responseWindow = new Timer();
        InteractiveElementRecord finalLiveElement = liveElementRecord;
        //Start countdown timer, then perform render of results
        //TODO: Modify to enable live modification of Polls
        responseWindow.schedule(new TimerTask() {
            @Override
            public void run() {
                ediManager.getSocketClient().setInteractiveElementLive(activePresentation.getPresentationMetadata().getPresentationID(), finalLiveElement.getInteractive_element_id(), false);
                logger.info("Interactive element response window closed");

                //Find Interactions that belong to this current interactive element
                for (InteractionRecord interactionRecord : interactionsFromStudents) {
                    if (interactionRecord.getInteractive_element_id() == finalLiveElement.getInteractive_element_id()) {
                        elementInteractions.add(interactionRecord.getInteraction_data());
                    }
                }

                //If its a WordCloud, set the wordList
                if (interactiveElement instanceof WordCloudElement) {
                    if (interactionsFromStudents.isEmpty()) {
                        for (int i = 0; i < 100; i++) {
                            elementInteractions.add("NONE");
                        }
                        logger.error("No interactions received for Interactive Element: " + interactiveElement.getElementID());
                    }
                    ((WordCloudElement) interactiveElement).setWordList(elementInteractions);
                    Platform.runLater(() -> ((WordCloudElement) interactiveElement).generateWordCloud());
                } else if (interactiveElement instanceof PollElement) { //If its a poll, set the answer data
                    ((PollElement) interactiveElement).setPollOutput(elementInteractions);
                    Platform.runLater(() -> {
                        ((PollElement) interactiveElement).displayDone();
                    });
                }
            }
        }, interactiveElement.getTimeLimit() * 1000);
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

    private void gatherUserStatistics() {
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

        ((PresentationManagerTeacher) ediManager.getPresentationManager()).updateStudentList();
    }

    public ArrayList<Question> getQuestionQueue() {
        return questionQueue;
    }

    public void setQuestionQueue(ArrayList<Question> activeQuestions) {
        this.questionQueue = activeQuestions;

        //Update the QuestionQueue UI
        ((PresentationManagerTeacher) ediManager.getPresentationManager()).updateQuestionList();
    }

    public void setInteractionsForPresentation(ArrayList<InteractionRecord> interactionsFromStudents) {
        this.interactionsFromStudents = interactionsFromStudents;

        for (InteractionRecord interactionRecord : interactionsFromStudents) {
            logger.info("Interaction for " + interactionRecord.getInteractive_element_id() + " is " + interactionRecord.getInteraction_data() + " from user " + interactionRecord.getUser_id());
        }
    }

    private void addSlideTimeListener() {
        //Initialise the array with enough space for all of the slides. and set the time on ach slide to 0
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
