package com.i2lp.edi.server.packets;

import java.sql.Timestamp;

/**
 * Created by amriksadhra on 28/05/2017.
 */
public class Question {
    private int question_id;
    private int user_id;
    private int presentation_id;
    private Timestamp time_created;
    private Timestamp time_answered;
    private String question_data;
    private int slide_number;


    public Question(int question_id, int user_id, int presentation_id, Timestamp time_created, Timestamp time_answered, String question_data, int slide_number) {
        this.question_id = question_id;
        this.user_id = user_id;
        this.presentation_id = presentation_id;
        this.time_created = time_created;
        this.time_answered = time_answered;
        this.question_data = question_data;
        this.slide_number = slide_number;
    }


    public String getQuestion_data() {
        return question_data;
    }

    public int getQuestion_id() {
        return question_id;
    }

    public Timestamp getTime_created() {
        return time_created;
    }

}
