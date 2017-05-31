package com.i2lp.edi.client;

import com.i2lp.edi.server.packets.InteractionRecord;
import com.i2lp.edi.server.packets.User;

import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by amriksadhra on 30/05/2017.
 */
public class UserStatistics {
    private User userData;
    private int numberOfResponses;
    private ArrayList<InteractionRecord> userInteractionRecords;
    private Time averageTimeToReply;
    private ArrayList<Time> timePerSlide;

    public UserStatistics(){
        //For each interaction type I want
        //WordCloud: Number of words submitted
        //Poll: Number

        for(InteractionRecord userInteractionRecord : userInteractionRecords){
            //userInteractionRecord
        }
    }
}
