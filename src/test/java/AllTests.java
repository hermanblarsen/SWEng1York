
/**
 * Created by habl on 26/02/2017.
 */

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        //Dashboard
        com.i2lp.edi.client.dashboard.TeacherDashboardTest.class,
        com.i2lp.edi.client.dashboard.StudentDashboardTest.class,
        //Editor
        com.i2lp.edi.client.editor.PollEditorTest.class,

        //PresentationElements
        com.i2lp.edi.client.presentationElements.AudioElementIntegrationTestbench.class,
        com.i2lp.edi.client.presentationElements.GraphicElementTest.class,
        com.i2lp.edi.client.presentationElements.GraphicElementIntegrationTest.class,
        com.i2lp.edi.client.presentationElements.ImageElementTestbench.class,
        com.i2lp.edi.client.presentationElements.ImageElementIntegrationTestbench.class,
        com.i2lp.edi.client.presentationElements.VideoElementTest.class,

        //PresentationViewerElements
        com.i2lp.edi.client.presentationViewerElements.CommentPanelTest.class,
        com.i2lp.edi.client.presentationViewerElements.QuestionIndicatorTest.class,
        com.i2lp.edi.client.presentationViewerElements.ResponseIndicatorTest.class,

        //Utilities
        com.i2lp.edi.client.utilities.ParserXMLTest.class,

        //login
        com.i2lp.edi.client.login.LoginTest.class,
})
public class AllTests {
    //Will run all tests above. -Herman
}
