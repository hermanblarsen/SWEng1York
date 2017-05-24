
/**
 * Created by habl on 26/02/2017.
 */

import com.i2lp.edi.client.presentationViewerElements.CommentPanelTest;
import com.i2lp.edi.client.presentationViewerElements.QuestionIndicatorTest;
import com.i2lp.edi.client.presentationViewerElements.ResponseIndicatorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        CommentPanelTest.class,
        com.i2lp.edi.client.presentationElements.VideoElementTest.class,
        com.i2lp.edi.client.presentationElements.GraphicElementTest.class,
        QuestionIndicatorTest.class,
        ResponseIndicatorTest.class,
        com.i2lp.edi.client.dashboard.TeacherDashboardTest.class,
        com.i2lp.edi.client.editor.PollEditorTest.class,
        com.i2lp.edi.client.utilities.ParserXMLTest.class,
        com.i2lp.edi.client.login.LoginTest.class
})
public class AllTests {

}
