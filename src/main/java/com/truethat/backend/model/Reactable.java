package com.truethat.backend.model;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;
import com.google.common.annotations.VisibleForTesting;
import com.truethat.backend.common.Util;
import com.truethat.backend.servlet.StudioServlet;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

/**
 * Proudly created by ohad on 27/06/2017.
 * <p>
 * A media item that the user can be reacted to, such as a {@link Scene}.
 * <p>
 * Each implementation should register at {@link Util#GSON}.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/Reactable.java</a>
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"}) public abstract class Reactable extends BaseModel {
  /**
   * Multipart HTTP request part names, as used by backend endpoints such as {@link
   * com.truethat.backend.servlet.StudioServlet}.
   *
   * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/StudioAPI.java</a>
   */
  public static final String REACTABLE_PART = "reactable";
  /**
   * Datastore kind.
   */
  public static final String DATASTORE_KIND = "Reactable";
  /**
   * Datastore column names.
   */
  private static final String DATASTORE_CREATED = "created";
  public static final String DATASTORE_DIRECTOR_ID = "directorId";
  private static final String DATASTORE_TYPE = "type";
  /**
   * ID of the Reactable director (i.e. its creator).
   */
  private Long directorId;
  /**
   * Client created UTC timestamp
   */
  private Timestamp created;
  /**
   * Whether the reactable was viewed by the user.
   */
  private boolean viewed;
  /**
   * Counters of emotional reactions to the reactable, per each emotion.
   */
  private Map<Emotion, Long> reactionCounters;
  /**
   * The user reaction to the reactable, {@code null} for no reaction.
   */
  private Emotion userReaction;
  /**
   * Reactable director (i.e. its creator). This field is what eventually will be returned to client
   * endpoints.
   */
  private User director;

  public Reactable(Entity entity) {
    super(entity);
    if (entity.contains(DATASTORE_DIRECTOR_ID)) {
      directorId = entity.getLong(DATASTORE_DIRECTOR_ID);
    }
    if (entity.contains(DATASTORE_CREATED)) {
      created = entity.getTimestamp(DATASTORE_CREATED);
    }
  }

  @VisibleForTesting public Reactable(Long directorId, Timestamp created) {
    this.directorId = directorId;
    this.created = created;
  }

  // A default constructor is provided for serialization and de-serialization.
  @SuppressWarnings("unused") Reactable() {
  }

  /**
   * @param entity to create a {@link Reactable} from.
   * @return a {@link Reactable} based on {@code entity}. The correct constructor is called based on {@code entity.getProperty(DATASTORE_TYPE}.
   */
  public static Reactable fromEntity(Entity entity) {
    if (entity.getString(DATASTORE_TYPE) == null) {
      throw new IllegalArgumentException("Entity is missing a type property");
    }
    String type = entity.getString(DATASTORE_TYPE);
    Reactable reactable = null;
    if (Objects.equals(type, Scene.class.getSimpleName())) {
      reactable = new Scene(entity);
    }
    return reactable;
  }

  public boolean isViewed() {
    return viewed;
  }

  public void setViewed(boolean viewed) {
    this.viewed = viewed;
  }

  public Map<Emotion, Long> getReactionCounters() {
    return reactionCounters;
  }

  public void setReactionCounters(
      Map<Emotion, Long> reactionCounters) {
    this.reactionCounters = reactionCounters;
  }

  public Emotion getUserReaction() {
    return userReaction;
  }

  public void setUserReaction(Emotion userReaction) {
    this.userReaction = userReaction;
  }

  public User getDirector() {
    return director;
  }

  public void setDirector(User director) {
    directorId = null;
    this.director = director;
  }

  public long getDirectorId() {
    return director == null ? directorId : director.getId();
  }

  @VisibleForTesting public boolean hasDirectorId() {
    return directorId != null;
  }

  public Timestamp getCreated() {
    return created;
  }

  public void setCreated(Timestamp created) {
    this.created = created;
  }

  @Override public FullEntity.Builder<IncompleteKey> toEntityBuilder(KeyFactory keyFactory) {
    FullEntity.Builder<IncompleteKey> builder = super.toEntityBuilder(keyFactory);
    builder.set(DATASTORE_TYPE, this.getClass().getSimpleName());
    if (created != null) {
      builder.set(DATASTORE_CREATED, created);
    }
    if (directorId != null) {
      builder.set(DATASTORE_DIRECTOR_ID, getDirectorId());
    }
    return builder;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (created != null ? created.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Reactable)) return false;
    if (!super.equals(o)) return false;

    Reactable reactable = (Reactable) o;

    if (viewed != reactable.viewed) return false;
    if (directorId != null ? !directorId.equals(reactable.directorId)
        : reactable.directorId != null) {
      return false;
    }
    if (created != null ? !created.equals(reactable.created) : reactable.created != null) {
      return false;
    }
    if (reactionCounters != null ? !reactionCounters.equals(reactable.reactionCounters)
        : reactable.reactionCounters != null) {
      return false;
    }
    if (userReaction != reactable.userReaction) return false;
    return director != null ? director.equals(reactable.director) : reactable.director == null;
  }

  public void save(HttpServletRequest req, StudioServlet servlet) throws Exception {
    saveMedia(req, servlet);
    FullEntity entity = toEntityBuilder(servlet.getReactableKeyFactory()).build();
    Entity savedEntity = servlet.getDatastore().add(entity);
    id = savedEntity.getKey().getId();
  }

  abstract void saveMedia(HttpServletRequest req, StudioServlet servlet) throws Exception;
}
