package com.truethat.backend.model;

import com.google.cloud.datastore.FullEntity;
import com.google.common.annotations.VisibleForTesting;

/**
 * Proudly created by ohad on 08/09/2017.
 */
public class Video extends Media {
  @VisibleForTesting public Video(Long id, String url) {
    super(id, url);
  }

  Video(FullEntity entity) {
    super(entity);
  }
}
