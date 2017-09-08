package com.truethat.backend.model;

import com.google.cloud.datastore.FullEntity;

/**
 * Proudly created by ohad on 08/09/2017.
 */
public class Photo extends Media {
  public Photo(String url) {
    super(url);
  }

  public Photo(FullEntity entity) {
    super(entity);
  }
}
