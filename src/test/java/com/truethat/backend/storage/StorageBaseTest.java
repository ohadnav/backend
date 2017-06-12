package com.truethat.backend.storage;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageRequest;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 14/05/2017.
 */
public class StorageBaseTest {
  protected final String bucketName = "test-bucket-" + System.currentTimeMillis();
  protected Storage client;

  /**
   * Sets up the Storage client and creates a dummy bucket.
   */
  @Before
  public void setUp() throws Exception {
    client = StorageFactory.getService();
    Bucket bucket = new Bucket();
    bucket.setName(bucketName);
    // Creates dummy bucket.
    client.buckets().insert(System.getenv("GOOGLE_CLOUD_PROJECT"), bucket).execute();
    // Fetching the bucket to ensure its existence.
    client.buckets().get(bucketName).execute();
  }

  /**
   * Deletes the dummy bucket.
   */
  @After
  public void tearDown() throws Exception {
    Objects remainingObjects = client.objects().list(bucketName).execute();
    if (remainingObjects.containsKey("items")) {
      for (StorageObject object : remainingObjects.getItems()) {
        client.objects().delete(bucketName, object.getName()).execute();
      }
    }
    // Deletes dummy bucket.
    client.buckets().delete(bucketName).execute();
    assertTrue(isDeleted(client.buckets().get(bucketName)));
  }

  /**
   * @param getRequest on the storage object.
   * @return whether the storage object exists. i.e. whether the requests could be completed.
   */
  private boolean isDeleted(StorageRequest getRequest) {
    try {
      getRequest.execute();
      return false;
    } catch (IOException ignored) {
    }
    return true;
  }
}
