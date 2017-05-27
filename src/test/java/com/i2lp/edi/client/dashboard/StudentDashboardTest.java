package com.i2lp.edi.client.dashboard;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import static org.junit.Assert.assertTrue;

/**
 * Created by Luke on 06/05/2017.
 */
public class StudentDashboardTest {

    /*TODO add to start method:
    * if (IS_CIRCLE_BUILD) {
            System.out.println("Skipping test requiring graphics on circle.ci (CI server is headless)");
            return;
        }
    */

    @Before
    public void setUp() {
        //Ignores the test if the build is run from circle (headless) environment
        Assume.assumeTrue(!IS_CIRCLE_BUILD);



    }

    @Test
    public void emptyTest() {
        //TODO Fill in actual tests, this is to satisfy JUnit
        assertTrue(true);
    }

}
