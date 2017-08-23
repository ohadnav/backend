package com.truethat.backend.storage;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.common.io.ByteStreams;
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

import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 30/05/2017.
 */
public class DefaultUrlSignerIntegrationTest extends BaseStorageTestSuite {
  private static final String FILENAME = "richard.branson";
  private static final String CONTENT_TYPE = "text/plain";
  private DefaultUrlSigner urlSigner;
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

  @Override public void setUp() throws Exception {
    super.setUp();
    urlSigner = new DefaultUrlSigner(privateKey);
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
    final BlobInfo uploaded = storage.save(
        FILENAME, CONTENT_TYPE, ByteStreams.toByteArray(new FileInputStream(tempFile)), bucketName);
    // Asserts that file exists
    Blob blob = bucket.get(uploaded.getName());
    assertTrue(blob.exists());
    // Asserts the file cannot be accessed.
    TestUtil.assertUrl(
        DefaultUrlSigner.BASE_GOOGLE_CLOUD_STORAGE_URL + "/" + bucketName + "/" + FILENAME,
        HttpURLConnection.HTTP_FORBIDDEN, null);
    // Asserts the file can be accessed upon signing.
    TestUtil.assertUrl(urlSigner.sign(bucketName + "/" + FILENAME),
        HttpURLConnection.HTTP_OK, TestUtil.toInputStream(quote));
  }
}