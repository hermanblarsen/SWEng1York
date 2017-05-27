package com.i2lp.edi.client;

import com.i2lp.edi.server.packets.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by amriksadhra on 27/05/2017.
 */
public class PresentationSession {
    Logger logger = LoggerFactory.getLogger(PresentationSession.class);
    //Use better Date datastructure to store time per slide
    Date startDate;
    Date endDate;

    public PresentationSession(){
        startDate = new Date();
        logger.info("Live Presentation Session beginning at " + startDate.toString());
    }

    public void endSession(){
        endDate = new Date();
        logger.info("Live Presentation session ending. Presentation lasted " + (int)((endDate.getTime() - startDate.getTime()) / 1000) + "seconds.");
    }

    ArrayList<User> activeUsers;

    public ArrayList<User> getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(ArrayList<User> activeUsers) {
        this.activeUsers = activeUsers;

        logger.info(activeUsers.size() + " Currently Active Users: ");

        for(User activeUser : activeUsers){
            logger.info(activeUser.getFirstName());
        }
    }
}
