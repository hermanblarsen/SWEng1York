package presentationViewer;

import managers.PresentationManager;


/**
 * Created by kma517 on 16/03/2017.
 */
public class StudentPresentationManager extends PresentationManager {
    public StudentPresentationManager() {
    }

    public static void main(String[] args){launch(args);}


    @Override
    protected void questionQueueFunction() {
        System.out.println("Question Queue: Not yet implemented");
    }

    @Override
    protected void commentFunction() {
        System.out.println("Commenting: Not yet implemented");
    }
}

