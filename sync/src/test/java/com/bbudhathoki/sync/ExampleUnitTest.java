package com.bbudhathoki.sync;

import org.junit.Test;

import static org.junit.Assert.*;
import com.bbudhathoki.sync.requesthandler.ApiHandler;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void plugloadQueryTest() {
        ApiHandler handler = new ApiHandler();
        handler.queryPlugload();
    }
}