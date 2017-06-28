package com.truethat.backend.storage;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

/**
 * Proudly created by ohad on 28/06/2017.
 */
public class DefaultStorageClient implements StorageClient {
  private Storage client;

  public DefaultStorageClient() throws IOException, GeneralSecurityException {
    client = StorageFactory.getService();
  }

  @Override public void addBucket(String bucketName) throws IOException {
    Bucket bucket = new Bucket();
    bucket.setName(bucketName);
    client.buckets().insert(System.getenv("GOOGLE_CLOUD_PROJECT"), bucket).execute();
  }

  @Override public String save(String destinationName, String contentType, InputStream inputStream,
      String bucketName) throws IOException, GeneralSecurityException {
    InputStreamContent contentStream = new InputStreamContent(contentType, inputStream);
    // Setting the length improves upload performance
    contentStream.setLength(inputStream.available());
    StorageObject objectMetadata = new StorageObject()
        // Set the destination object name
        .setName(destinationName);

    // Do the insert
    Storage.Objects.Insert insertRequest =
        client.objects().insert(bucketName, objectMetadata, contentStream);
    return insertRequest.execute().getName();
  }

  public Storage getClient() {
    return client;
  }
}
