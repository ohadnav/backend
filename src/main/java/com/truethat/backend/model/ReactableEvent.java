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
public class ReactableEvent {
  /**
   * Datastore kind.
   */
  public static final String DATASTORE_KIND = "ReactableEvent";
  /**
   * Datastore column names.
   */
  public static final String DATASTORE_TIMESTAMP = "timestamp";
  public static final String DATASTORE_USER_ID = "userId";
  public static final String DATASTORE_SCENE_ID = "sceneId";
  public static final String DATASTORE_EVENT_TYPE = "eventType";
  public static final String DATASTORE_REACTION = "reaction";

  /**
   * ReactableEvent ID, as defined by its datastore key.
   */
  @SuppressWarnings("unused") private long id;

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
   * Of the {@link Scene} that was interacted with.
   */
  private long sceneId;

  @VisibleForTesting
  public ReactableEvent(long userId, long sceneId, Date timestamp, EventType eventType, @Nullable
      Emotion reaction) {
    this.timestamp = timestamp;
    this.userId = userId;
    this.reaction = reaction;
    this.eventType = eventType;
    this.sceneId = sceneId;
  }

  public Entity toEntity() {
    Entity entity = new Entity(ReactableEvent.DATASTORE_KIND);
    entity.setProperty(ReactableEvent.DATASTORE_SCENE_ID, sceneId);
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

  public long getUserId() {
    return userId;
  }

  public Emotion getReaction() {
    return reaction;
  }

  public EventType getEventType() {
    return eventType;
  }

  public long getSceneId() {
    return sceneId;
  }
}
