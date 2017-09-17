package com.truethat.backend.servlet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.truethat.backend.model.Emotion;
import com.truethat.backend.model.EventType;
import com.truethat.backend.model.InteractionEvent;
import com.truethat.backend.model.Photo;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import java.util.Collections;
import org.junit.Test;

import static com.truethat.backend.model.Emotion.HAPPY;
import static com.truethat.backend.model.Emotion.SURPRISE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 29/06/2017.
 */
public class SceneEnricherTest extends BaseServletTestSuite {
  private static final Emotion REACTION = HAPPY;
  private Scene scene;
  private User director, friend;

  @Override public void setUp() throws Exception {
    super.setUp();
    // Adds the director of the scene to datastore
    director = new User(DEVICE_ID + "-2", FIRST_NAME, LAST_NAME, NOW);
    friend = new User("my-phone", "the", "terminator", NOW);
    saveUser(director);
    saveUser(friend);
    // Saves a scene.
    scene = new Scene(director, NOW, Collections.singletonList(new Photo(0L, "")), null);
    saveScene(scene);
    // Adds the user.
    saveUser(defaultUser);
  }

  @Test public void enrichScene_noScenes() throws Exception {
    // Checks that no exceptions are thrown.
    enricher.enrichScenes(Collections.emptyList(), defaultUser);
  }

  @Test public void enrichScene_reaction() throws Exception {
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTION,
            REACTION, 0L);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Enriches the scene
    enricher.enrichScenes(Collections.singletonList(scene), defaultUser);
    assertEquals(REACTION, scene.getUserReaction());
    assertTrue(
        Maps.difference(ImmutableMap.of(REACTION, 1L), scene.getReactionCounters()).areEqual());
  }

  @Test public void enrichScene_multipleReactions() throws Exception {
    // Saves a happy reaction.
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTION,
            HAPPY, 0L);
    saveInteraction(interactionEvent);
    // Saves a sad reaction.
    interactionEvent =
        new InteractionEvent(friend.getId(), scene.getId(), NOW, EventType.REACTION,
            SURPRISE, 0L);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Enriches the scene
    enricher.enrichScenes(Collections.singletonList(scene), defaultUser);
    assertTrue(
        Maps.difference(ImmutableMap.of(HAPPY, 1L, SURPRISE, 1L), scene.getReactionCounters())
        .areEqual());
  }

  @Test public void enrichScene_multipleUsers() throws Exception {
    // Saves a happy reaction.
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTION,
            HAPPY, 0L);
    saveInteraction(interactionEvent);
    // Saves a sad reaction.
    interactionEvent =
        new InteractionEvent(friend.getId(), scene.getId(), NOW, EventType.REACTION,
            HAPPY, 0L);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Enriches the scene
    enricher.enrichScenes(Collections.singletonList(scene), defaultUser);
    assertTrue(Maps.difference(ImmutableMap.of(HAPPY, 2L), scene.getReactionCounters()).areEqual());
  }

  @Test public void enrichScene_reactionUniqueIds() throws Exception {
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTION,
            REACTION, 0L);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Enriches the scene
    enricher.enrichScenes(Collections.singletonList(scene), defaultUser);
    assertTrue(
        Maps.difference(ImmutableMap.of(REACTION, 1L), scene.getReactionCounters()).areEqual());
  }

  @Test public void enrichScene_view() throws Exception {
    InteractionEvent interactionEvent =
        new InteractionEvent(defaultUser.getId(), scene.getId(), NOW, EventType.VIEW,
            null, 0L);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Enriches the scene
    enricher.enrichScenes(Collections.singletonList(scene), defaultUser);
    assertTrue(scene.isViewed());
  }

  @Test public void enrichScene_userIsDirector() throws Exception {
    InteractionEvent interactionEvent =
        new InteractionEvent(director.getId(), scene.getId(), NOW, EventType.REACTION,
            REACTION, 0L);
    // Saves the event.
    saveInteraction(interactionEvent);
    // Enriches the scene
    enricher.enrichScenes(Collections.singletonList(scene), director);
    assertTrue(scene.isViewed());
    assertEquals(null, scene.getUserReaction());
    assertTrue(scene.getReactionCounters().isEmpty());
  }

  @Test public void enrichScene_user() throws Exception {
    // Enriches the scene
    enricher.enrichScenes(Collections.singletonList(scene), defaultUser);
    // Director field should be assigned
    assertEquals(director.getId(), scene.getDirectorId());
    assertEquals(director.getFirstName(), scene.getDirector().getFirstName());
    assertEquals(director.getLastName(), scene.getDirector().getLastName());
    // Private information should not be passed.
    assertNull(scene.getDirector().getDeviceId());
  }
}