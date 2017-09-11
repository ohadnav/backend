package com.truethat.backend.model;

import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;

/**
 * Proudly created by ohad on 11/09/2017.
 * <p>
 * Describes relations between media items and the flow in which user will interact with them.
 * {@code <0, 1, HAPPY>} means users that had a {@code HAPPY} reaction to the 0-indexed media item
 * will than view 1-indexed item.
 * <p>
 * Note that we regard the {@link Media} item order in {@link Scene#mediaItems} as its index.
 */
public class Edge extends BaseModel {
  /**
   * Datastore column names.
   */
  private static final String DATASTORE_SOURCE = "source";
  private static final String DATASTORE_TARGET = "target";
  private static final String DATASTORE_REACTION = "reaction";

  /**
   * Index of media source item in {@link Scene#mediaItems}, i.e. {@code photo1} in the above
   * example.
   */
  private Long sourceIndex;

  /**
   * Index of media target item in {@link Scene#mediaItems}, i.e. {@code video1} in the above
   * example.
   */
  private Long targetIndex;

  /**
   * Which reaction should trigger the follow up this edge describes.
   */
  private Emotion reaction;

  public Edge(Long sourceIndex, Long targetIndex, Emotion reaction) {
    this.sourceIndex = sourceIndex;
    this.targetIndex = targetIndex;
    this.reaction = reaction;
  }

  Edge(FullEntity entity) {
    super(entity);
    if (entity.contains(DATASTORE_SOURCE)) {
      sourceIndex = entity.getLong(DATASTORE_SOURCE);
    }
    if (entity.contains(DATASTORE_TARGET)) {
      targetIndex = entity.getLong(DATASTORE_TARGET);
    }
    if (entity.contains(DATASTORE_REACTION)) {
      reaction = Emotion.fromCode((int) entity.getLong(DATASTORE_REACTION));
    }
  }

  public Long getSourceIndex() {
    return sourceIndex;
  }

  public Long getTargetIndex() {
    return targetIndex;
  }

  public Emotion getReaction() {
    return reaction;
  }

  @Override public FullEntity.Builder<IncompleteKey> toEntityBuilder(KeyFactory keyFactory) {
    FullEntity.Builder<IncompleteKey> builder = super.toEntityBuilder(keyFactory);
    if (sourceIndex != null) {
      builder.set(DATASTORE_SOURCE, sourceIndex);
    }
    if (targetIndex != null) {
      builder.set(DATASTORE_TARGET, targetIndex);
    }
    if (reaction != null) {
      builder.set(DATASTORE_REACTION, reaction.getCode());
    }
    return builder;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (sourceIndex != null ? sourceIndex.hashCode() : 0);
    result = 31 * result + (targetIndex != null ? targetIndex.hashCode() : 0);
    result = 31 * result + (reaction != null ? reaction.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Edge)) return false;
    if (!super.equals(o)) return false;

    Edge edge = (Edge) o;

    if (sourceIndex != null ? !sourceIndex.equals(edge.sourceIndex) : edge.sourceIndex != null) {
      return false;
    }
    if (targetIndex != null ? !targetIndex.equals(edge.targetIndex) : edge.targetIndex != null) {
      return false;
    }
    return reaction == edge.reaction;
  }
}
