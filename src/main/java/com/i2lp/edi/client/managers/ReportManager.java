package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.presentationElements.PollElement;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.presentationElements.SlideElement;
import com.i2lp.edi.client.presentationElements.WordCloudElement;
import com.i2lp.edi.server.SocketClient;
import com.i2lp.edi.server.packets.*;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

import static com.i2lp.edi.client.Constants.PRESENTATIONS_PATH;

/**
 * Created by Koen on 25/05/2017.
 */
public class ReportManager {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    ArrayList<Question> questionQueueQuestions;
    ArrayList<PresentationStatisticsRecord> presentationStatistics;
    ArrayList<InteractionRecord> interactions;
    ArrayList<InteractiveElementRecord> interactiveElementsData;
    HashMap<Integer, Students> students = new HashMap<>();
    private int totalParticipatingStudents = 0;

    EdiManager ediManager;
    Presentation presentation;

    private void getAllPresentationData(){
        SocketClient sock = ediManager.getSocketClient();
        int presentationId = presentation.getPresentationMetadata().getPresentationID();

        questionQueueQuestions = sock.getQuestionsForPresentation(presentationId);
        presentationStatistics = sock.getPresentationStatistics(presentationId);
	    interactions = sock.getInteractionsForPresentation(presentationId);
	    interactiveElementsData = sock.getInteractiveElementsForPresentation(presentationId);
        sock.getStudentsForModule(presentation.getPresentationMetadata().getModule_id()).forEach(item -> students.put(
        		item.getUserID(),
		        new Students(
        		    item,
				        (int) interactions.stream().filter( interaction->interaction.getUser_id() == item.getUserID()).count() )
        ));


        // Populate students with whether they were present or not (based on whether there is a statistics entry for that student)
        for(PresentationStatisticsRecord statEntry: presentationStatistics){
            students.get(statEntry.getUserID()).setWasPresent(true);
            totalParticipatingStudents++;
        }
    }

    public void openReportPanel(Presentation presentation, EdiManager ediManager){
        this.ediManager = ediManager;
        this.presentation = presentation;
        getAllPresentationData();

        Stage stage = new Stage();
        stage.setTitle(presentation.getDocumentID() + " Report");
        FlowPane reportPane = new FlowPane();
        ScrollPane scrollWrapper = new ScrollPane();
        scrollWrapper.setFitToHeight(true);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setContent(reportPane);
        reportPane.setPadding(new Insets(10,10,10,10));
        stage.setScene(new Scene(scrollWrapper,780,1000));
        stage.setResizable(false);
        stage.show();

        //Attendance Tile
        Tile studentsInPresentation = TileBuilder.create()
                .skinType(Tile.SkinType.NUMBER)
                .prefSize(250,250)
                .title("Attendance")
                .value(presentationStatistics.size())
                .decimals(0)
                .description("of " + students.size())
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
	    int averageParticipation = students.values().stream().filter( i->i.getWasPresent() ).mapToInt( i->i.getParticipation() ).sum()/(totalParticipatingStudents*interactiveElementsData.size());
        Tile presentationParticipation = TileBuilder.create()
                .skinType(Tile.SkinType.CIRCULAR_PROGRESS)
                .prefSize(250,250)
                .title("Average Participation")
                .value(averageParticipation*100)
                .unit("\u0025")
                .build();

        //Slide Times slide
        TableView slideTimeTable = generateSlideTimesTable(presentation, presentationStatistics);

        //Add the elements so far to the window.
        reportPane.getChildren().addAll(
                studentsInPresentation,
                questionsAsked,
                presentationParticipation,
                slideTimeTable
        );


        //Listeners
        questionsAsked.addEventHandler(MouseEvent.MOUSE_CLICKED,evt-> {new ReportManager().showQuestions(questionQueueQuestions);});
        studentsInPresentation.addEventHandler(MouseEvent.MOUSE_CLICKED,evt->showStudents());


        //Poll results:
        interactiveElementsData.forEach(interactiveElement ->{
            SlideElement correspondingSlideElement = presentation.getSlide( interactiveElement.getSlide_number()-1 )
                                                    .getElementWithID( interactiveElement.getXml_element_id() );
            if(correspondingSlideElement instanceof PollElement) {
                reportPane.getChildren().add(generatePollTile(
                        interactiveElement,
                        ((PollElement)correspondingSlideElement).getQuestion(),
                        Arrays.asList(((PollElement)correspondingSlideElement).getAnswers().split( "," ))
                ));
            } else if(correspondingSlideElement instanceof WordCloudElement){
            	//generateWordCloudTile(interactiveElement, ((WordCloudElement)correspondingSlideElement).getQuestion());
            } else {
                logger.error("Mismatching interactive element IDs between database entry and the local presentation!!");
                return;
            }
        });


        //Wordcloud Element
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
                Image wordCloud = new Image("file:" + f.getAbsolutePath());
                ImageView wordCloudImage = new ImageView(wordCloud);
                wordCloudImage.prefWidth(750);
                wordCloudImage.setPreserveRatio(true);
                reportPane.getChildren().add(wordCloudImage);
            }
        }
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
        slideTimeTable.setPrefWidth(750);

        return slideTimeTable;
    }

    private Tile generatePollTile(InteractiveElementRecord elementRecord, String question, List<String> possibleAnswers){
        ArrayList<BarChartItem> pollResults = new ArrayList<>();
        int[] resultsTally = new int[possibleAnswers.size()];

        // Tally the number of responses for each possibly answer
        interactions.stream()
                .filter(interaction -> interaction.getInteractive_element_id() == elementRecord.getInteractive_element_id())
                .forEach(result -> resultsTally[ Integer.parseInt( result.getInteraction_data() ) ]++);

        // Format the responses ready for the bar chart.
        for (int i=0; i<possibleAnswers.size(); i++){
            pollResults.add(new BarChartItem(possibleAnswers.get(i), resultsTally[i], Tile.BLUE));
        }

        Tile pollPanel = TileBuilder.create()
                .skinType(Tile.SkinType.BAR_CHART)
                .prefSize(750,250)
                .title(question)
                .barChartItems(pollResults)
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
            Label participation = new Label("Participation: "+student.getParticipation()+" of "+interactiveElementsData.size());
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


