package com.truethat.backend.storage;

import com.google.api.services.storage.model.StorageObject;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.truethat.backend.common.Util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 30/05/2017.
 */
public class UrlSignerTest extends StorageBaseTest {
  private static final String FILENAME = "richard.branson";
  private static final String CONTENT_TYPE = "text/plain";

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
  public void getSignedUrl() throws Exception {
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
    final StorageObject uploaded = StorageUtil.uploadStream(
        FILENAME, CONTENT_TYPE, new FileInputStream(tempFile), bucketName);
    // Asserts that file exists
    final StorageObject found = client.objects().get(bucketName, uploaded.getName()).execute();
    assertEquals(found.getName(), FILENAME);
    // Asserts the file cannot be accessed.
    URL url = new URL(UrlSigner.BASE_GOOGLE_CLOUD_STORAGE_URL + "/" + bucketName + "/" + FILENAME);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.connect();
    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, connection.getResponseCode());
    connection.disconnect();
    // Sign the URL (omit base google cloud path).
    URL signedUrl = new URL(UrlSigner.getSignedUrl(privateKey, bucketName + "/" + FILENAME));
    // Asserts the file can now be accessed.
    connection = (HttpURLConnection) signedUrl.openConnection();
    connection.connect();
    assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
    // Asserts the file content are as expected.
    assertEquals(quote, Util.inputStreamToString(connection.getInputStream()));
    connection.disconnect();
  }
}