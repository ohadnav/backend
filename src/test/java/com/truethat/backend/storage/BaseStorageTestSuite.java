package com.truethat.backend.storage;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import org.junit.After;
import org.junit.Before;

/**
 * Proudly created by ohad on 14/05/2017.
 */
public class BaseStorageTestSuite {
  protected final String bucketName = "test-bucket-" + System.currentTimeMillis();
  DefaultStorageClient storage;
  Bucket bucket;

  /**
   * Sets up the Storage storage and creates a dummy bucket.
   */
  @Before
  public void setUp() throws Exception {
    storage = new DefaultStorageClient();
    // Creates dummy bucket.
    storage.addBucket(bucketName);
    bucket = storage.getStorage().get(bucketName);
  }

  /**
   * Deletes the dummy bucket.
   */
  @After
  public void tearDown() throws Exception {
    if (bucket.exists()) {
      Iterable<Blob> blobsIterable = bucket.list().iterateAll();
      for (Blob blob : blobsIterable) {
        blob.delete();
      }
      bucket.delete();
    }
  }
}
