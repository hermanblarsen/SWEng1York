package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.server.SocketClient;
import com.i2lp.edi.server.packets.PresentationStatisticsRecord;
import com.i2lp.edi.server.packets.Question;
import com.i2lp.edi.server.packets.User;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.skins.BarChartItem;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.i2lp.edi.client.Constants.PRESENTATIONS_PATH;

/**
 * Created by Koen on 25/05/2017.
 */
public class ReportManager {
    ArrayList<Question> questionQueueQuestions;
    ArrayList<PresentationStatisticsRecord> presentationStatistics;
    HashMap<Integer, Students> students = new HashMap<>();
    EdiManager ediManager;
    Presentation presentation;

    private void getAllPresentationData(){
        SocketClient sock = ediManager.getSocketClient();
        int presentationId = presentation.getPresentationMetadata().getPresentationID();

        questionQueueQuestions = sock.getQuestionsForPresentation(presentationId);
        presentationStatistics = sock.getPresentationStatistics(presentationId);
        sock.getStudentsForModule(presentation.getPresentationMetadata().getModule_id()).forEach(item -> students.put(item.getUserID(), new Students(item, 0)));

        // Populate students with thether they were present or not (based on whether there is a statistics entry for that student)
        for(PresentationStatisticsRecord statEntry: presentationStatistics){
            students.get(statEntry.getUserID()).setWasPresent(true);
        }
    }

    public void openReportPanel(Presentation presentation, EdiManager ediManager){
        this.ediManager = ediManager;
        this.presentation = presentation;
        getAllPresentationData();

        Stage stage = new Stage();
        stage.setTitle(presentation.getDocumentID() + " Report");
        ScrollPane reportPane = new ScrollPane();
        stage.setScene(new Scene(reportPane,750,500));
        stage.show();
        VBox flow = new VBox();
        HBox reportPanels = new HBox();

        //Attendance Tile
        Tile studentsInPresentation = TileBuilder.create()
                .skinType(Tile.SkinType.NUMBER)
                .prefSize(250,250)
                .title("Students")
                .value(25)
                .decimals(0)
                .description("of 30")
                .textVisible(true)
                .build();

        //Question Queue Tile
        Tile questionsAsked = TileBuilder.create()
                .skinType(Tile.SkinType.NUMBER)
                .prefSize(250,250)
                .title("Question Queue")
                .value(questionQueueQuestions.size())
                .decimals(0)
                .description("Click to see questions")
                .textVisible(true)
                .build();

        //Participation Tile
        Tile presentationParticipation = TileBuilder.create()
                .skinType(Tile.SkinType.CIRCULAR_PROGRESS)
                .prefSize(250,250)
                .title("Average Participation")
                .value(70)
                .unit("\u0025")
                .build();

        //Slide Times slide
        TableView slideTimeTable = generateSlideTimesTable(presentation, presentationStatistics);

        reportPanels.getChildren().addAll(
                studentsInPresentation,
                questionsAsked,
                presentationParticipation,
                slideTimeTable
        );

        questionsAsked.addEventHandler(MouseEvent.MOUSE_CLICKED,evt-> {new ReportManager().showQuestions(questionQueueQuestions);});
        studentsInPresentation.addEventHandler(MouseEvent.MOUSE_CLICKED,evt->showStudents());
        flow.getChildren().add(reportPanels);
        //reportPane.setContent(reportPanels);

        int numberOfPolls = 2;//Todo Remove this once connected to server.
        for(int i = 0;i<numberOfPolls;i++){
            int numberOfQuestions = 2+(int)Math.random()*6;
            flow.getChildren().add(this.generatePollTile("Generic Question",numberOfQuestions));
        }


        File wordcloudPath = new File(PRESENTATIONS_PATH+"/" +presentation.getDocumentID()+"/Wordclouds/");
        System.out.println(wordcloudPath.toString());
        if(wordcloudPath.exists()){
            System.out.println("TEST");
            String[] ext = {"png"};
            FilenameFilter imageFilter = (dir, name) -> {
                for (final String ext1 : ext) {
                    if (name.endsWith("." + ext1)) {
                        return true;
                    }
                }
                return false;
            };

            for(File f: wordcloudPath.listFiles(imageFilter)){
                System.out.println(f.getAbsolutePath());
                Image wordCloud = new Image("file:"+f.getAbsolutePath());
                ImageView wordCloudImage = new ImageView(wordCloud);
                flow.getChildren().add(wordCloudImage);
            }
        }
        reportPane.setContent(flow);
    }

