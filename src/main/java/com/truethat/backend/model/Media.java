package com.truethat.backend.model;

import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;
import com.truethat.backend.common.Util;
import java.util.Objects;

/**
 * Proudly created by ohad on 08/09/2017.
 * <p>
 * A media item, such as a photo or a video.
 * <p>
 * Each implementation should register at {@link Util#GSON}.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/Media.java</a>
 * @ios <a>https://github.com/true-that/ios/blob/master/TrueThat/Model/Media.swift</a>
 */
public class Media extends BaseModel {
  /**
   * Multipart HTTP request part name prefix, as used by backend endpoints such as {@link
   * com.truethat.backend.servlet.StudioServlet}.
   * <p>
   * As {@link Scene} can have more than one {@link Media} item their names will have this prefix
   * followed by "_" and {@link #id}.
   */
  public static final String MEDIA_PART_PREFIX = "media_";
  /**
   * Sub path for media items within the storage bucket.
   */
  static final String STORAGE_SUB_PATH = "media/";
  /**
   * Datastore column names.
   */
  private static final String DATASTORE_URL = "url";
  private static final String DATASTORE_TYPE = "type";
  /**
   * URL of media content as stored on Google storage.
   */
  private String url;

  Media(String url) {
    this.url = url;
  }

  Media(FullEntity entity) {
    super(entity);
    if (entity.contains(DATASTORE_URL)) {
      url = entity.getString(DATASTORE_URL);
    }
  }

  /**
   * @param entity from which to create a media.
   *
   * @return a subtype of media that is built based on {@code entity}.
   */
  static Media fromEntity(FullEntity entity) {
    if (entity.getString(DATASTORE_TYPE) == null) {
      throw new IllegalArgumentException("Entity is missing a type property");
    }
    String type = entity.getString(DATASTORE_TYPE);
    Media media = null;
    if (Objects.equals(type, Photo.class.getSimpleName())) {
      media = new Photo(entity);
    }
    if (Objects.equals(type, Video.class.getSimpleName())) {
      media = new Video(entity);
    }
    return media;
  }

  @Override public FullEntity.Builder<IncompleteKey> toEntityBuilder(KeyFactory keyFactory) {
    FullEntity.Builder<IncompleteKey> builder = super.toEntityBuilder(keyFactory);
    builder.set(DATASTORE_TYPE, this.getClass().getSimpleName());
    if (url != null) {
      builder.set(DATASTORE_URL, url);
    }
    return builder;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (url != null ? url.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Media)) return false;
    if (!super.equals(o)) return false;

    Media media = (Media) o;

    return url != null ? url.equals(media.url) : media.url == null;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
