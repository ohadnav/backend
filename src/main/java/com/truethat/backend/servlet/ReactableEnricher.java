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
import com.truethat.backend.model.Reactable;
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
class ReactableEnricher {
  Datastore datastore;
  private KeyFactory userKeyFactory;

  ReactableEnricher(Datastore datastore) {
    this.datastore = datastore;
    userKeyFactory = datastore.newKeyFactory().setKind(User.DATASTORE_KIND);
  }

  /**
   * Enriches {@link Reactable}s with data of {@link User} and {@link InteractionEvent}s.
   *  @param reactables to enrichReactables
   * @param user       for which to enrichReactables the reactables.
   */
  void enrichReactables(List<Reactable> reactables,
      User user) {
    enrichUsers(reactables);
    enrichEvents(reactables, user);
  }

  /**
   * Enriches {@link Reactable}s with data of {@link Reactable#director} first and last names.
   *
   * @param reactables to enrichReactables
   */
  private void enrichUsers(List<Reactable> reactables) {
    Map<Long, List<Reactable>> reactableByDirectorId = reactables.stream().collect(groupingBy(
        Reactable::getDirectorId, toList()));
    List<Key> directorsEntitiesKeys = reactableByDirectorId.keySet()
        .stream()
        .map(directorId -> userKeyFactory.newKey(directorId))
        .collect(toList());
    Iterator<Entity> directorEntities = datastore.get(directorsEntitiesKeys);
    while (directorEntities.hasNext()) {
      User director = new User(directorEntities.next());
      director.deletePrivateData();
      for (Reactable reactable : reactableByDirectorId.get(director.getId())) {
        reactable.setDirector(director);
      }
    }
  }

  /**
   * Enriches {@link Reactable}s with data of {@link Reactable#director} first and last names.
   *  @param reactables to enrichReactables
   * @param user       for which to enrichReactables the reactables.
   */
  private void enrichEvents(List<Reactable> reactables, User user) {
    for (Reactable reactable : reactables) {
      boolean isUserDirector = Objects.equals(user.getId(), reactable.getDirectorId());
      Query<Entity> query = Query.newEntityQueryBuilder().setKind(InteractionEvent.DATASTORE_KIND)
          .setFilter(StructuredQuery.PropertyFilter.eq(InteractionEvent.DATASTORE_REACTABLE_ID,
              reactable.getId())).build();
      List<InteractionEvent> interactionEvents = Lists.newArrayList(datastore.run(query))
          .stream()
          .map(InteractionEvent::new)
          .collect(toList());
      // Calculate reaction counters
      Map<Emotion, Long> reactionCounters = interactionEvents.parallelStream()
          // Filter for reaction event not of the user.
          .filter(
              interaction -> interaction.getEventType() == EventType.REACTABLE_REACTION
                  && !Objects.equals(interaction.getUserId(), reactable.getDirectorId()))
          // Group by reactions
          .collect(groupingBy(InteractionEvent::getReaction,
              // Group by user IDs, to avoid duplicates
              collectingAndThen(groupingBy(InteractionEvent::getUserId, counting()),
                  userIds -> (long) userIds.keySet().size())));
      reactable.setReactionCounters(reactionCounters);
      // Determine user reaction.
      if (!isUserDirector) {
        // Find a reaction event of user.
        Optional<InteractionEvent> reactionEvent = interactionEvents.stream()
            .filter(interaction -> Objects.equals(interaction.getUserId(), user.getId())
                && interaction.getEventType() == EventType.REACTABLE_REACTION)
            .findAny();
        reactionEvent.ifPresent(
            interaction -> reactable.setUserReaction(interaction.getReaction()));
      }
      // Determine whether {@code reactable} was viewed by {@code user}.
      // if the user is the director, then mark as viewed.
      if (isUserDirector) {
        reactable.setViewed(true);
      }
      boolean viewed = reactable.isViewed();
      if (!viewed) {
        viewed = interactionEvents.stream()
            .anyMatch(interaction -> Objects.equals(interaction.getUserId(), user.getId())
                && interaction.getEventType() == EventType.REACTABLE_VIEW);
      }
      reactable.setViewed(viewed);
    }
  }
}
