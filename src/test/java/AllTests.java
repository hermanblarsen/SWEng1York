
/**
 * Created by habl on 26/02/2017.
 */

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ client.utilities.ParserXMLTest.class,
                client.utilities.VideoElementTest.class,
                client.utilities.GraphicElementTest.class })
public class AllTests {

}
