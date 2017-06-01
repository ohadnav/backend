package com.truethat.backend.storage;

import com.google.api.services.storage.model.StorageObject;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 08/05/2017.
 */
public class StorageUtilTest extends StorageBaseTest {
    private static final String FILENAME     = "bitcoin-gen.txt";
    private static final String CONTENT_TYPE = "text/plain";

    @Test
    public void uploadStream() throws Exception {
        // Uploads the file
        final StorageObject uploaded = StorageUtil.uploadStream(
                FILENAME, CONTENT_TYPE, new ByteArrayInputStream(new byte[]{(byte) 0, (byte) 1}),
                bucketName);
        assertEquals(CONTENT_TYPE, uploaded.getContentType());
        assertEquals(FILENAME, uploaded.getName());
        // Asserts that file exists
        final StorageObject found = client.objects().get(bucketName, uploaded.getName()).execute();
        assertEquals(found.getBucket(), bucketName);
        assertEquals(found.getName(), FILENAME);
    }

    @Test
    public void uploadFile() throws Exception {
        // Create a temp file to upload
        File tempFile = File.createTempFile(FILENAME.split("\\.")[0], "txt");
        tempFile.deleteOnExit();
        // Uploads the file
        final StorageObject uploaded = StorageUtil.uploadStream(
                FILENAME, CONTENT_TYPE, new FileInputStream(tempFile), bucketName);
        assertEquals(CONTENT_TYPE, uploaded.getContentType());
        assertEquals(FILENAME, uploaded.getName());
        // Asserts that file exists
        final StorageObject found = client.objects().get(bucketName, uploaded.getName()).execute();
        assertEquals(found.getBucket(), bucketName);
        assertEquals(found.getName(), FILENAME);
    }
}