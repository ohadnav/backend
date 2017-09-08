package com.truethat.backend.common;

import com.google.cloud.Timestamp;
import com.google.gson.JsonElement;
import com.truethat.backend.model.Media;
import com.truethat.backend.model.Photo;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import org.junit.Test;

import static com.truethat.backend.external.RuntimeTypeAdapterFactory.TYPE_FIELD_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 01/06/2017.
 */
public class UtilTest {
  private static final Timestamp TIMESTAMP = Timestamp.of(new Date(1));
  private static final String UTC_DATE = "\"1970-01-01T00:00:00.001+0000\"";
  private static final Media MEDIA =
      new Photo("baba.ganush.com");

  @Test
  public void inputStreamToString() throws Exception {
    final String s = "my name is indigo montoya";
    InputStream stream = new ByteArrayInputStream(s.getBytes());
    assertEquals(s, Util.inputStreamToString(stream));
  }

  @Test
  public void inputStreamToString_streamNotReset() throws Exception {
    final String s = "my name is indigo montoya";
    InputStream stream = new ByteArrayInputStream(s.getBytes());
    //noinspection ResultOfMethodCallIgnored
    stream.read();
    assertEquals(s, Util.inputStreamToString(stream));
  }

  @Test public void timestampToDate() throws Exception {
    long timestamp = new Date().getTime();
    assertEquals(new Date(timestamp), Util.timestampToDate(Timestamp.of(new Date(timestamp))));
  }

  @Test public void gsonSerialize_timestamp() throws Exception {
    String actual = Util.GSON.toJson(TIMESTAMP);
    assertEquals(UTC_DATE, actual);
  }

  @Test public void gson_timestampDoesNotShift() throws Exception {
    Timestamp deserialized = Util.GSON.fromJson(Util.GSON.toJson(Timestamp.now()), Timestamp.class);
    Date now = new Date();
    assertTrue(Math.abs(now.getTime() - Util.timestampToDate(deserialized).getTime()) <= 1);
  }

  @Test public void gsonDeserialize_timestamp() throws Exception {
    Timestamp actual = Util.GSON.fromJson(UTC_DATE, Timestamp.class);
    assertEquals(TIMESTAMP, actual);
  }

  @Test public void gsonSerialize_subTypes() throws Exception {
    JsonElement serialized = Util.GSON.toJsonTree(MEDIA);
    // Should have type.
    assertEquals(MEDIA.getClass().getSimpleName(),
        serialized.getAsJsonObject().get(TYPE_FIELD_NAME).getAsString());
  }

  @Test public void gsonDeserialize_subTypes() throws Exception {
    Photo actual =
        (Photo) Util.GSON.fromJson(Util.GSON.toJson(MEDIA), Media.class);
    assertEquals(MEDIA, actual);
  }
}