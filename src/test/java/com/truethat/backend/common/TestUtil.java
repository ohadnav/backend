package com.truethat.backend.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 01/06/2017.
 */
public class TestUtil {
  public static InputStream toInputStream(String s) {
    return new ByteArrayInputStream(s.getBytes());
  }

  public static BufferedReader toBufferedReader(String s) {
    return new BufferedReader(new StringReader(s));
  }

  public static void assertUrl(String url, int expectedCode,
      @Nullable InputStream expectedInputStream) throws Exception {
    // Asserts the file can now be accessed.
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.connect();
    assertEquals(expectedCode, connection.getResponseCode());
    if (expectedInputStream != null) {
      assertInputStreamsEqual(expectedInputStream, connection.getInputStream());
    }
    connection.disconnect();
  }

  static void assertInputStreamsEqual(InputStream expected, InputStream actual)
      throws Exception {
    while (expected.available() > 0 && actual.available() > 0) {
      assertEquals(expected.read(), actual.read());
    }
    // Both streams should reach the end.
    assertEquals(0, expected.available());
    assertEquals(0, actual.available());
  }
}
