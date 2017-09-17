package com.truethat.backend.servlet;

import com.truethat.backend.model.Emotion;
import com.truethat.backend.model.EventType;
import com.truethat.backend.model.InteractionEvent;
import com.truethat.backend.model.Photo;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import java.io.IOException;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 03/07/2017.
 */
public class InteractionServletTest extends BaseServletTestSuite {
  private Scene scene;
  private User director =
      new User(DEVICE_ID + "-2", FIRST_NAME, LAST_NAME, NOW);

  @Override public void setUp() throws Exception {
    super.setUp();
    saveUser(director);
    saveUser(defaultUser);
    scene = new Scene(director, NOW, Collections.singletonList(new Photo(0L, "")), null);
  }

  @Test
  public void doPost_viewEvent() throws Exception {
    saveScene(scene);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.VIEW,
            null, 0L);
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
            Emotion.HAPPY, 0L);
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
            Emotion.HAPPY, 0L);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_reactionWitouthReaction() throws Exception {
    saveScene(scene);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTION,
            null, 0L);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_missingUserId() throws Exception {
    saveScene(scene);
    InteractionEvent interactionEvent =
        new InteractionEvent(null, scene.getId(), NOW, EventType.REACTION,
            null, 0L);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_userNotFound() throws Exception {
    saveScene(scene);
    emptyDatastore(User.KIND);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTION,
            null, 0L);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_missingTimestamp() throws Exception {
    saveScene(scene);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), null, EventType.REACTION,
            null, 0L);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_missingSceneId() throws Exception {
    saveScene(scene);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), null, NOW, EventType.REACTION,
            null, 0L);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_missingMediaIndex() throws Exception {
    saveScene(scene);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTION,
            null, null);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_sceneNotFound() throws Exception {
    saveScene(scene);
    emptyDatastore(Scene.KIND);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTION,
            null, 0L);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_mediaIndexOutOfRange() throws Exception {
    saveScene(scene);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTION,
            null, 1L);
    // Saves the event.
    saveInteraction(interactionEvent);
  }
}