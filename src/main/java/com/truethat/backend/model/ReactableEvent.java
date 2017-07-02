package com.truethat.backend.model;

import com.google.appengine.api.datastore.Entity;
import com.google.common.annotations.VisibleForTesting;
import java.util.Date;
import javax.annotation.Nullable;

/**
 * Proudly created by ohad on 11/06/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/ReactableEvent.java</a>
 */
@SuppressWarnings("FieldCanBeLocal") public class ReactableEvent {
  /**
   * Datastore kind.
   */
  public static final String DATASTORE_KIND = "ReactableEvent";
  /**
   * Datastore column names.
   */
  public static final String DATASTORE_TIMESTAMP = "timestamp";
  public static final String DATASTORE_USER_ID = "userId";
  public static final String DATASTORE_REACTABLE_ID = "reactableId";
  public static final String DATASTORE_EVENT_TYPE = "eventType";
  public static final String DATASTORE_REACTION = "reaction";

  /**
   * ReactableEvent ID, as defined by its datastore key.
   */
  @SuppressWarnings("unused") private Long id;

  /**
   * Client UTC timestamp
   */
  private Date timestamp;

  /**
   * ID of the user that triggered the event.
   */
  private long userId;

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
  private long reactableId;

  public ReactableEvent(Entity entity) {
    if (entity.getProperty(DATASTORE_USER_ID) != null) {
      userId = (Long) entity.getProperty(DATASTORE_USER_ID);
    }
    if (entity.getProperty(DATASTORE_EVENT_TYPE) != null) {
      eventType = EventType.fromCode(((Long) entity.getProperty(DATASTORE_EVENT_TYPE)).intValue());
    }
    if (entity.getProperty(DATASTORE_REACTION) != null) {
      reaction = Emotion.fromCode(((Long) entity.getProperty(DATASTORE_REACTION)).intValue());
    }
    if (entity.getProperty(DATASTORE_REACTABLE_ID) != null) {
      reactableId = (Long) entity.getProperty(DATASTORE_REACTABLE_ID);
    }
    if (entity.getProperty(DATASTORE_TIMESTAMP) != null) {
      timestamp = (Date) entity.getProperty(DATASTORE_TIMESTAMP);
    }
    id = entity.getKey().getId();
  }
  @VisibleForTesting
  public ReactableEvent(long userId, long reactableId, Date timestamp, EventType eventType,
      @Nullable
      Emotion reaction) {
    this.timestamp = timestamp;
    this.userId = userId;
    this.reaction = reaction;
    this.eventType = eventType;
    this.reactableId = reactableId;
  }

  public Entity toEntity() {
    Entity entity = new Entity(ReactableEvent.DATASTORE_KIND);
    entity.setProperty(ReactableEvent.DATASTORE_REACTABLE_ID, reactableId);
    entity.setProperty(ReactableEvent.DATASTORE_TIMESTAMP, timestamp);
    if (eventType != null) {
      entity.setProperty(ReactableEvent.DATASTORE_EVENT_TYPE, eventType.getCode());
    }
    entity.setProperty(ReactableEvent.DATASTORE_USER_ID, userId);
    if (reaction != null) {
      entity.setProperty(ReactableEvent.DATASTORE_REACTION, reaction.getCode());
    }
    return entity;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
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

  public long getReactableId() {
    return reactableId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
