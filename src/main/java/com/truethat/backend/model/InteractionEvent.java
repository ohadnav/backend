package com.truethat.backend.model;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.common.annotations.VisibleForTesting;
import com.truethat.backend.servlet.BaseServlet;
import javax.annotation.Nullable;

/**
 * Proudly created by ohad on 11/06/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/InteractionEvent.java</a>
 * @ios <a>https://github.com/true-that/ios/blob/master/TrueThat/Model/InteractionEvent.swift</a>
 */
@SuppressWarnings("FieldCanBeLocal") public class InteractionEvent extends BaseModel {
  /**
   * Datastore kind.
   */
  public static final String KIND = "InteractionEvent";
  /**
   * Datastore column names.
   */
  public static final String COLUMN_SCENE_ID = "sceneId";
  private static final String COLUMN_TIMESTAMP = "timestamp";
  private static final String COLUMN_USER_ID = "userId";
  private static final String COLUMN_EVENT_TYPE = "eventType";
  private static final String COLUMN_REACTION = "reaction";
  private static final String COLUMN_MEDIA_ID = "mediaId";

  /**
   * Client UTC timestamp
   */
  private Timestamp timestamp;

  /**
   * ID of the user that triggered the event.
   */
  private Long userId;

  /**
   * For {@link EventType#REACTION}.
   * <p>
   * Must be null for irrelevant events (such as {@link EventType#VIEW}).
   */
  private Emotion reaction;

  /**
   * Event type, to sync with frontend clients.
   */
  private EventType eventType;

  /**
   * Of the {@link Scene} that was interacted with.
   */
  private Long sceneId;
  /**
   * The {@link Media} index within {@link Scene#mediaNodes}.
   */
  private Long mediaId;

  public InteractionEvent(FullEntity entity) {
    super(entity);
    if (entity.contains(COLUMN_USER_ID)) {
      userId = entity.getLong(COLUMN_USER_ID);
    }
    if (entity.contains(COLUMN_EVENT_TYPE)) {
      eventType = EventType.fromCode((int) entity.getLong(COLUMN_EVENT_TYPE));
    }
    if (entity.contains(COLUMN_REACTION)) {
      reaction = Emotion.fromCode((int) entity.getLong(COLUMN_REACTION));
    }
    if (entity.contains(COLUMN_SCENE_ID)) {
      sceneId = entity.getLong(COLUMN_SCENE_ID);
    }
    if (entity.contains(COLUMN_MEDIA_ID)) {
      mediaId = entity.getLong(COLUMN_MEDIA_ID);
    }
    if (entity.contains(COLUMN_TIMESTAMP)) {
      timestamp = entity.getTimestamp(COLUMN_TIMESTAMP);
    }
  }

  @VisibleForTesting
  public InteractionEvent(Long userId, Long sceneId, Timestamp timestamp, EventType eventType,
      @Nullable Emotion reaction, Long mediaId) {
    this.timestamp = timestamp;
    this.userId = userId;
    this.reaction = reaction;
    this.eventType = eventType;
    this.sceneId = sceneId;
    this.mediaId = mediaId;
  }

  @Override public FullEntity.Builder<IncompleteKey> toEntityBuilder(BaseServlet servlet) {
    FullEntity.Builder<IncompleteKey> builder = super.toEntityBuilder(servlet);
    if (sceneId != null) {
      builder.set(InteractionEvent.COLUMN_SCENE_ID, sceneId);
    }
    if (mediaId != null) {
      builder.set(InteractionEvent.COLUMN_MEDIA_ID, mediaId);
    }
    if (timestamp != null) {
      builder.set(InteractionEvent.COLUMN_TIMESTAMP, timestamp);
    }
    if (eventType != null) {
      builder.set(InteractionEvent.COLUMN_EVENT_TYPE, eventType.getCode());
    }
    if (userId != null) {
      builder.set(InteractionEvent.COLUMN_USER_ID, userId);
    }
    if (reaction != null) {
      builder.set(InteractionEvent.COLUMN_REACTION, reaction.getCode());
    }
    return builder;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
    result = 31 * result + (userId != null ? userId.hashCode() : 0);
    result = 31 * result + (reaction != null ? reaction.hashCode() : 0);
    result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
    result = 31 * result + (sceneId != null ? sceneId.hashCode() : 0);
    result = 31 * result + (mediaId != null ? mediaId.hashCode() : 0);
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
    if (sceneId != null ? !sceneId.equals(that.sceneId) : that.sceneId != null) return false;
    return mediaId != null ? mediaId.equals(that.mediaId) : that.mediaId == null;
  }

  @Override String getKind() {
    return KIND;
  }

  public Long getMediaId() {
    return mediaId;
  }

  public Long getUserId() {
    return userId;
  }

  public Long getSceneId() {
    return sceneId;
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
