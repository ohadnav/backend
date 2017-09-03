package com.truethat.backend.storage;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Proudly created by ohad on 28/06/2017.
 */
public class DefaultStorageClient implements StorageClient {
  private Storage storage;

  public DefaultStorageClient() throws IOException, GeneralSecurityException {
    storage = StorageOptions.getDefaultInstance().getService();
  }

  @Override public void addBucket(String bucketName) throws IOException {
    storage.create(BucketInfo.newBuilder(bucketName).build());
  }

  @Override public BlobInfo save(String destinationName, String contentType, byte[] bytes,
      String bucketName) throws IOException, GeneralSecurityException {
    return storage.create(
        BlobInfo.newBuilder(bucketName, destinationName).setContentType(contentType).build(),
        bytes, Storage.BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));
  }

  Storage getStorage() {
    return storage;
  }
}
