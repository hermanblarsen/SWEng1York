package com.i2lp.edi.client.managers;

import com.i2lp.edi.client.presentationElements.PollElement;
import com.i2lp.edi.client.presentationElements.Presentation;
import com.i2lp.edi.client.presentationElements.SlideElement;
import com.i2lp.edi.client.presentationElements.WordCloudElement;
import com.i2lp.edi.server.SocketClient;
import com.i2lp.edi.server.packets.*;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.skins.BarChartItem;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.i2lp.edi.client.Constants.PRESENTATIONS_PATH;

/**
 * Created by Koen on 25/05/2017.
 */

/**
 * Class to generate reports of previously viewed presentations,
 * with interaction data from interactive elements, and other
 * statistics such as attendance.
 */
public class ReportManager {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    ArrayList<Question> questionQueueQuestions;
    ArrayList<PresentationStatisticsRecord> presentationStatistics;
    ArrayList<InteractionRecord> interactions;
    ArrayList<InteractiveElementRecord> interactiveElementsData;
    HashMap<Integer, User> students = new HashMap<>();
    private int totalParticipatingStudents = 0;

    EdiManager ediManager;
    Presentation presentation;

    private void getAllPresentationData(){
        SocketClient sock = ediManager.getSocketClient();
        int presentationId = presentation.getPresentationMetadata().getPresentationID();

        questionQueueQuestions = sock.getQuestionsForPresentation(presentationId, true);
        presentationStatistics = sock.getPresentationStatistics(presentationId);
	    interactions = sock.getInteractionsForPresentation(presentationId);
	    interactiveElementsData = sock.getInteractiveElementsForPresentation(presentationId);
        sock.getStudentsForModule(presentation.getPresentationMetadata().getModule_id()).forEach(item -> students.put(
        		item.getUserID(),
		        new User(
        		    item,
                        (int)interactions.stream().filter(interaction->interaction.getUser_id()== item.getUserID()).filter(distinctByKey(i->i.getInteractive_element_id())).count(
                )
                )
        ));


        // Populate students with whether they were present or not (based on whether there is a statistics entry for that student)
        for(PresentationStatisticsRecord statEntry: presentationStatistics){
            students.get(statEntry.getUserID()).setWasPresent(true);
            totalParticipatingStudents++;
        }
    }

