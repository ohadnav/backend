package com.truethat.backend.servlet;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.StructuredQuery;
import com.google.common.collect.Lists;
import com.truethat.backend.model.Emotion;
import com.truethat.backend.model.EventType;
import com.truethat.backend.model.InteractionEvent;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Proudly created by ohad on 29/06/2017.
 */
class SceneEnricher {
  Datastore datastore;
  private KeyFactory userKeyFactory;

  SceneEnricher(Datastore datastore) {
    this.datastore = datastore;
    userKeyFactory = datastore.newKeyFactory().setKind(User.DATASTORE_KIND);
  }

  /**
   * Enriches {@link Scene}s with data of {@link User} and {@link InteractionEvent}s.
   *
   * @param scenes to enrichScenes
   * @param user   for which to enrichScenes the scenes.
   */
  void enrichScenes(List<Scene> scenes,
      User user) {
    enrichUsers(scenes);
    enrichEvents(scenes, user);
  }

  /**
   * Enriches {@link Scene}s with data of {@link Scene#director} first and last names.
   *
   * @param scenes to enrichScenes
   */
  private void enrichUsers(List<Scene> scenes) {
    Map<Long, List<Scene>> sceneByDirectorId = scenes.stream().collect(groupingBy(
        Scene::getDirectorId, toList()));
    List<Key> directorsEntitiesKeys = sceneByDirectorId.keySet()
        .stream()
        .map(directorId -> userKeyFactory.newKey(directorId))
        .collect(toList());
    Iterator<Entity> directorEntities = datastore.get(directorsEntitiesKeys);
    while (directorEntities.hasNext()) {
      User director = new User(directorEntities.next());
      director.deletePrivateData();
      for (Scene scene : sceneByDirectorId.get(director.getId())) {
        scene.setDirector(director);
      }
    }
  }

  /**
   * Enriches {@link Scene}s with data of {@link Scene#director} first and last names.
   *
   * @param scenes to enrichScenes
   * @param user   for which to enrichScenes the scenes.
   */
  private void enrichEvents(List<Scene> scenes, User user) {
    for (Scene scene : scenes) {
      boolean isUserDirector = Objects.equals(user.getId(), scene.getDirectorId());
      Query<Entity> query = Query.newEntityQueryBuilder().setKind(InteractionEvent.DATASTORE_KIND)
          .setFilter(StructuredQuery.PropertyFilter.eq(InteractionEvent.DATASTORE_SCENE_ID,
              scene.getId())).build();
      List<InteractionEvent> interactionEvents = Lists.newArrayList(datastore.run(query))
          .stream()
          .map(InteractionEvent::new)
          .collect(toList());
      // Calculate reaction counters
      Map<Emotion, Long> reactionCounters = interactionEvents.parallelStream()
          // Filter for reaction event not of the user.
          .filter(
              interaction -> interaction.getEventType() == EventType.REACTION
                  && !Objects.equals(interaction.getUserId(), scene.getDirectorId()))
          // Group by reactions
          .collect(groupingBy(InteractionEvent::getReaction,
              // Group by user IDs, to avoid duplicates
              collectingAndThen(groupingBy(InteractionEvent::getUserId, counting()),
                  userIds -> (long) userIds.keySet().size())));
      scene.setReactionCounters(reactionCounters);
      // Determine user reaction.
      if (!isUserDirector) {
        // Find a reaction event of user.
        Optional<InteractionEvent> reactionEvent = interactionEvents.stream()
            .filter(interaction -> Objects.equals(interaction.getUserId(), user.getId())
                && interaction.getEventType() == EventType.REACTION)
            .findAny();
        reactionEvent.ifPresent(
            interaction -> scene.setUserReaction(interaction.getReaction()));
      }
      // Determine whether {@code scene} was viewed by {@code user}.
      // if the user is the director, then mark as viewed.
      if (isUserDirector) {
        scene.setViewed(true);
      }
      boolean viewed = scene.isViewed();
      if (!viewed) {
        viewed = interactionEvents.stream()
            .anyMatch(interaction -> Objects.equals(interaction.getUserId(), user.getId())
                && interaction.getEventType() == EventType.VIEW);
      }
      scene.setViewed(viewed);
    }
  }
}
