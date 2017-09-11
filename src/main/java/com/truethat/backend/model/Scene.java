package com.truethat.backend.model;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityValue;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.storage.BlobInfo;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;
import com.truethat.backend.servlet.StudioServlet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import static java.util.stream.Collectors.toList;

/**
 * Proudly created by ohad on 27/06/2017.
 * <p>
 * A media item that the user can be reacted to, such as a crazy video or a sassy photo.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/Scene.java</a>
 * @ios <a>https://github.com/true-that/ios/blob/master/TrueThat/Model/Scene.swift</a>
 */
public class Scene extends BaseModel {
  /**
   * Multipart HTTP request part names, as used by backend endpoints such as {@link
   * com.truethat.backend.servlet.StudioServlet}.
   */
  public static final String SCENE_PART = "scene";
  /**
   * Datastore kind.
   */
  public static final String DATASTORE_KIND = "Scene";
  /**
   * Datastore column names.
   */
  public static final String DATASTORE_CREATED = "created";
  public static final String DATASTORE_DIRECTOR_ID = "directorId";
  private static final String DATASTORE_MEDIA = "media";
  private static final String DATASTORE_EDGES = "edge";
  /**
   * The media items of this scene.
   */
  private List<Media> mediaItems;
  /**
   * The interaction flow of users with this scene.
   */
  private List<Edge> edges;
  /**
   * ID of the Scene director (i.e. its creator).
   */
  private Long directorId;
  /**
   * Client created UTC timestamp
   */
  private Timestamp created;
  /**
   * Whether the scene was viewed by the user.
   */
  private boolean viewed;
  /**
   * Counters of emotional reactions to the scene, per each emotion.
   */
  private Map<Emotion, Long> reactionCounters;
  /**
   * The user reaction to the scene, {@code null} for no reaction.
   */
  private Emotion userReaction;
  /**
   * Scene director (i.e. its creator). This field is the one returned to client endpoints, and not
   * {@link #directorId}.
   */
  private User director;

  public Scene(FullEntity entity) {
    super(entity);
    if (entity.contains(DATASTORE_DIRECTOR_ID)) {
      directorId = entity.getLong(DATASTORE_DIRECTOR_ID);
    }
    if (entity.contains(DATASTORE_CREATED)) {
      created = entity.getTimestamp(DATASTORE_CREATED);
    }
    if (entity.contains(DATASTORE_MEDIA)) {
      @SuppressWarnings("unchecked") List<EntityValue> mediaEntities =
          entity.getList(DATASTORE_MEDIA);
      mediaItems = mediaEntities.stream()
          .map(entityValue -> Media.fromEntity(entityValue.get()))
          .collect(toList());
    }
    if (entity.contains(DATASTORE_EDGES)) {
      @SuppressWarnings("unchecked") List<EntityValue> edgeEntities =
          entity.getList(DATASTORE_EDGES);
      edges =
          edgeEntities.stream().map(entityValue -> new Edge(entityValue.get())).collect(toList());
    }
  }

  @VisibleForTesting public Scene(User director, Timestamp created, List<Media> mediaItems,
      List<Edge> edges) {
    this.director = director;
    this.created = created;
    this.mediaItems = mediaItems;
    this.edges = edges;
  }

  // A default constructor is provided for serialization and de-serialization.
  @SuppressWarnings("unused") Scene() {
  }

  public List<Media> getMediaItems() {
    return mediaItems;
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

  public Long getDirectorId() {
    return director == null ? directorId : director.getId();
  }

  public void setDirectorId(Long directorId) {
    this.directorId = directorId;
  }

  public Timestamp getCreated() {
    return created;
  }

  public void setCreated(Timestamp created) {
    this.created = created;
  }

  public void setMediaItems(List<Media> mediaItems) {
    this.mediaItems = mediaItems;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (created != null ? created.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Scene)) return false;
    if (!super.equals(o)) return false;

    Scene scene = (Scene) o;

    if (viewed != scene.viewed) return false;
    if (directorId != null ? !directorId.equals(scene.directorId)
        : scene.directorId != null) {
      return false;
    }
    if (created != null ? !created.equals(scene.created) : scene.created != null) {
      return false;
    }
    if (reactionCounters != null ? !reactionCounters.equals(scene.reactionCounters)
        : scene.reactionCounters != null) {
      return false;
    }
    if (userReaction != scene.userReaction) return false;
    return director != null ? director.equals(scene.director) : scene.director == null;
  }

