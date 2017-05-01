
/**
 * Created by habl on 26/02/2017.
 */

import com.i2lp.edi.client.presentationElements.GraphicElementTest;
import com.i2lp.edi.client.presentationElements.VideoElementTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        com.i2lp.edi.client.presentationElements.CommentTest.class,
        com.i2lp.edi.client.editor.PollEditorTest.class,
        com.i2lp.edi.client.utilities.ParserXMLTest.class,
        VideoElementTest.class,
        GraphicElementTest.class,
        com.i2lp.edi.client.login.LoginTest.class
})
public class AllTests {

}
