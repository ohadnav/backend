package com.truethat.backend.model;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.storage.BlobInfo;
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
 * @ios <a>https://github.com/true-that/ios/blob/master/TrueThat/Model/Pose.swift</a>
 */
public class Pose extends Reactable {
  /**
   * Multipart HTTP request part names, as used by {@link com.truethat.backend.servlet.StudioServlet}.
   *
   * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/StudioApi.java</a>
   */
  public static final String IMAGE_PART = "pose_image";
  /**
   * Datastore column names.
   */
  private static final String DATASTORE_IMAGE_URL = "imageUrl";
  /**
   * Sub path for pose images within the storage bucket.
   */
  private static final String STORAGE_IMAGES_PATH = "pose/images/";

  private static final String DEFAULT_IMAGE_TYPE = "jpg";

  /**
   * Authenticated query string for the pose image, which is stored in Google Storage.
   */
  private String imageUrl;

  Pose(Entity entity) {
    super(entity);
    if (entity.contains(DATASTORE_IMAGE_URL)) {
      imageUrl = entity.getString(DATASTORE_IMAGE_URL);
    }
  }

  @VisibleForTesting public Pose(User director, Timestamp created, String imageUrl) {
    super(director, created);
    this.imageUrl = imageUrl;
  }

  @Override public FullEntity.Builder<IncompleteKey> toEntityBuilder(KeyFactory keyFactory) {
    FullEntity.Builder<IncompleteKey> builder = super.toEntityBuilder(keyFactory);
    if (imageUrl != null) {
      builder.set(DATASTORE_IMAGE_URL, imageUrl);
    }
    return builder;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Pose)) return false;
    if (!super.equals(o)) return false;

    Pose pose = (Pose) o;

    return imageUrl != null ? imageUrl.equals(pose.imageUrl)
        : pose.imageUrl == null;
  }

  @Override void saveMedia(HttpServletRequest req, StudioServlet servlet) throws Exception {
    Part imagePart = req.getPart(IMAGE_PART);
    if (imagePart == null) throw new IOException("Missing pose image, are you being shy?");
    String imagePath = getImagePath();
    BlobInfo blobInfo = servlet.getStorageClient().save(imagePath,
        imagePart.getContentType(),
        ByteStreams.toByteArray(imagePart.getInputStream()),
        servlet.getBucketName());
    imageUrl = blobInfo.getMediaLink();
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  /**
   * @return the sub path of the image destination within the storage bucket.
   */
  private String getImagePath() {
    return STORAGE_IMAGES_PATH
        + getDirectorId()
        + "/"
        + Math.round(Math.random() * 1000000000)
        + "."
        + DEFAULT_IMAGE_TYPE;
  }
}
