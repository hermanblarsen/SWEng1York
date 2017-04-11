
/**
 * Created by habl on 26/02/2017.
 */

import client.utilities.VideoElementOLDTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ client.utilities.ParserXMLTest.class,
                VideoElementOLDTest.class,
                client.utilities.GraphicElementTest.class })
public class AllTests {

}
