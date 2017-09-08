package com.truethat.backend.model;

import com.google.cloud.datastore.FullEntity;

/**
 * Proudly created by ohad on 08/09/2017.
 */
public class Video extends Media {
  public Video(String url) {
    super(url);
  }

  Video(FullEntity entity) {
    super(entity);
  }
}
