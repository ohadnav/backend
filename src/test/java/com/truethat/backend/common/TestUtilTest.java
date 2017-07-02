package com.truethat.backend.common;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

  @Test
  public void assertInputStreamsEqual() throws Exception {
    String s1 = "a";
    String s2 = "a";
    TestUtil.assertInputStreamsEqual(TestUtil.toInputStream(s1), TestUtil.toInputStream(s2));
  }

  @Test(expected = AssertionError.class)
  public void assertInputStreamsNotEqual() throws Exception {
    String s1 = "a";
    String s2 = "b";
    TestUtil.assertInputStreamsEqual(TestUtil.toInputStream(s1), TestUtil.toInputStream(s2));
  }

  @Test
  public void assertInputStreamsEqual_files() throws Exception {
    FileInputStream file1 = new FileInputStream(new File("src/test/resources/api/1x1_pixel.jpg"));
    FileInputStream file2 = new FileInputStream(new File("src/test/resources/api/1x1_pixel.jpg"));
    TestUtil.assertInputStreamsEqual(file1, file2);
  }

  @Test(expected = AssertionError.class)
  public void assertInputStreamsNotEqual_files() throws Exception {
    FileInputStream file1 = new FileInputStream(new File("src/test/resources/api/1x1_pixel.jpg"));
    File tempFile = File.createTempFile("name", "txt");
    tempFile.deleteOnExit();
    FileInputStream file2 = new FileInputStream(tempFile);
    TestUtil.assertInputStreamsEqual(file1, file2);
  }

  @Test public void assertUrl_urlOk() throws Exception {
    String url = "https://www.snap.com/images/brand-guidelines/branding-logos.png";
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.connect();
    assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
    TestUtil.assertUrl(url, HttpURLConnection.HTTP_OK, connection.getInputStream());
    connection.disconnect();
  }

  @Test(expected = AssertionError.class) public void assertUrl_differentContent() throws Exception {
    String url = "https://www.snap.com/images/brand-guidelines/branding-logos.png";
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.connect();
    assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
    TestUtil.assertUrl("http://google.com", HttpURLConnection.HTTP_OK, connection.getInputStream());
    connection.disconnect();
  }
}