package com.i2lp.edi.client.utilities;

import static com.i2lp.edi.client.Constants.IS_CIRCLE_BUILD;
import javafx.stage.Stage;
import org.junit.Before;
import org.testfx.framework.junit.ApplicationTest;

import static org.junit.Assume.assumeFalse;

/**
 * Created by Luke on 25/05/2017.
 */
public abstract class GraphicalTest extends ApplicationTest {
    @Before
    public void before() {
        assumeFalse("Skipping GraphicsTest on circle (CI server is headless): "
                + getClass().getName(), IS_CIRCLE_BUILD);
    }
}
