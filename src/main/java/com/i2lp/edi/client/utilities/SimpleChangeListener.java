package com.i2lp.edi.client.utilities;

/**
 * Created by zain on 25/05/2017.
 * A Functional Interface for Simple On Change event listeners
 */
public interface SimpleChangeListener {
	void changed(Object oldVal, Object newVal);
}