    private TableView generateSlideTimesTable(Presentation presentation, ArrayList<PresentationStatisticsRecord> presentationStatistics){
        TableView slideTimeTable = new TableView();
        ArrayList<TableColumn> slideTimeColumns= new ArrayList<>();
        ArrayList<TableRow> slideTimeRows = new ArrayList<>();

        //Add first column
        TableColumn<StudentSlideTimes, String> studentColumn = new TableColumn("Student");
        studentColumn.setCellValueFactory(data -> data.getValue().studentNameProperty());
        slideTimeTable.getColumns().add(studentColumn);

        for(int i=0; i < presentation.getMaxSlideNumber(); i++){
            //Generate columns
            TableColumn<StudentSlideTimes, Integer> column= new TableColumn(Integer.toString(i+1));
            final int columnIndex = i;
            column.setCellValueFactory(data -> data.getValue().getSlideTime(columnIndex));
            slideTimeColumns.add(column);
            slideTimeTable.getColumns().add(column);
        }

        //Populate the table: (Process each statistics record intto a StudentSlideTime and add it to a list)
        ObservableList<StudentSlideTimes> slideTimesTableData = FXCollections.observableArrayList();
        for (PresentationStatisticsRecord record : presentationStatistics){
            slideTimesTableData.add(
                    new StudentSlideTimes(record.getSlideTimes(),
                            students.get(record.getUserID()).getFullName()
                    )
            );
        }

        slideTimeTable.setItems(slideTimesTableData);

        return slideTimeTable;
    }

    private Tile generatePollTile(String task, int numberOfQuestions){
        PollQuestions pQ = new PollQuestions(task,numberOfQuestions);
        ArrayList<Integer> pollOutput = pQ.generatePollResults();
        ArrayList<BarChartItem> chartDataList = new ArrayList<>();


        for(int j= 0;j<pollOutput.size();j++){
            chartDataList.add(new BarChartItem(Integer.toString(j),pollOutput.get(j).intValue(),Tile.BLUE));
        }

        System.out.println(chartDataList.size());
        System.out.println(chartDataList.get(0).getValue());
        Tile pollPanel = TileBuilder.create()
                .skinType(Tile.SkinType.BAR_CHART)
                .prefSize(500,250)
                .title(pQ.getTask())
                .barChartItems(chartDataList)
                .decimals(0)
                .build();
        return pollPanel;
    }

    public void showQuestions(ArrayList<Question> questionQueueQuestions){
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

        Panel[] slides = new Panel[questionQueueQuestions.size()];

        for(int i = 0;i<questionQueueQuestions.size();i++){
            slides[i] = new Panel();
            Label question = new Label(questionQueueQuestions.get(i).getQuestion_data());
            question.setWrapText(true);
            question.setTextFill(Color.WHITE);
            //Label timeWaited = new Label("Time waited "+questions.get(i).getTimeWaited()+" minutes");
           // timeWaited.setTextFill(Color.WHITE);
            //timeWaited.setWrapText(true);
            VBox slideBox = new VBox();
            slideBox.getChildren().addAll(question);
            slides[i].setStyle("-fx-background-color: #34495e");
            slides[i].setBody(slideBox);
            slides[i].setMinWidth(430);
            fp.getChildren().add(slides[i]);
        }
        sp.setContent(fp);
        stage.show();
    }

