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
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/Pose.java</a>
 */
public class Pose extends Reactable {
  /**
   * Multipart HTTP request part names, as used by {@link com.truethat.backend.servlet.StudioServlet}.
   *
   * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/StudioAPI.java</a>
   */
  public static final String IMAGE_PART = "pose_image";
  /**
   * Datastore column names.
   */
  private static final String DATASTORE_IMAGE_SIGNED_URL = "imageSignedUrl";
  /**
   * Sub path for pose images within the storage bucket.
   */
  private static final String STORAGE_IMAGES_PATH = "pose/images/";

  private static final String DEFAULT_IMAGE_TYPE = "jpg";

  /**
   * Authenticated query string for the pose image, which is stored in Google Storage.
   */
  private String imageSignedUrl;

  Pose(Entity entity) {
    super(entity);
    if (entity.contains(DATASTORE_IMAGE_SIGNED_URL)) {
      imageSignedUrl = entity.getString(DATASTORE_IMAGE_SIGNED_URL);
    }
  }

  @VisibleForTesting public Pose(User director, Timestamp created, String imageSignedUrl) {
    super(director, created);
    this.imageSignedUrl = imageSignedUrl;
  }

  @Override public FullEntity.Builder<IncompleteKey> toEntityBuilder(KeyFactory keyFactory) {
    FullEntity.Builder<IncompleteKey> builder = super.toEntityBuilder(keyFactory);
    if (imageSignedUrl != null) {
      builder.set(DATASTORE_IMAGE_SIGNED_URL, imageSignedUrl);
    }
    return builder;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Pose)) return false;
    if (!super.equals(o)) return false;

    Pose pose = (Pose) o;

    return imageSignedUrl != null ? imageSignedUrl.equals(pose.imageSignedUrl)
        : pose.imageSignedUrl == null;
  }

  @Override void saveMedia(HttpServletRequest req, StudioServlet servlet) throws Exception {
    Part imagePart = req.getPart(IMAGE_PART);
    if (imagePart == null) throw new IOException("Missing pose image, are you being shy?");
    servlet.getStorageClient().save(getImagePath(),
        imagePart.getContentType(),
        ByteStreams.toByteArray(imagePart.getInputStream()),
        servlet.getBucketName());
    imageSignedUrl = servlet.getUrlSigner()
        .sign(servlet.getBucketName() + "/" + getImagePath());
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
