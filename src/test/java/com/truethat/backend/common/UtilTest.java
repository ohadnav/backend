package com.truethat.backend.common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 01/06/2017.
 */
public class UtilTest {
  @Test
  public void inputStreamToString() throws Exception {
    final String s = "my name is indigo montoya";
    InputStream stream = new ByteArrayInputStream(s.getBytes());
    assertEquals(s, Util.inputStreamToString(stream));
  }
}