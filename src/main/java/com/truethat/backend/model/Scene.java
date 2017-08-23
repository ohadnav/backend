package com.truethat.backend.model;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;
import com.truethat.backend.servlet.StudioServlet;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

/**
 * Proudly created by ohad on 08/05/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/Scene.java</a>
 */
public class Scene extends Reactable {
  /**
   * Multipart HTTP request part names, as used by {@link com.truethat.backend.servlet.StudioServlet}.
   *
   * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/StudioAPI.java</a>
   */
  public static final String IMAGE_PART = "scene_image";
  /**
   * Datastore column names.
   */
  private static final String DATASTORE_IMAGE_SIGNED_URL = "imageSignedUrl";
  /**
   * Sub path for scene images within the storage bucket.
   */
  private static final String STORAGE_IMAGES_PATH = "scene/images/";

  private static final String DEFAULT_IMAGE_TYPE = "jpg";

  /**
   * Authenticated query string for the scene image, which is stored in Google Storage.
   */
  private String imageSignedUrl;

  Scene(Entity entity) {
    super(entity);
    if (entity.contains(DATASTORE_IMAGE_SIGNED_URL)) {
      imageSignedUrl = entity.getString(DATASTORE_IMAGE_SIGNED_URL);
    }
  }

  @VisibleForTesting public Scene(Long directorId, Timestamp created, String imageSignedUrl) {
    super(directorId, created);
    this.imageSignedUrl = imageSignedUrl;
  }

  @Override public FullEntity.Builder<IncompleteKey> toEntityBuilder(KeyFactory keyFactory) {
    FullEntity.Builder<IncompleteKey> builder = super.toEntityBuilder(keyFactory);
    if (imageSignedUrl != null) {
      builder.set(DATASTORE_IMAGE_SIGNED_URL, imageSignedUrl);
    }
    return builder;
  }

  @Override void saveMedia(HttpServletRequest req, StudioServlet servlet) throws Exception {
    Part imagePart = req.getPart(IMAGE_PART);
    if (imagePart == null) throw new IOException("Missing scene image, are you being shy?");
    servlet.getStorageClient().save(getImagePath(),
        imagePart.getContentType(),
        ByteStreams.toByteArray(imagePart.getInputStream()),
        servlet.getBucketName());
    imageSignedUrl = servlet.getUrlSigner()
        .sign(servlet.getBucketName() + "/" + getImagePath());
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Scene)) return false;
    if (!super.equals(o)) return false;

    Scene scene = (Scene) o;

    return imageSignedUrl != null ? imageSignedUrl.equals(scene.imageSignedUrl)
        : scene.imageSignedUrl == null;
  }

  public String getImageSignedUrl() {
    return imageSignedUrl;
  }

  public void setImageSignedUrl(String imageSignedUrl) {
    this.imageSignedUrl = imageSignedUrl;
  }

  /**
   * @return the sub path of the image destination within the storage bucket.
   */
  private String getImagePath() {
    return STORAGE_IMAGES_PATH
        + getDirectorId()
        + "/"
        + getCreated().getSeconds()
        + "."
        + DEFAULT_IMAGE_TYPE;
  }
}
