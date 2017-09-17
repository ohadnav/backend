package com.truethat.backend.model;

import com.google.cloud.datastore.FullEntity;
import com.google.common.annotations.VisibleForTesting;

/**
 * Proudly created by ohad on 08/09/2017.
 */
public class Photo extends Media {
  @VisibleForTesting public Photo(Long id, String url) {
    super(id, url);
  }

  public Photo(FullEntity entity) {
    super(entity);
  }
}
