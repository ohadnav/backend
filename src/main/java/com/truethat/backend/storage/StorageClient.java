package com.truethat.backend.storage;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

/**
 * Proudly created by ohad on 28/06/2017.
 */
public interface StorageClient {
  void addBucket(String bucketName) throws IOException;

  /**
   * Uploads data to an object in a bucket.
   *
   * @param destinationName the name of the destination object.
   * @param contentType     the MIME type of the data.
   * @param inputStream     the file to upload.
   * @param bucketName      the name of the bucket to create the object in.
   * @return the URL of the new object.
   */
  String save(String destinationName, String contentType, InputStream inputStream, String bucketName) throws IOException,
      GeneralSecurityException;
}
