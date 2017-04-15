
/**
 * Created by habl on 26/02/2017.
 */

import com.i2lp.edi.client.utilities.VideoElementOLDTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        com.i2lp.edi.client.utilities.ParserXMLTest.class,
        com.i2lp.edi.client.utilities.VideoElementOLDTest.class,
        com.i2lp.edi.client.utilities.GraphicElementTest.class,
        com.i2lp.edi.client.login.LoginTest.class
})
public class AllTests {

}
