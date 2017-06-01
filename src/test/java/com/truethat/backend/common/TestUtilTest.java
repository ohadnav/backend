package com.truethat.backend.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 01/06/2017.
 */
public class TestUtilTest {
    @Test
    public void toInputStream() throws Exception {
        String s = "Tel Aviv";
        assertEquals(s, Util.inputStreamToString(TestUtil.toInputStream(s)));
    }

}