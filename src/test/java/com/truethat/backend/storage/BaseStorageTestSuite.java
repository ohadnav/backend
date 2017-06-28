package com.truethat.backend.storage;

import com.google.api.services.storage.StorageRequest;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 14/05/2017.
 */
public class BaseStorageTestSuite {
  protected final String bucketName = "test-bucket-" + System.currentTimeMillis();
  protected DefaultStorageClient storageClient;

  /**
   * Sets up the Storage storageClient and creates a dummy bucket.
   */
  @Before
  public void setUp() throws Exception {
    storageClient = new DefaultStorageClient();
    // Creates dummy bucket.
    storageClient.addBucket(bucketName);
  }

  /**
   * Deletes the dummy bucket.
   */
  @After
  public void tearDown() throws Exception {
    Objects remainingObjects = storageClient.getClient().objects().list(bucketName).execute();
    if (remainingObjects.containsKey("items")) {
      for (StorageObject object : remainingObjects.getItems()) {
        storageClient.getClient().objects().delete(bucketName, object.getName()).execute();
      }
    }
    // Deletes dummy bucket.
    storageClient.getClient().buckets().delete(bucketName).execute();
    assertTrue(isDeleted(storageClient.getClient().buckets().get(bucketName)));
  }

  /**
   * @param getRequest on the storage object.
   * @return whether the storage object exists. i.e. whether the requests could be completed.
   */
  protected boolean isDeleted(StorageRequest getRequest) {
    try {
      getRequest.execute();
      return false;
    } catch (IOException ignored) {
    }
    return true;
  }
}
