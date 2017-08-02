package com.truethat.backend.common;

import com.google.appengine.api.datastore.Entity;
import com.truethat.backend.model.InteractionEvent;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Proudly created by ohad on 01/06/2017.
 */
public class TestUtil {
  public static InputStream toInputStream(String s) {
    return new ByteArrayInputStream(s.getBytes());
  }

  public static BufferedReader toBufferedReader(String s) {
    return new BufferedReader(new StringReader(s));
  }

  public static void assertEqualsForEntityAndInteraction(Entity entity,
      InteractionEvent interactionEvent) {
    assertEquals(interactionEvent.getUserId(),
        entity.getProperty(InteractionEvent.DATASTORE_USER_ID));
    assertEquals(interactionEvent.getReactableId(),
        entity.getProperty(InteractionEvent.DATASTORE_REACTABLE_ID));
    assertEquals(interactionEvent.getTimestamp(),
        entity.getProperty(InteractionEvent.DATASTORE_TIMESTAMP));
    // Event code is assumed to be a non null int.
    assertEquals(interactionEvent.getEventType().getCode(),
        ((Long) entity.getProperty(InteractionEvent.DATASTORE_EVENT_TYPE)).intValue());
    Long entityReactionCode = (Long) entity.getProperty(InteractionEvent.DATASTORE_REACTION);
    if (interactionEvent.getReaction() != null) {
      assertEquals(interactionEvent.getReaction().getCode(), entityReactionCode.intValue());
    } else {
      assertNull(entityReactionCode);
    }
  }

  public static void assertEqualsForEntityAndReactable(Entity entity, Reactable reactable) {
    assertEquals(reactable.getId(), entity.getKey().getId());
    assertEquals(reactable.getCreated(), entity.getProperty(Reactable.DATASTORE_CREATED));
    assertEquals(reactable.getDirectorId(), entity.getProperty(Reactable.DATASTORE_DIRECTOR_ID));
    if (Scene.class.getSimpleName().equals(entity.getProperty(Reactable.DATASTORE_TYPE))) {
      assertEquals(((Scene) reactable).getImageSignedUrl(),
          entity.getProperty(Scene.DATASTORE_IMAGE_SIGNED_URL));
    }
  }

  public static void assertUrl(String url, int expectedCode,
      @Nullable InputStream expectedInputStream) throws Exception {
    // Asserts the file can now be accessed.
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.connect();
    assertEquals(expectedCode, connection.getResponseCode());
    if (expectedInputStream != null) {
      assertInputStreamsEqual(expectedInputStream, connection.getInputStream());
    }
    connection.disconnect();
  }

  static void assertInputStreamsEqual(InputStream expected, InputStream actual)
      throws Exception {
    while (expected.available() > 0 && actual.available() > 0) {
      assertEquals(expected.read(), actual.read());
    }
    // Both streams should reach the end.
    assertEquals(0, expected.available());
    assertEquals(0, actual.available());
  }
}
