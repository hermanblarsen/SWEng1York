package com.i2lp.edi.client.managers;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.util.ArrayList;

/**
 * Created by Koen on 25/05/2017.
 */
public class ReportManager {

    public static void openReportPanel(String presentationID){
        Stage stage = new Stage();
        stage.setTitle(presentationID + " Report");
        ScrollPane reportPane = new ScrollPane();
        stage.setScene(new Scene(reportPane,750,500));
        stage.show();
        HBox reportPanels = new HBox();

        Tile studentsInPresentation = TileBuilder.create()
                                                 .skinType(Tile.SkinType.NUMBER)
                                                 .prefSize(250,250)
                                                 .title("Students")
                                                 .value(25)
                                                 .description("of 30")
                                                 .textVisible(true)
                                                 .build();

        Tile questionsAsked = TileBuilder.create()
                                         .skinType(Tile.SkinType.NUMBER)
                                         .prefSize(250,250)
                                         .title("Question Queue")
                                         .value(8)
                                         .description("Click to see questions")
                                         .textVisible(true)
                                         .build();

        Tile presentationParticipation = TileBuilder.create()
                                                    .skinType(Tile.SkinType.CIRCULAR_PROGRESS)
                                                    .prefSize(250,250)
                                                    .title("Participation")
                                                    .value(70)
                                                    .unit("\u0025")
                                                    .build();
        reportPanels.getChildren().addAll(studentsInPresentation,questionsAsked,presentationParticipation);
        questionsAsked.addEventHandler(MouseEvent.MOUSE_CLICKED,evt-> {new ReportManager().showQuestions();});
        reportPane.setContent(reportPanels);



    }

    public void showQuestions(){
        Stage stage = new Stage();
        stage.setTitle("Questions");
        ScrollPane sp = new ScrollPane();
        stage.setScene(new Scene(sp,450,450));
        FlowPane fp = new FlowPane();
        fp.setPrefWrapLength(100);
        fp.setPadding(new Insets(5,0,5,0));
        fp.setVgap(4);
        fp.setHgap(4);
        fp.setStyle("-fx-background-color: whitesmoke");
        sp.setStyle("-fx-background-color: whitesmoke");

        ArrayList<questions> questions = new ArrayList<>();
        questions.add(new questions("Poop!",2));
        questions.add(new questions("Poop?",3));
        questions.add(new questions("Poop!?",9));
        questions.add(new questions("||Poop||",6));

        Panel[] slides = new Panel[questions.size()];
        StackPane slidePane = new StackPane();

        for(int i = 0;i<questions.size();i++){
            slides[i] = new Panel(questions.get(i).getQuestion());
            Label timeWaited = new Label("Time waited "+questions.get(i).getTimeWaited()+" minutes");
            slides[i].setBody(timeWaited);
            slides[i].setMaxWidth(400);
            fp.getChildren().add(slides[i]);
        }
        sp.setContent(fp);
        stage.show();
    }

    private class questions{
        private String question;
        private int timeWaited;
        public questions(String question, int timeWaited) {
            this.question =question;
            this.timeWaited = timeWaited;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public int getTimeWaited() {
            return timeWaited;
        }

        public void setTimeWaited(int timeWaited) {
            this.timeWaited = timeWaited;
        }
    }
}


