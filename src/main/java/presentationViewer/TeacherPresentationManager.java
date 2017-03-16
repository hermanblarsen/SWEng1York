package presentationViewer;
import managers.PresentationManager;


/**
 * Created by kma517 on 16/03/2017.
 */
public class TeacherPresentationManager extends PresentationManager {
    public static void main(String[] args){launch(args);}
    public TeacherPresentationManager() {

    }


    @Override
    protected void questionQueueFunction() {
        System.out.println("Teacher QQ: Not yet implemented");
    }

    @Override
    protected void commentFunction() {
        System.out.println("SlideNotes: Not yet implemented");

    }
}
