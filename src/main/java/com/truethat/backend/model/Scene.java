package com.truethat.backend.model;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityValue;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.storage.BlobInfo;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.ByteStreams;
import com.truethat.backend.servlet.BaseServlet;
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
  public static final String KIND = "Scene";
  /**
   * Datastore column names.
   */
  public static final String COLUMN_CREATED = "created";
  public static final String COLUMN_DIRECTOR_ID = "directorId";
  public static final String COLUMN_MEDIA = "media";
  private static final String COLUMN_EDGES = "edge";
  /**
   * The media items of this scene.
   */
  private List<Media> mediaNodes;
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
  /**
   * Maps media client IDs to datastore one.
   */
  private transient BiMap<Long, Long> clientIdToDatastoreId = HashBiMap.create();

  public Scene(FullEntity entity) {
    super(entity);
    if (entity.contains(COLUMN_DIRECTOR_ID)) {
      directorId = entity.getLong(COLUMN_DIRECTOR_ID);
    }
    if (entity.contains(COLUMN_CREATED)) {
      created = entity.getTimestamp(COLUMN_CREATED);
    }
    if (entity.contains(COLUMN_MEDIA)) {
      @SuppressWarnings("unchecked") List<EntityValue> mediaEntities =
          entity.getList(COLUMN_MEDIA);
      mediaNodes = mediaEntities.stream()
          .map(entityValue -> Media.fromEntity(entityValue.get()))
          .collect(toList());
    }
    if (entity.contains(COLUMN_EDGES)) {
      @SuppressWarnings("unchecked") List<EntityValue> edgeEntities =
          entity.getList(COLUMN_EDGES);
      edges =
          edgeEntities.stream().map(entityValue -> new Edge(entityValue.get())).collect(toList());
    }
  }

  @VisibleForTesting public Scene(User director, Timestamp created, List<Media> mediaNodes,
      List<Edge> edges) {
    this.director = director;
    this.created = created;
    this.mediaNodes = mediaNodes;
    this.edges = edges;
  }

  // A default constructor is provided for serialization and de-serialization.
  @SuppressWarnings("unused") Scene() {
  }

  public List<Media> getMediaNodes() {
    return mediaNodes;
  }

  public void setMediaNodes(List<Media> mediaNodes) {
    this.mediaNodes = mediaNodes;
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

  @Override public FullEntity.Builder<IncompleteKey> toEntityBuilder(BaseServlet servlet) {
    FullEntity.Builder<IncompleteKey> builder = super.toEntityBuilder(servlet);
    if (created != null) {
      builder.set(COLUMN_CREATED, created);
    }
    if (getDirectorId() != null) {
      builder.set(COLUMN_DIRECTOR_ID, getDirectorId());
    }
    if (mediaNodes != null && !mediaNodes.isEmpty()) {
      builder.set(COLUMN_MEDIA, mediaNodes.stream()
          .map(media -> {
            FullEntity.Builder mediaEntity = media.toEntityBuilder(servlet);
            Key newKey = servlet.getKeyFactory(Media.KIND).newKey(media.getId());
            //noinspection unchecked
            mediaEntity.setKey(newKey);
            return new EntityValue(mediaEntity.build());
          })
          .collect(toList()));
    }
    if (edges != null && !edges.isEmpty()) {
      edges.forEach(edge -> edge.updateIds(clientIdToDatastoreId));
      builder.set(COLUMN_EDGES, edges.stream()
          .map(edge -> new EntityValue(edge.toEntityBuilder(servlet).build()))
          .collect(toList()));
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

  @Override String getKind() {
    return KIND;
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
    if (mediaNodes != null) {
      for (Media media : mediaNodes) {
        updateMediaId(media, servlet);
        saveMedia(media, req, servlet);
      }
    }
    FullEntity entity = toEntityBuilder(servlet).build();
    Entity savedEntity = servlet.getDatastore().add(entity);
    id = savedEntity.getKey().getId();
  }

  /**
   * Saves {@link #mediaNodes} to storage.
   *  @param media to save.
   * @param req        in which the scene is described.
   * @param servlet    from which the client requested the save.
   */
  private void saveMedia(Media media, HttpServletRequest req, StudioServlet servlet)
      throws Exception {
    Part part = req.getPart(generatePartName(media));
    if (part == null) throw new IOException("Missing " + generatePartName(media) + " part");
    String relativeUrl = getSaveDestination(media, part);
    BlobInfo blobInfo = servlet.getStorageClient().save(relativeUrl,
        part.getContentType(),
        ByteStreams.toByteArray(part.getInputStream()),
        servlet.getBucketName());
    mediaNodes.get(mediaNodes.indexOf(media))
        .setUrl(servlet.getStorageClient().getPublicLink(blobInfo));
  }

  /**
   * @param media to get a save path for
   * @param part       of {@link #mediaNodes}
   *
   * @return sub path within the storage in which to save {@code media} content.
   */
  private String getSaveDestination(Media media, Part part) {
    return Media.STORAGE_SUB_PATH
        + getDirectorId()
        + "_"
        + media.getId()
        + "_"
        + Math.round(
        Math.random() * 1000000000)
        + "."
        + part.getContentType().split("/")[1];
  }

  /**
   * @param media to query for
   *
   * @return expected HTTP multipart name for media content of the {@code mediaId}-th item.
   */
  private String generatePartName(Media media) {
    return Media.MEDIA_PART_PREFIX + clientIdToDatastoreId.inverse().get(media.getId());
  }

  /**
   * Updates {@code media} ID with a new ID allocated by datastore.
   *
   * @param media   to update
   * @param servlet to obtain datastore and key factories.
   */
  private void updateMediaId(Media media, BaseServlet servlet) {
    Key newKey =
        servlet.getDatastore().allocateId(servlet.getKeyFactory(Media.KIND).newKey());
    clientIdToDatastoreId.put(media.getId(), newKey.getId());
    media.setId(newKey.getId());
  }
}