    public void showStudents(){
        Stage stage = new Stage();
        stage.setTitle("Students");
        ScrollPane sp = new ScrollPane();
        stage.setScene(new Scene(sp,450,450));
        FlowPane fp = new FlowPane();
        fp.setPrefWrapLength(100);
        fp.setPadding(new Insets(5,0,5,0));
        fp.setVgap(4);
        fp.setHgap(4);
        fp.setStyle("-fx-background-color: whitesmoke");
        sp.setStyle("-fx-background-color: whitesmoke");

        Panel[] slides = new Panel[students.size()];

        for (Map.Entry<Integer, Students> entry : students.entrySet()){
            Students student = entry.getValue();
            Panel studentParticipationPanel = new Panel();

            Label studentName = new Label(student.getFullName());
            studentName.setWrapText(true);
            studentName.setTextFill(Color.WHITE);
            Label participation = new Label("Participation: "+student.getParticipation()+" of "+10);//TODO Remove hardcoded total participations
            participation.setTextFill(Color.WHITE);
            participation.setWrapText(true);
            VBox slideBox = new VBox();
            slideBox.getChildren().addAll(studentName,participation);
            if (student.getWasPresent()) {
                studentParticipationPanel.setStyle("-fx-background-color: #65d16c");
            } else {
                studentParticipationPanel.setStyle("-fx-background-color: #d64c45");
            }
            studentParticipationPanel.setBody(slideBox);
            studentParticipationPanel.setMinWidth(430);
            fp.getChildren().add(studentParticipationPanel);
        }

        sp.setContent(fp);
        stage.show();
    }

    // Classes
    private class Questions{
        private String question;
        private int timeWaited;

        public Questions(String question, int timeWaited) {
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

    private class Students{
        int userId;
        private String firstName;
        private String lastName;
        private int participation;//Number of interactiv elements the student participated in.
		private boolean wasPresent = false;

        public Students(int userId, String firstName, String lastName, int participation) {
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.participation = participation;
        }

        //Initialise Student from their db record.
        public Students(User userRecord, int participation){
            this(userRecord.getUserID(), userRecord.getFirstName(), userRecord.getSecondName(), participation);
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public int getParticipation() {
            return participation;
        }

        public void setParticipation(int participation) {
            this.participation = participation;
        }

        public int getUserId() {
            return userId;
        }

        public String getFullName(){
            return this.firstName + " " + this.lastName;
        }

        public void setWasPresent(boolean wasPresent){
            this.wasPresent = wasPresent;
        }

        public boolean getWasPresent() {
            return wasPresent;
        }
    }

    private class PollQuestions{
        private String task;
        private int numberOfQuestions;
        private ArrayList<Integer> pollOutPut;

        public PollQuestions(String task, int numberOfQuestions) {
            this.task = task;
            this.numberOfQuestions = numberOfQuestions;
        }

        public ArrayList generatePollResults(){
            pollOutPut = new ArrayList<>();
            for(int i = 0; i<numberOfQuestions; i++){
                pollOutPut.add((int)(Math.random()*10));
            }
            return pollOutPut;
        }

        public String getTask() {
            return task;
        }

        public void setTask(String task) {
            this.task = task;
        }

        public int getNumberOfQuestions() {
            return numberOfQuestions;
        }

        public void setNumberOfQuestions(int numberOfQuestions) {
            this.numberOfQuestions = numberOfQuestions;
        }
    }

    //Class for the slide timing table's structure.
    private class StudentSlideTimes{
        private ArrayList<ObservableValue<Integer>> slideTimes = new ArrayList<>();
        private SimpleStringProperty studentName;

        public StudentSlideTimes(ArrayList<Integer> slideTimes, String studentName) {

            for(Integer slideTime : slideTimes){
                this.slideTimes.add(new ReadOnlyObjectWrapper<Integer>(slideTime));
            }
            this.studentName = new SimpleStringProperty(studentName);
        }

        public ArrayList<ObservableValue<Integer>> getSlideTimes() {
            return slideTimes;
        }

        public void setSlideTimes(ArrayList<ObservableValue<Integer>> slideTimes) {
            this.slideTimes = slideTimes;
        }

        public String getStudentName() {
            return studentName.get();
        }

        public SimpleStringProperty studentNameProperty() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName.set(studentName);
        }

        public ObservableValue<Integer> getSlideTime(int slideNum){
            return this.slideTimes.get(slideNum);
        }
    }
}