  @Override public FullEntity.Builder<IncompleteKey> toEntityBuilder(KeyFactory keyFactory) {
    FullEntity.Builder<IncompleteKey> builder = super.toEntityBuilder(keyFactory);
    if (created != null) {
      builder.set(DATASTORE_CREATED, created);
    }
    if (getDirectorId() != null) {
      builder.set(DATASTORE_DIRECTOR_ID, getDirectorId());
    }
    if (mediaItems != null && !mediaItems.isEmpty()) {
      builder.set(DATASTORE_MEDIA, mediaItems.stream()
          .map(media -> new EntityValue(media.toEntityBuilder(keyFactory).build()))
          .collect(toList()));
    }
    if (edges != null && !edges.isEmpty()) {
      builder.set(DATASTORE_EDGES, edges.stream()
          .map(edge -> new EntityValue(edge.toEntityBuilder(keyFactory).build()))
          .collect(toList()));
    }
    return builder;
  }

  public List<Edge> getEdges() {
    return edges;
  }

  public void setEdges(List<Edge> edges) {
    this.edges = edges;
  }

  /**

   * Saves this scene to datastore and storage.
   *
   * @param req     in which the scene is described.
   * @param servlet from which the client requested the save.
   */
  public void save(HttpServletRequest req, StudioServlet servlet) throws Exception {
    if (mediaItems != null) {
      for (int i = 0; i < mediaItems.size(); i++) {
        saveMedia(i, req, servlet);
      }
    }
    FullEntity entity = toEntityBuilder(servlet.getSceneKeyFactory()).build();
    Entity savedEntity = servlet.getDatastore().add(entity);
    id = savedEntity.getKey().getId();
  }

  /**
   * Saves {@link #mediaItems} to storage.
   *  @param mediaIndex   to save.
   * @param req     in which the scene is described.
   * @param servlet from which the client requested the save.
   */
  private void saveMedia(int mediaIndex, HttpServletRequest req,
      StudioServlet servlet)
      throws Exception {
    Part part = req.getPart(generatePartName(mediaIndex));
    if (part == null) throw new IOException("Missing " + generatePartName(mediaIndex) + " part");
    String relativeUrl = getSaveDestination(mediaIndex, part);
    BlobInfo blobInfo = servlet.getStorageClient().save(relativeUrl,
        part.getContentType(),
        ByteStreams.toByteArray(part.getInputStream()),
        servlet.getBucketName());
    mediaItems.get(mediaIndex).setUrl(servlet.getStorageClient().getPublicLink(blobInfo));
  }

  /**
   * @param mediaIndex to get a save path for
   * @param part  of {@link #mediaItems}
   *
   * @return sub path within the storage in which to save {@code media} content.
   */
  private String getSaveDestination(int mediaIndex, Part part) {
    return Media.STORAGE_SUB_PATH
        + getDirectorId()
        + "/"
        + mediaIndex
        + "-"
        + created.getSeconds()
        + "-"
        + Math.round(
        Math.random() * 1000000000)
        + "."
        + part.getContentType().split("/")[1];
  }

  /**
   * @param mediaIndex to query for
   *
   * @return expected HTTP multipart name for media content of the {@code mediaIndex}-th item.
   */
  private String generatePartName(int mediaIndex) {
    return Media.MEDIA_PART_PREFIX + "_" + mediaIndex;
  }
}
