package com.truethat.backend.model;

import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.truethat.backend.servlet.BaseServlet;
import java.util.Map;

/**
 * Proudly created by ohad on 11/09/2017.
 * <p>
 * Describes relations between media nodes and the flow in which user will interact with them.
 * {@code <0, 1, HAPPY>} means users that had a {@code HAPPY} reaction to the 0-indexed media node
 * will than view 1-indexed node.
 * <p>
 * Note that we regard the {@link Media} node order in {@link Scene#mediaNodes} as its index.
 */
public class Edge extends BaseModel {
  /**
   * Datastore kind.
   */
  private static final String KIND = "Edge";
  /**
   * Datastore column names.
   */
  private static final String COLUMN_SOURCE = "source";
  private static final String COLUMN_TARGET = "target";
  private static final String COLUMN_REACTION = "reaction";

  /**
   * Index of media source node in {@link Scene#mediaNodes}, i.e. {@code photo1} in the above
   * example.
   */
  private Long sourceId;

  /**
   * Index of media target node in {@link Scene#mediaNodes}, i.e. {@code video1} in the above
   * example.
   */
  private Long targetId;

  /**
   * Which reaction should trigger the follow up this edge describes.
   */
  private Emotion reaction;

  public Edge(Long sourceId, Long targetId, Emotion reaction) {
    this.sourceId = sourceId;
    this.targetId = targetId;
    this.reaction = reaction;
  }

  Edge(FullEntity entity) {
    super(entity);
    if (entity.contains(COLUMN_SOURCE)) {
      sourceId = entity.getLong(COLUMN_SOURCE);
    }
    if (entity.contains(COLUMN_TARGET)) {
      targetId = entity.getLong(COLUMN_TARGET);
    }
    if (entity.contains(COLUMN_REACTION)) {
      reaction = Emotion.fromCode((int) entity.getLong(COLUMN_REACTION));
    }
  }

  public Long getSourceId() {
    return sourceId;
  }

  public Long getTargetId() {
    return targetId;
  }

  public Emotion getReaction() {
    return reaction;
  }

  @Override public FullEntity.Builder<IncompleteKey> toEntityBuilder(BaseServlet servlet) {
    FullEntity.Builder<IncompleteKey> builder = super.toEntityBuilder(servlet);
    if (sourceId != null) {
      builder.set(COLUMN_SOURCE, sourceId);
    }
    if (targetId != null) {
      builder.set(COLUMN_TARGET, targetId);
    }
    if (reaction != null) {
      builder.set(COLUMN_REACTION, reaction.getCode());
    }
    return builder;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (sourceId != null ? sourceId.hashCode() : 0);
    result = 31 * result + (targetId != null ? targetId.hashCode() : 0);
    result = 31 * result + (reaction != null ? reaction.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Edge)) return false;
    if (!super.equals(o)) return false;

    Edge edge = (Edge) o;

    if (sourceId != null ? !sourceId.equals(edge.sourceId) : edge.sourceId != null) {
      return false;
    }
    if (targetId != null ? !targetId.equals(edge.targetId) : edge.targetId != null) {
      return false;
    }
    return reaction == edge.reaction;
  }

  @Override String getKind() {
    return KIND;
  }

  /**
   * Update deserialized IDs that were given by the client, with new IDs that are provided by
   * datastore.
   *
   * @param oldIdToNewId maps old(client) IDs to new IDs (allocated by datastore).
   */
  void updateIds(Map<Long, Long> oldIdToNewId) {
    if (oldIdToNewId.containsKey(sourceId)) {
      sourceId = oldIdToNewId.get(sourceId);
    }
    if (oldIdToNewId.containsKey(targetId)) {
      targetId = oldIdToNewId.get(targetId);
    }
  }
}
