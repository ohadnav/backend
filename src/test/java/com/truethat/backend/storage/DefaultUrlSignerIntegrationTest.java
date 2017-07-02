package com.truethat.backend.storage;

import com.google.api.services.storage.model.StorageObject;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.truethat.backend.common.TestUtil;
import com.truethat.backend.common.Util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 30/05/2017.
 */
public class DefaultUrlSignerIntegrationTest extends BaseStorageTestSuite {
  private static final String FILENAME = "richard.branson";
  private static final String CONTENT_TYPE = "text/plain";
  private static final DefaultUrlSigner URL_SIGNER = new DefaultUrlSigner();
  private static String privateKey;

  @BeforeClass
  public static void beforeClass() throws Exception {
    FileInputStream fileInputStream =
        new FileInputStream(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
    String credentialsString = Util.inputStreamToString(fileInputStream);
    JsonObject credentials =
        new GsonBuilder().create().fromJson(credentialsString, JsonElement.class).getAsJsonObject();
    privateKey = credentials.get("private_key").getAsString();
  }

  @Test
  public void sign() throws Exception {
    final String quote = "screw it let\'s do it";
    // Create a temp file to upload
    File tempFile = File.createTempFile(FILENAME.split("\\.")[0], FILENAME.split("\\.")[1]);
    // Write to file.
    FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
    Writer writer = new BufferedWriter(outputStreamWriter);
    writer.write(quote);
    writer.close();
    tempFile.deleteOnExit();
    // Uploads the file
    final String uploaded = storageClient.save(
        FILENAME, CONTENT_TYPE, new FileInputStream(tempFile), bucketName);
    // Asserts that file exists
    final StorageObject found =
        storageClient.getClient().objects().get(bucketName, uploaded).execute();
    assertEquals(found.getName(), FILENAME);
    // Asserts the file cannot be accessed.
    TestUtil.assertUrl(
        DefaultUrlSigner.BASE_GOOGLE_CLOUD_STORAGE_URL + "/" + bucketName + "/" + FILENAME,
        HttpURLConnection.HTTP_FORBIDDEN, null);
    // Asserts the file can be accessed upon signing.
    TestUtil.assertUrl(URL_SIGNER.sign(privateKey, bucketName + "/" + FILENAME),
        HttpURLConnection.HTTP_OK, TestUtil.toInputStream(quote));
  }
}