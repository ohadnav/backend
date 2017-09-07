package com.truethat.backend.servlet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.truethat.backend.model.Emotion;
import com.truethat.backend.model.EventType;
import com.truethat.backend.model.InteractionEvent;
import com.truethat.backend.model.Pose;
import com.truethat.backend.model.User;
import java.util.Collections;
import org.junit.Test;

import static com.truethat.backend.model.Emotion.HAPPY;
import static com.truethat.backend.model.Emotion.SAD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 29/06/2017.
 */
public class ReactableEnricherTest extends BaseServletTestSuite {
  private static final Emotion REACTION = HAPPY;
  private Pose pose;
  private User director, friend;

  @Override public void setUp() throws Exception {
    super.setUp();
    // Adds the director of the pose to datastore
    director = new User(DEVICE_ID + "-2", FIRST_NAME, LAST_NAME, NOW);
    friend = new User("my-phone", "the", "terminator", NOW);
    saveUser(director);
    saveUser(friend);
    // Saves a pose.
    pose = new Pose(director, NOW, null);
    savePose(pose);
    // Adds the user.
    saveUser(defaultUser);
  }

  @Test public void enrichReactable_noReactables() throws Exception {
    // Checks that no exceptions are thrown.
    enricher.enrichReactables(Collections.emptyList(), defaultUser);
  }

  @Test public void enrichReactable_reaction() throws Exception {
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), pose.getId(), NOW, EventType.REACTION,
            REACTION);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Enriches the pose
    enricher.enrichReactables(Collections.singletonList(pose), defaultUser);
    assertEquals(REACTION, pose.getUserReaction());
    assertTrue(
        Maps.difference(ImmutableMap.of(REACTION, 1L), pose.getReactionCounters()).areEqual());
  }

  @Test public void enrichReactable_multipleReactions() throws Exception {
    // Saves a happy reaction.
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), pose.getId(), NOW, EventType.REACTION,
            HAPPY);
    saveInteraction(interactionEvent);
    // Saves a sad reaction.
    interactionEvent =
        new InteractionEvent(friend.getId(), pose.getId(), NOW, EventType.REACTION,
            SAD);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Enriches the pose
    enricher.enrichReactables(Collections.singletonList(pose), defaultUser);
    assertTrue(Maps.difference(ImmutableMap.of(HAPPY, 1L, SAD, 1L), pose.getReactionCounters())
        .areEqual());
  }

  @Test public void enrichReactable_multipleUsers() throws Exception {
    // Saves a happy reaction.
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), pose.getId(), NOW, EventType.REACTION,
            HAPPY);
    saveInteraction(interactionEvent);
    // Saves a sad reaction.
    interactionEvent =
        new InteractionEvent(friend.getId(), pose.getId(), NOW, EventType.REACTION,
            HAPPY);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Enriches the pose
    enricher.enrichReactables(Collections.singletonList(pose), defaultUser);
    assertTrue(Maps.difference(ImmutableMap.of(HAPPY, 2L), pose.getReactionCounters()).areEqual());
  }

  @Test public void enrichReactable_reactionUniqueIds() throws Exception {
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), pose.getId(), NOW, EventType.REACTION,
            REACTION);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Enriches the pose
    enricher.enrichReactables(Collections.singletonList(pose), defaultUser);
    assertTrue(
        Maps.difference(ImmutableMap.of(REACTION, 1L), pose.getReactionCounters()).areEqual());
  }

  @Test public void enrichReactable_view() throws Exception {
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), pose.getId(), NOW, EventType.VIEW,
            null);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Enriches the pose
    enricher.enrichReactables(Collections.singletonList(pose), defaultUser);
    assertTrue(pose.isViewed());
  }

  @Test public void enrichReactable_userIsDirector() throws Exception {
    InteractionEvent interactionEvent =
        new InteractionEvent(director.getId(), pose.getId(), NOW, EventType.REACTION,
            REACTION);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Enriches the pose
    enricher.enrichReactables(Collections.singletonList(pose), director);
    assertTrue(pose.isViewed());
    assertEquals(null, pose.getUserReaction());
    assertTrue(pose.getReactionCounters().isEmpty());
  }

  @Test public void enrichReactable_user() throws Exception {
    // Enriches the pose
    enricher.enrichReactables(Collections.singletonList(pose), defaultUser);
    // Director field should be assigned
    assertEquals(director.getId(), pose.getDirectorId());
    assertEquals(director.getFirstName(), pose.getDirector().getFirstName());
    assertEquals(director.getLastName(), pose.getDirector().getLastName());
    // Private information should not be passed.
    assertNull(pose.getDirector().getDeviceId());
  }
}