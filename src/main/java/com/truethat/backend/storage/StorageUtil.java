package com.truethat.backend.storage;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.StorageObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

/**
 * Proudly created by ohad on 08/05/2017.
 */
public class StorageUtil {
    /**
     * Uploads data to an object in a bucket.
     *
     * @param destinationName the name of the destination object.
     * @param contentType     the MIME type of the data.
     * @param inputStream     the file to upload.
     * @param bucketName      the name of the bucket to create the object in.
     * @return the URL of the new object.
     */
    public static StorageObject uploadStream(
            String destinationName, String contentType, InputStream inputStream, String bucketName
    )
            throws IOException, GeneralSecurityException {
        InputStreamContent contentStream = new InputStreamContent(contentType, inputStream);
        // Setting the length improves upload performance
        contentStream.setLength(inputStream.available());
        StorageObject objectMetadata = new StorageObject()
                // Set the destination object name
                .setName(destinationName);

        // Do the insert
        Storage client = StorageFactory.getService();
        Storage.Objects.Insert insertRequest = client.objects().insert(
                bucketName, objectMetadata, contentStream);

        return insertRequest.execute();
    }
}
