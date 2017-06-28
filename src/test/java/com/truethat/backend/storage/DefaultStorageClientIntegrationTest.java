package com.truethat.backend.storage;

import com.google.api.services.storage.model.StorageObject;
import java.io.File;
import java.io.FileInputStream;
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
    tempFile.deleteOnExit();
    // Uploads the file
    String uploaded = storageClient.save(
        FILENAME, CONTENT_TYPE, new FileInputStream(tempFile), bucketName);
    // Asserts that file exists
    final StorageObject found = storageClient.getClient().objects().get(bucketName, uploaded).execute();
    assertEquals(found.getName(), FILENAME);
    assertEquals(CONTENT_TYPE, found.getContentType());
    assertEquals(FILENAME, found.getName());
    assertEquals(bucketName, found.getBucket());
  }
}