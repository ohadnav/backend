package com.truethat.backend.servlet;

import com.truethat.backend.model.Emotion;
import com.truethat.backend.model.EventType;
import com.truethat.backend.model.InteractionEvent;
import com.truethat.backend.model.Pose;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.User;
import java.io.IOException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 03/07/2017.
 */
public class InteractionEventServletTest extends BaseServletTestSuite {
  private Pose pose;
  private User director =
      new User(DEVICE_ID + "-2", FIRST_NAME, LAST_NAME, NOW);

  @Override public void setUp() throws Exception {
    super.setUp();
    saveUser(director);
    saveUser(defaultUser);
    pose = new Pose(director, NOW, null);
  }

  @Test
  public void doPost_viewEvent() throws Exception {
    savePose(pose);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), pose.getId(), NOW, EventType.REACTABLE_VIEW,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Retrieves the saves event from datastore.
    assertEquals(interactionEvent,
        new InteractionEvent(datastore.get(eventKeyFactory.newKey(interactionEvent.getId()))));
  }

  @Test
  public void doPost_reactionEvent() throws Exception {
    savePose(pose);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), pose.getId(), NOW, EventType.REACTABLE_REACTION,
            Emotion.HAPPY);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Retrieves the saves event from datastore.
    assertEquals(interactionEvent,
        new InteractionEvent(datastore.get(eventKeyFactory.newKey(interactionEvent.getId()))));
  }

  @Test(expected = IOException.class)
  public void invalidEvent_viewWithReaction() throws Exception {
    savePose(pose);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), pose.getId(), NOW, EventType.REACTABLE_VIEW,
            Emotion.HAPPY);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_reactionWitouthReaction() throws Exception {
    savePose(pose);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), pose.getId(), NOW, EventType.REACTABLE_REACTION,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_missingUserId() throws Exception {
    savePose(pose);
    InteractionEvent interactionEvent =
        new InteractionEvent(null, pose.getId(), NOW, EventType.REACTABLE_REACTION,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_userNotFound() throws Exception {
    savePose(pose);
    emptyDatastore(User.DATASTORE_KIND);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), pose.getId(), NOW, EventType.REACTABLE_REACTION,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_missingTimestamp() throws Exception {
    savePose(pose);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), pose.getId(), null, EventType.REACTABLE_REACTION,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_missingReactableId() throws Exception {
    savePose(pose);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), null, NOW, EventType.REACTABLE_REACTION,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
  }

  @Test(expected = IOException.class)
  public void invalidEvent_reactableNotFound() throws Exception {
    savePose(pose);
    emptyDatastore(Reactable.DATASTORE_KIND);
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), pose.getId(), NOW, EventType.REACTABLE_REACTION,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
  }
}