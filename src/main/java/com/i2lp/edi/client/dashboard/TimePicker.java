package com.i2lp.edi.client.dashboard;

import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.time.LocalTime;

/**
 * Created by Kacper on 2017-04-22.
 */
public class TimePicker extends HBox {

    ComboBox<String> hours, minutes;

    public TimePicker() {
        hours = new ComboBox<>();
        hours.getItems().add("00");
        hours.getItems().add("01");
        hours.getItems().add("02");
        hours.getItems().add("03");
        hours.getItems().add("04");
        hours.getItems().add("05");
        hours.getItems().add("06");
        hours.getItems().add("07");
        hours.getItems().add("08");
        hours.getItems().add("09");
        hours.getItems().add("10");
        hours.getItems().add("11");
        hours.getItems().add("12");
        hours.getItems().add("13");
        hours.getItems().add("14");
        hours.getItems().add("15");
        hours.getItems().add("16");
        hours.getItems().add("17");
        hours.getItems().add("18");
        hours.getItems().add("19");
        hours.getItems().add("20");
        hours.getItems().add("21");
        hours.getItems().add("22");
        hours.getItems().add("23");
        hours.setValue(hours.getItems().get(0));

        minutes = new ComboBox<>();
        minutes.getItems().add("00");
        minutes.getItems().add("05");
        minutes.getItems().add("10");
        minutes.getItems().add("15");
        minutes.getItems().add("20");
        minutes.getItems().add("25");
        minutes.getItems().add("30");
        minutes.getItems().add("35");
        minutes.getItems().add("40");
        minutes.getItems().add("45");
        minutes.getItems().add("50");
        minutes.getItems().add("55");
        minutes.setValue(minutes.getItems().get(0));

        Label separator = new Label(":");

        getChildren().add(hours);
        getChildren().add(separator);
        getChildren().add(minutes);
        setAlignment(Pos.CENTER);
    }

    public LocalTime getValue() {
        return LocalTime.parse(hours.getValue()+ ":" + minutes.getValue());
    }

}
