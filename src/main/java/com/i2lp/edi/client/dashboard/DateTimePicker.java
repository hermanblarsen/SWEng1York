package com.i2lp.edi.client.dashboard;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by Kacper on 2017-05-31.
 *
 * Class provides a date and time picket used for scheduling presentations.
 */
public class DateTimePicker extends VBox {

    protected Button scheduleButton;
    private DatePicker datePicker;
    private TimePicker timePicker;

    /**
     * Creates a window containing the date time picker. Using getDateTime and getScheduleButton, developers can find the selected time.
     */
    public DateTimePicker() {
        VBox popupVBoxTop = new VBox(5);
        popupVBoxTop.setPadding(new Insets(0, 0, 5, 0));
        popupVBoxTop.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), null)));
        popupVBoxTop.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderStroke.DEFAULT_WIDTHS)));

        datePicker = new DatePicker(LocalDate.now());
        popupVBoxTop.getChildren().add(datePicker);

        timePicker = new TimePicker();
        popupVBoxTop.getChildren().add(timePicker);

        scheduleButton = new Button("Schedule");
        scheduleButton.getStyleClass().setAll("btn", "btn-default");

        popupVBoxTop.setAlignment(Pos.CENTER);
        getChildren().add(popupVBoxTop);
        getChildren().add(scheduleButton);
        setAlignment(Pos.CENTER);
    }

    public Button getScheduleButton() { return scheduleButton; }

    public LocalDateTime getDateTime() {
        return LocalDateTime.of(datePicker.getValue(), timePicker.getValue());
    }
}
