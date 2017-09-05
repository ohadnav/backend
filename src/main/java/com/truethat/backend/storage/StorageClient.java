package com.truethat.backend.storage;

import com.google.cloud.storage.BlobInfo;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Proudly created by ohad on 28/06/2017.
 */
public interface StorageClient {
  /**
   * Base google storage URL for public data.
   */
  String STORAGE_BASE_URL = "https://storage.googleapis.com/";

  void addBucket(String bucketName) throws IOException;

  /**
   * Uploads data to a bucket, and makes it publicly available.
   *
   * @param destinationName the name of the destination object.
   * @param contentType     the MIME type of the data.
   * @param bytes           the file to upload.
   * @param bucketName      the name of the bucket to create the object in.
   *
   * @return the saved object
   */
  BlobInfo save(String destinationName, String contentType, byte[] bytes, String bucketName)
      throws IOException,
      GeneralSecurityException;

  /**
   * @param blobInfo of a storage item.
   *
   * @return the public link from which endpoint clients access the storage item.
   */
  String getPublicLink(BlobInfo blobInfo);
}
