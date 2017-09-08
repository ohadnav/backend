package com.truethat.backend.model;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;
import com.truethat.backend.common.Util;

/**
 * Proudly created by ohad on 23/08/2017.
 */
public abstract class BaseModel {
  /**
   * As saved in the datastore.
   */
  Long id;

  BaseModel() {
  }

  BaseModel(FullEntity entity) {
    if (entity instanceof Entity && entity.getKey() != null) {
      id = ((Entity) entity).getKey().getId();
    }
  }

  public FullEntity.Builder<IncompleteKey> toEntityBuilder(KeyFactory keyFactory) {
    FullEntity.Builder<IncompleteKey> builder = Entity.newBuilder();
    builder.setKey(keyFactory.newKey());
    return builder;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BaseModel)) return false;

    BaseModel baseModel = (BaseModel) o;

    return id != null ? id.equals(baseModel.id) : baseModel.id == null;
  }

  @Override
  public String toString() {
    return Util.GSON.toJson(this);
  }
}
