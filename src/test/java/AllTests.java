
/**
 * Created by habl on 26/02/2017.
 */

import com.i2lp.edi.client.dashboard.DashboardTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        //Dashboard
        com.i2lp.edi.client.dashboard.StudentDashboardTest.class,
        com.i2lp.edi.client.dashboard.TeacherDashboardTest.class,

        //Editor
        com.i2lp.edi.client.editor.PollEditorTest.class,

        //login
        com.i2lp.edi.client.login.LoginTest.class,

        //Manager
        com.i2lp.edi.client.managers.StudentPresentationViewerTest.class,
        com.i2lp.edi.client.managers.TeacherPresentationViewerTest.class,

        //PresentationElements
        com.i2lp.edi.client.presentationElements.AudioElementIntegrationTestbench.class,
        com.i2lp.edi.client.presentationElements.GraphicElementTest.class,
        com.i2lp.edi.client.presentationElements.GraphicElementIntegrationTest.class,
        com.i2lp.edi.client.presentationElements.ImageElementTestbench.class,
        com.i2lp.edi.client.presentationElements.ImageElementIntegrationTestbench.class,
        com.i2lp.edi.client.presentationElements.PollElementTest.class,
        com.i2lp.edi.client.presentationElements.TextElementTest.class,
        com.i2lp.edi.client.presentationElements.VideoElementTest.class,
        com.i2lp.edi.client.presentationElements.WordCloudElementTest.class,

        //PresentationViewerElements
        com.i2lp.edi.client.presentationViewerElements.CommentPanelTest.class,
        com.i2lp.edi.client.presentationViewerElements.QuestionIndicatorTest.class,
        com.i2lp.edi.client.presentationViewerElements.ResponseIndicatorTest.class,

        //Utilities
        com.i2lp.edi.client.utilities.ParserXMLTest.class,
})
public class AllTests {
    //Will run all tests above. -Herman
}
