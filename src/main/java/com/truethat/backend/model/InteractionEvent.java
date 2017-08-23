package com.truethat.backend.model;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;

/**
 * Proudly created by ohad on 11/06/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/InteractionEvent.java</a>
 */
@SuppressWarnings("FieldCanBeLocal") public class InteractionEvent extends BaseModel {
  /**
   * Datastore kind.
   */
  public static final String DATASTORE_KIND = "InteractionEvent";
  /**
   * Datastore column names.
   */
  private static final String DATASTORE_TIMESTAMP = "timestamp";
  private static final String DATASTORE_USER_ID = "userId";
  public static final String DATASTORE_REACTABLE_ID = "reactableId";
  private static final String DATASTORE_EVENT_TYPE = "eventType";
  private static final String DATASTORE_REACTION = "reaction";

  /**
   * Client UTC timestamp
   */
  private Timestamp timestamp;

  /**
   * ID of the user that triggered the event.
   */
  private Long userId;

  /**
   * For {@link EventType#REACTABLE_REACTION}.
   * <p>
   * Must be null for irrelevant events (such as {@link EventType#REACTABLE_VIEW}).
   */
  private Emotion reaction;

  /**
   * Event type, to sync with frontend clients.
   */
  private EventType eventType;

  /**
   * Of the {@link Reactable} that was interacted with.
   */
  private Long reactableId;

  public InteractionEvent(Entity entity) {
    super(entity);
    if (entity.contains(DATASTORE_USER_ID)) {
      userId = entity.getLong(DATASTORE_USER_ID);
    }
    if (entity.contains(DATASTORE_EVENT_TYPE)) {
      eventType = EventType.fromCode((int) entity.getLong(DATASTORE_EVENT_TYPE));
    }
    if (entity.contains(DATASTORE_REACTION)) {
      reaction = Emotion.fromCode((int) entity.getLong(DATASTORE_REACTION));
    }
    if (entity.contains(DATASTORE_REACTABLE_ID)) {
      reactableId = entity.getLong(DATASTORE_REACTABLE_ID);
    }
    if (entity.contains(DATASTORE_TIMESTAMP)) {
      timestamp = entity.getTimestamp(DATASTORE_TIMESTAMP);
    }
  }
  @VisibleForTesting
  public InteractionEvent(long userId, long reactableId, Timestamp timestamp, EventType eventType,
      @Nullable
      Emotion reaction) {
    this.timestamp = timestamp;
    this.userId = userId;
    this.reaction = reaction;
    this.eventType = eventType;
    this.reactableId = reactableId;
  }

  /**
   * @return whether the event has a valid data.
   */
  public boolean isValid() {
    if (eventType == null) return false;
    if (eventType == EventType.REACTABLE_VIEW) return reaction == null;
    if (eventType == EventType.REACTABLE_REACTION) return reaction != null;
    // Should not reach here.
    return false;
  }

  @Override public FullEntity.Builder<IncompleteKey> toEntityBuilder(KeyFactory keyFactory) {
    FullEntity.Builder<IncompleteKey> builder = super.toEntityBuilder(keyFactory);
    if (reactableId != null) {
      builder.set(InteractionEvent.DATASTORE_REACTABLE_ID, reactableId);
    }
    if (timestamp != null) {
      builder.set(InteractionEvent.DATASTORE_TIMESTAMP, timestamp);
    }
    if (eventType != null) {
      builder.set(InteractionEvent.DATASTORE_EVENT_TYPE, eventType.getCode());
    }
    if (userId != null) {
      builder.set(InteractionEvent.DATASTORE_USER_ID, userId);
    }
    if (reaction != null) {
      builder.set(InteractionEvent.DATASTORE_REACTION, reaction.getCode());
    }
    return builder;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
    result = 31 * result + (userId != null ? userId.hashCode() : 0);
    result = 31 * result + (reaction != null ? reaction.hashCode() : 0);
    result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
    result = 31 * result + (reactableId != null ? reactableId.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InteractionEvent)) return false;
    if (!super.equals(o)) return false;

    InteractionEvent that = (InteractionEvent) o;

    if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) {
      return false;
    }
    if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
    if (reaction != that.reaction) return false;
    if (eventType != that.eventType) return false;
    return reactableId != null ? reactableId.equals(that.reactableId) : that.reactableId == null;
  }

  public long getUserId() {
    return userId;
  }

  public Emotion getReaction() {
    return reaction;
  }

  public EventType getEventType() {
    return eventType;
  }

  public Timestamp getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
  }
}
