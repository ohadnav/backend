package com.truethat.backend.servlet;

import com.truethat.backend.model.Emotion;
import com.truethat.backend.model.EventType;
import com.truethat.backend.model.InteractionEvent;
import com.truethat.backend.model.Photo;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import java.io.IOException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 03/07/2017.
 */
public class InteractionEventServletTest extends BaseServletTestSuite {
  private Scene scene;
  private User director =
      new User(DEVICE_ID + "-2", FIRST_NAME, LAST_NAME, NOW);

  @Override public void setUp() throws Exception {
    super.setUp();
    saveUser(director);
    saveUser(defaultUser);
    scene = new Scene(director, NOW, new Photo(""));
  }

  @Test
  public void doPost_viewEvent() throws Exception {
    saveScene(scene);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.VIEW,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Retrieves the saves event from datastore.
    assertEquals(interactionEvent,
        new InteractionEvent(datastore.get(eventKeyFactory.newKey(interactionEvent.getId()))));
  }

  @Test
  public void doPost_reactionEvent() throws Exception {
    saveScene(scene);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTION,
            Emotion.HAPPY);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Retrieves the saves event from datastore.
    assertEquals(interactionEvent,
        new InteractionEvent(datastore.get(eventKeyFactory.newKey(interactionEvent.getId()))));
  }

  @Test(expected = IOException.class)
  public void invalidEvent_viewWithReaction() throws Exception {
    saveScene(scene);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.VIEW,
            Emotion.HAPPY);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_reactionWitouthReaction() throws Exception {
    saveScene(scene);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTION,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_missingUserId() throws Exception {
    saveScene(scene);
    InteractionEvent interactionEvent =
        new InteractionEvent(null, scene.getId(), NOW, EventType.REACTION,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_userNotFound() throws Exception {
    saveScene(scene);
    emptyDatastore(User.DATASTORE_KIND);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTION,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_missingTimestamp() throws Exception {
    saveScene(scene);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), null, EventType.REACTION,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_missingSceneId() throws Exception {
    saveScene(scene);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), null, NOW, EventType.REACTION,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_sceneNotFound() throws Exception {
    saveScene(scene);
    emptyDatastore(Scene.DATASTORE_KIND);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTION,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
  }
}