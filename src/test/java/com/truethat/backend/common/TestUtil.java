package com.truethat.backend.common;

import com.google.appengine.api.datastore.Entity;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.ReactableEvent;
import com.truethat.backend.model.Scene;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Proudly created by ohad on 01/06/2017.
 */
public class TestUtil {
  public static InputStream toInputStream(String s) {
    return new ByteArrayInputStream(s.getBytes());
  }

  public static void assertEqualsForEntityAndReactableEvent(Entity entity,
      ReactableEvent reactableEvent) {
    assertEquals(reactableEvent.getUserId(), entity.getProperty(ReactableEvent.DATASTORE_USER_ID));
    assertEquals(reactableEvent.getReactableId(),
        entity.getProperty(ReactableEvent.DATASTORE_REACTABLE_ID));
    assertEquals(reactableEvent.getTimestamp(),
        entity.getProperty(ReactableEvent.DATASTORE_TIMESTAMP));
    // Event code is assumed to be a non null int.
    assertEquals(reactableEvent.getEventType().getCode(),
        ((Long) entity.getProperty(ReactableEvent.DATASTORE_EVENT_TYPE)).intValue());
    Long entityReactionCode = (Long) entity.getProperty(ReactableEvent.DATASTORE_REACTION);
    if (reactableEvent.getReaction() != null) {
      assertEquals(reactableEvent.getReaction().getCode(), entityReactionCode.intValue());
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
}
