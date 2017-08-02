package com.truethat.backend.servlet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.truethat.backend.model.Emotion;
import com.truethat.backend.model.EventType;
import com.truethat.backend.model.Interaction;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import java.util.Collections;
import org.junit.Test;

import static com.truethat.backend.model.Emotion.HAPPY;
import static com.truethat.backend.model.Emotion.SAD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 29/06/2017.
 */
public class ReactableEnricherTest extends BaseServletTestSuite {
  private static final Emotion REACTION = HAPPY;
  private Scene scene;
  private User director, friend;

  @Override public void setUp() throws Exception {
    super.setUp();
    // Adds the director of the scene to datastore
    director = new User(PHONE_NUMBER + "-2", DEVICE_ID + "-2", FIRST_NAME, LAST_NAME, NOW);
    friend = new User("911", "my-phone", "the", "terminator", NOW);
    saveUser(director);
    saveUser(friend);
    // Saves a scene.
    scene = new Scene(director.getId(), NOW, null);
    saveScene(scene);
    // Adds the user.
    saveUser(defaultUser);
  }

  @Test public void enrichReactable_noReactables() throws Exception {
    // Checks that no exceptions are thrown.
    ReactableEnricher.enrich(Collections.emptyList(), defaultUser);
  }

  @Test public void enrichReactable_reaction() throws Exception {
    Interaction interaction =
        new Interaction(defaultUser.getId(), scene.getId(), NOW, EventType.REACTABLE_REACTION,
            REACTION);
    // Saves the event.
    saveInteraction(interaction);
    // Enriches the scene
    ReactableEnricher.enrich(Collections.singletonList(scene), defaultUser);
    assertEquals(REACTION, scene.getUserReaction());
    assertTrue(Maps.difference(ImmutableMap.of(REACTION, 1L), scene.getReactionCounters()).areEqual());
  }

  @Test public void enrichReactable_multipleReactions() throws Exception {
    // Saves a happy reaction.
    Interaction interaction =
        new Interaction(defaultUser.getId(), scene.getId(), NOW, EventType.REACTABLE_REACTION,
            HAPPY);
    saveInteraction(interaction);
    // Saves a sad reaction.
    interaction =
        new Interaction(friend.getId(), scene.getId(), NOW, EventType.REACTABLE_REACTION,
            SAD);
    // Saves the event.
    saveInteraction(interaction);
    // Enriches the scene
    ReactableEnricher.enrich(Collections.singletonList(scene), defaultUser);
    assertTrue(Maps.difference(ImmutableMap.of(HAPPY, 1L, SAD, 1L), scene.getReactionCounters()).areEqual());
  }

  @Test public void enrichReactable_multipleUsers() throws Exception {
    // Saves a happy reaction.
    Interaction interaction =
        new Interaction(defaultUser.getId(), scene.getId(), NOW, EventType.REACTABLE_REACTION,
            HAPPY);
    saveInteraction(interaction);
    // Saves a sad reaction.
    interaction =
        new Interaction(friend.getId(), scene.getId(), NOW, EventType.REACTABLE_REACTION,
            HAPPY);
    // Saves the event.
    saveInteraction(interaction);
    // Enriches the scene
    ReactableEnricher.enrich(Collections.singletonList(scene), defaultUser);
    assertTrue(Maps.difference(ImmutableMap.of(HAPPY, 2L), scene.getReactionCounters()).areEqual());
  }

  @Test public void enrichReactable_reactionUniqueIds() throws Exception {
    Interaction interaction =
        new Interaction(defaultUser.getId(), scene.getId(), NOW, EventType.REACTABLE_REACTION,
            REACTION);
    // Saves the event.
    saveInteraction(interaction);
    // Saves the event.
    saveInteraction(interaction);
    // Enriches the scene
    ReactableEnricher.enrich(Collections.singletonList(scene), defaultUser);
    assertTrue(Maps.difference(ImmutableMap.of(REACTION, 1L), scene.getReactionCounters()).areEqual());
  }

  @Test public void enrichReactable_view() throws Exception {
    Interaction interaction =
        new Interaction(defaultUser.getId(), scene.getId(), NOW, EventType.REACTABLE_VIEW, null);
    // Saves the event.
    saveInteraction(interaction);
    // Enriches the scene
    ReactableEnricher.enrich(Collections.singletonList(scene), defaultUser);
    assertTrue(scene.isViewed());
  }

  @Test public void enrichReactable_userIsDirector() throws Exception {
    Interaction interaction =
        new Interaction(director.getId(), scene.getId(), NOW, EventType.REACTABLE_REACTION,
            REACTION);
    // Saves the event.
    saveInteraction(interaction);
    // Enriches the scene
    ReactableEnricher.enrich(Collections.singletonList(scene), director);
    assertTrue(scene.isViewed());
    assertEquals(null, scene.getUserReaction());
    assertTrue(scene.getReactionCounters().isEmpty());
  }

  @Test public void enrichReactable_user() throws Exception {
    // Enriches the scene
    ReactableEnricher.enrich(Collections.singletonList(scene), defaultUser);
    // Should not have director id as a field.
    assertFalse(scene.hasDirectorId());
    // Director field should be assigned
    assertEquals(director.getId(), scene.getDirectorId());
    assertEquals(director.getFirstName(), scene.getDirector().getFirstName());
    assertEquals(director.getLastName(), scene.getDirector().getLastName());
    // Private information should not be passed.
    assertNull(scene.getDirector().getPhoneNumber());
    assertNull(scene.getDirector().getDeviceId());
  }
}