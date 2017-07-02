package com.truethat.backend.model;

import com.google.appengine.api.datastore.Entity;
import com.google.common.annotations.VisibleForTesting;
import com.truethat.backend.servlet.StudioServlet;
import java.io.IOException;
import java.util.Date;
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
  public static final String DATASTORE_IMAGE_SIGNED_URL = "imageSignedUrl";
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
    imageSignedUrl = (String) entity.getProperty(DATASTORE_IMAGE_SIGNED_URL);
  }

  @VisibleForTesting public Scene(Long directorId, Date created, String imageSignedUrl) {
    super(directorId, created);
    this.imageSignedUrl = imageSignedUrl;
  }

  @Override public Entity toEntity() {
    Entity entity = super.toEntity();
    entity.setProperty(DATASTORE_IMAGE_SIGNED_URL, imageSignedUrl);
    return entity;
  }

  @Override public void prepareSave(HttpServletRequest req,
      StudioServlet servlet) throws Exception {
    Part imagePart = req.getPart(IMAGE_PART);
    if (imagePart == null) throw new IOException("Missing scene image, are you being shy?");
    servlet.getStorageClient().save(getImagePath(),
        imagePart.getContentType(),
        imagePart.getInputStream(),
        servlet.getBucketName());
    imageSignedUrl = servlet.getUrlSigner()
        .sign(servlet.getPrivateKey(), servlet.getBucketName() + "/" + getImagePath());
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
        + getCreated().getTime()
        + "."
        + DEFAULT_IMAGE_TYPE;
  }
}
