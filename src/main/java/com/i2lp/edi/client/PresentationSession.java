package com.i2lp.edi.client;

import com.i2lp.edi.client.managers.EdiManager;
import com.i2lp.edi.server.packets.Question;
import com.i2lp.edi.server.packets.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by amriksadhra on 27/05/2017.
 */
public class PresentationSession {
    private Logger logger = LoggerFactory.getLogger(PresentationSession.class);
    private EdiManager ediManager;


    private ArrayList<Question> questionQueue;
    private ArrayList<User> activeUsers;

    //Use better Date datastructure to store time per slide
    private Date startDate;
    private Date endDate;

    public PresentationSession(EdiManager ediManager){
        this.ediManager = ediManager;

        startDate = new Date();
        logger.info("Live Presentation Session beginning at " + startDate.toString());
    }

    public void endSession(){
        endDate = new Date();
        logger.info("Live Presentation session ending. Presentation lasted " + (int)((endDate.getTime() - startDate.getTime()) / 1000) + " seconds.");
    }

    public void setActiveUsers(ArrayList<User> activeUsers) {
        this.activeUsers = activeUsers;

        logger.info(activeUsers.size() + " Currently Active Users: ");

        for(User activeUser : activeUsers){
            logger.info(activeUser.getFirstName());
        }
    }

    public ArrayList<Question> getQuestionQueue() {
        return questionQueue;
    }

    public void setQuestionQueue(ArrayList<Question> activeQuestions){
        this.questionQueue = activeQuestions;

        logger.info("Received " + activeQuestions.size() + " questions");
        for(Question question : questionQueue){
            logger.info("Question: " + question.getQuestion_data());
        }
        //TODO: Tie to QuestionQueue UI for question answering
       /* if(ediManager.getSocketClient().answerQuestionInQuestionQueue(questionQueue.get(0).getQuestion_id())){
            logger.info("Answered question successfully");
        }*/
    }
}
