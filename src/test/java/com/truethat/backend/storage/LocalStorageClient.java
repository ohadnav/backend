package com.truethat.backend.storage;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Proudly created by ohad on 28/06/2017.
 */
public class LocalStorageClient implements StorageClient {
  private Map<String, Set<String>> bucketToFiles = new HashMap<>();

  @Override public void addBucket(String bucketName) throws IOException {
    bucketToFiles.put(bucketName, new HashSet<>());
  }

  @Override public String save(String destinationName, String contentType, InputStream inputStream,
      String bucketName) throws IOException, GeneralSecurityException {
    if (bucketToFiles.containsKey(bucketName)) {
      bucketToFiles.get(bucketName).add(destinationName);
    } else {
      throw new IOException("Bucket " + bucketName + " does not exist.");
    }
    return destinationName;
  }
}