    /**
     * Opens the report panel for a presentation, which will generate a report for the
     * selected presentaton.
     * @param presentation presentation selected
     * @param ediManager edimanager of selected presentation
     */
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
        reportPane.setVgap(10);
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
        double averageParticipation = 0;
        if(totalParticipatingStudents != 0) {
            averageParticipation = students.entrySet().stream().filter(i -> i.getValue().getWasPresent()).mapToInt(i -> i.getValue().getParticipation()).sum() / ((double)totalParticipatingStudents * interactiveElementsData.size());
        }
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
        reportPane.getChildren().addAll(generateWordCloudTile());

    }

    private TableView generateSlideTimesTable(Presentation presentation, ArrayList<PresentationStatisticsRecord> presentationStatistics){
        TableView slideTimeTable = new TableView();
        ArrayList<TableColumn> slideTimeColumns= new ArrayList<>();
        ArrayList<TableRow> slideTimeRows = new ArrayList<>();

        //Add first column
        TableColumn<StudentSlideTimes, String> studentColumn = new TableColumn("Student");
        studentColumn.setCellValueFactory(data -> data.getValue().studentNameProperty());
        slideTimeTable.getColumns().add(studentColumn);

        //Slide Times column
        TableColumn<StudentSlideTimes, String> slideTimesHeader = new TableColumn<>( "Time spent on each slide (Seconds)" );
        slideTimeTable.getColumns().addAll( slideTimesHeader );

        for(int i=0; i < presentation.getMaxSlideNumber(); i++){
            //Generate columns
            TableColumn<StudentSlideTimes, Integer> column= new TableColumn(Integer.toString(i+1));
            final int columnIndex = i;
            column.setCellValueFactory(data -> data.getValue().getSlideTime(columnIndex));
            slideTimeColumns.add(column);
            slideTimesHeader.getColumns().add(column);
        }

        //Populate the table: (Process each statistics record into a StudentSlideTime and add it to a list)
        ObservableList<StudentSlideTimes> slideTimesTableData = FXCollections.observableArrayList();
        for (PresentationStatisticsRecord record : presentationStatistics){
            slideTimesTableData.add(
                    new StudentSlideTimes(record.getSlideTimes(),
                            students.get(record.getUserID()).getFullName()
                    )
            );
        }

        //Disable column reordering :
        slideTimeTable.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            final TableHeaderRow header = (TableHeaderRow) slideTimeTable.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((o, oldVal, newVal) -> header.setReordering(false));
        });

        slideTimeTable.setItems(slideTimesTableData);
        slideTimeTable.setPrefWidth(750);
        slideTimeTable.setEditable( false );
        slideTimeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
            pollResults.add(new BarChartItem(possibleAnswers.get(i), resultsTally[i]/0.9D, Tile.BLUE));
        }

        Tile pollPanel = TileBuilder.create()
                .skinType(Tile.SkinType.BAR_CHART)
                .prefSize(750,250)
                .title(question)
                .minValue(0)
                .maxValue(IntStream.of(resultsTally).sum())
                .barChartItems(pollResults)
                .maxMeasuredValueVisible(true)
                .decimals(0)
                .build();

        return pollPanel;
    }

    private ArrayList<Node> generateWordCloudTile(){
        ArrayList<Node> wordclouds = new ArrayList<>(  );

        File wordcloudPath = new File(PRESENTATIONS_PATH+"/" +presentation.getDocumentID()+"/Wordclouds/");
        if(wordcloudPath.exists()){
            logger.info( "Retrieving wordclouds." );
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
                VBox wordcloudTile = new VBox(  );
                Text title = new Text();

                logger.info("Adding wordcloud at " + f.getAbsolutePath());

                Pattern pattern = Pattern.compile(".*_(\\d+)_(\\d+).png");
                Matcher matcher = pattern.matcher(f.getAbsolutePath());
                if(matcher.matches()) {
                    int slide = Integer.parseInt(matcher.group(1));
                    int elementid = Integer.parseInt(matcher.group(2));
                    logger.info("Wordcloud is at slide " + slide + " Element " + elementid);
                    title.setText(((WordCloudElement) presentation.getSlide(slide-1).getElementWithID(elementid)).getQuestion());
                } else {
                    title.setText("A Wordcloud");
                }
                title.setFont(Font.font(28));
                title.setFill(Color.WHITE);
                title.setX(10);


                Image wordCloud = new Image("file:" + f.getAbsolutePath());
                ImageView wordCloudImage = new ImageView(wordCloud);

                wordCloudImage.setPreserveRatio(true);
                wordCloudImage.setFitWidth( 750 );
                wordcloudTile.setMaxWidth( 750 );
                wordcloudTile.setStyle("-fx-background-color: #2a2a2a;-fx-background-radius: 5px;");

                wordcloudTile.getChildren().addAll( title, wordCloudImage );
                wordclouds.add(wordcloudTile);
            }
        }
        return wordclouds;
    }

    private void showQuestions(ArrayList<Question> questionQueueQuestions){
        Stage stage = new Stage();
        stage.setTitle("Questions");
        ScrollPane sp = new ScrollPane();
        stage.setScene(new Scene(sp,450,450));

        TableView questionsTable = new TableView(  );

        TableColumn<Question, String> question = new TableColumn( "Question" );
        question.setCellValueFactory( data -> {

            return new ReadOnlyObjectWrapper<>(data.getValue().getQuestion_data() );
        } );
        questionsTable.getColumns().add(question);

        TableColumn<Question, Integer> slide = new TableColumn( "Asked on Slide" );
        slide.setCellValueFactory( data-> new ReadOnlyObjectWrapper<>( data.getValue().getSlide_number()+1 ) );
        questionsTable.getColumns().add( slide );

        TableColumn<Question, String> wasAnswered= new TableColumn<>( "Answered?" );
        wasAnswered.setCellValueFactory( data-> new ReadOnlyObjectWrapper<>( data.getValue().getTime_answered()==null ? "No" : "Yes") );
        questionsTable.getColumns().add(wasAnswered);

        //Add the Question data to the table:
        questionsTable.setItems( FXCollections.observableArrayList(questionQueueQuestions) );

        //Table Formatting
        questionsTable.setEditable( false );
        questionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        sp.setFitToWidth( true );
        sp.setFitToHeight( true );
        sp.setContent(questionsTable);
        stage.show();
    }

    private void showStudents(){
        Stage stage = new Stage();
        stage.setTitle("User");
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

        students.forEach((key, student)->{
            if (!student.isTeacher){
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
        }});

        sp.setContent(fp);
        stage.show();
    }

    private class User {
        int userId;
        private String firstName;
        private String lastName;
        private int participation;//Number of interactiv elements the student participated in.
		private boolean wasPresent = false;
		private boolean isTeacher = false;

        public User(int userId, String firstName, String lastName, int participation, boolean isTeacher) {
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.participation = participation;
            this.isTeacher = isTeacher;
        }

        //Initialise Student from their db record.
        public User(com.i2lp.edi.server.packets.User userRecord, int participation){
            this(userRecord.getUserID(), userRecord.getFirstName(), userRecord.getSecondName(), participation, userRecord.isTeacher());
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

        public boolean isTeacher() {
            return isTeacher;
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T,Object> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
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


