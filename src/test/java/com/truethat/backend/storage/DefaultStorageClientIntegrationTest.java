package com.truethat.backend.storage;

import com.google.cloud.storage.BlobInfo;
import com.google.common.io.ByteStreams;
import com.truethat.backend.common.TestUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 28/06/2017.
 */
public class DefaultStorageClientIntegrationTest extends BaseStorageTestSuite {
  private static final String FILENAME = "bitcoin-gen.txt";
  private static final String CONTENT_TYPE = "text/plain";

  @Test
  public void uploadFile() throws Exception {
    // Create a temp file to upload
    File tempFile = File.createTempFile(FILENAME.split("\\.")[0], "txt");
    //noinspection ResultOfMethodCallIgnored
    tempFile.setWritable(true);
    PrintWriter writer = new PrintWriter(tempFile);
    writer.print("moneyyyyy");
    writer.close();
    tempFile.deleteOnExit();
    // Uploads the file
    BlobInfo uploaded = storage.save(
        FILENAME, CONTENT_TYPE, ByteStreams.toByteArray(new FileInputStream(tempFile)), bucketName);
    // Asserts that file exists
    assertEquals(FILENAME, uploaded.getName());
    assertEquals(CONTENT_TYPE, uploaded.getContentType());
    assertEquals(bucketName, uploaded.getBucket());
    // File should be available
    TestUtil.assertUrl(storage.getPublicLink(uploaded), HttpURLConnection.HTTP_OK,
        new FileInputStream(tempFile));
  }
}