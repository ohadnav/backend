package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.truethat.backend.model.Emotion;
import com.truethat.backend.model.EventType;
import com.truethat.backend.model.ReactableEvent;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import org.junit.Test;

import static com.truethat.backend.common.TestUtil.assertEqualsForEntityAndReactableEvent;

/**
 * Proudly created by ohad on 03/07/2017.
 */
public class InteractionServletTest extends BaseServletTestSuite {
  private Scene scene;
  private User director =
      new User(PHONE_NUMBER + "-2", DEVICE_ID + "-2", FIRST_NAME, LAST_NAME, NOW);

  @Override public void setUp() throws Exception {
    super.setUp();
    saveUser(director);
    saveUser(defaultUser);
    scene = new Scene(director.getId(), NOW, null);
  }

  @Test
  public void doPost_viewEvent() throws Exception {
    saveScene(scene);
    ReactableEvent reactableEvent =
        new ReactableEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTABLE_VIEW, null);
    // Saves the event.
    saveReactableEvent(reactableEvent);
    // Retrieves the saves event from datastore.
    Entity savedEntity =
        datastoreService.prepare(new Query(ReactableEvent.DATASTORE_KIND)).asSingleEntity();
    assertEqualsForEntityAndReactableEvent(savedEntity, reactableEvent);
  }

  @Test
  public void doPost_reactionEvent() throws Exception {
    saveScene(scene);
    ReactableEvent reactableEvent =
        new ReactableEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTABLE_REACTION,
            Emotion.HAPPY);
    // Saves the event.
    saveReactableEvent(reactableEvent);
    // Retrieves the saves event from datastore.
    Entity savedEntity =
        datastoreService.prepare(new Query(ReactableEvent.DATASTORE_KIND)).asSingleEntity();
    assertEqualsForEntityAndReactableEvent(savedEntity, reactableEvent);
  }
}