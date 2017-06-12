package com.truethat.backend.common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Proudly created by ohad on 01/06/2017.
 */
public class TestUtil {
  public static InputStream toInputStream(String s) {
    return new ByteArrayInputStream(s.getBytes());
  }
}
