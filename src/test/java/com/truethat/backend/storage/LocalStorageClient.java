package com.truethat.backend.storage;

import com.google.cloud.storage.BlobInfo;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 28/06/2017.
 */
public class LocalStorageClient implements StorageClient {
  private Map<String, Set<String>> bucketToFiles = new HashMap<>();

  @Override public void addBucket(String bucketName) throws IOException {
    bucketToFiles.put(bucketName, new HashSet<>());
  }

  @Override public BlobInfo save(String destinationName, String contentType, byte[] bytes,
      String bucketName) throws IOException, GeneralSecurityException {
    if (bucketToFiles.containsKey(bucketName)) {
      bucketToFiles.get(bucketName).add(destinationName);
    } else {
      throw new IOException("Bucket " + bucketName + " does not exist.");
    }
    BlobInfo blobInfo = mock(BlobInfo.class);
    when(blobInfo.getName()).thenReturn(destinationName);
    when(blobInfo.getMediaLink()).thenReturn(destinationName);
    return blobInfo;
  }
}
