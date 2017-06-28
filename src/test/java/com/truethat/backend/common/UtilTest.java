package com.truethat.backend.common;

import com.google.gson.JsonElement;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import org.junit.Test;

import static com.truethat.backend.external.RuntimeTypeAdapterFactory.TYPE_FIELD_NAME;
import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 01/06/2017.
 */
public class UtilTest {
  private static final Date DATE = new Date(0);
  private static final String UTC_DATE = "\"1970-01-01T00:00:00.000+0000\"";
  private static final Reactable SCENE = new Scene(1L, DATE, "url");

  @Test
  public void inputStreamToString() throws Exception {
    final String s = "my name is indigo montoya";
    InputStream stream = new ByteArrayInputStream(s.getBytes());
    assertEquals(s, Util.inputStreamToString(stream));
  }

  @Test public void gsonSerialize_date() throws Exception {
    String actual = Util.GSON.toJson(DATE);
    assertEquals(UTC_DATE, actual);
  }

  @Test public void gsonDeserialize_date() throws Exception {
    Date actual = Util.GSON.fromJson(UTC_DATE, Date.class);
    assertEquals(DATE, actual);
  }

  @Test public void gsonSerialize_reactable() throws Exception {
    JsonElement serialized = Util.GSON.toJsonTree(SCENE);
    // Should have type.
    assertEquals(SCENE.getClass().getSimpleName(),
        serialized.getAsJsonObject().get(TYPE_FIELD_NAME).getAsString());
  }

  @Test public void gsonDeserialize_reactable() throws Exception {
    Scene actual =
        (Scene) Util.GSON.fromJson(Util.GSON.toJson(SCENE), Reactable.class);
    assertEquals(SCENE, actual);
  }
}