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
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/Short.java</a>
 * @ios <a>https://github.com/true-that/ios/blob/master/TrueThat/Model/Short.swift</a>
 */
public class Short extends Reactable {
  /**
   * Multipart HTTP request part names, as used by {@link com.truethat.backend.servlet.StudioServlet}.
   *
   * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/StudioApi.java</a>
   */
  public static final String VIDEO_PART = "short_video";
  /**
   * Datastore column names.
   */
  private static final String DATASTORE_VIDEO_URL = "videoUrl";
  /**
   * Sub path for short videos within the storage bucket.
   */
  private static final String STORAGE_VIDEOS_PATH = "short/videos/";

  /**
   * Authenticated query string for the short video, which is stored in Google Storage.
   */
  private String videoUrl;

  Short(Entity entity) {
    super(entity);
    if (entity.contains(DATASTORE_VIDEO_URL)) {
      videoUrl = entity.getString(DATASTORE_VIDEO_URL);
    }
  }

  @VisibleForTesting public Short(User director, Timestamp created, String videoUrl) {
    super(director, created);
    this.videoUrl = videoUrl;
  }

  @Override public FullEntity.Builder<IncompleteKey> toEntityBuilder(KeyFactory keyFactory) {
    FullEntity.Builder<IncompleteKey> builder = super.toEntityBuilder(keyFactory);
    if (videoUrl != null) {
      builder.set(DATASTORE_VIDEO_URL, videoUrl);
    }
    return builder;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Short)) return false;
    if (!super.equals(o)) return false;

    Short s = (Short) o;

    return videoUrl != null ? videoUrl.equals(s.videoUrl) : s.videoUrl == null;
  }

  @Override void saveMedia(HttpServletRequest req, StudioServlet servlet) throws Exception {
    Part videoPart = req.getPart(VIDEO_PART);
    if (videoPart == null) throw new IOException("Missing short video, are you being shy?");
    String videoPath = getVideoPath(videoPart.getContentType());
    BlobInfo blobInfo = servlet.getStorageClient().save(videoPath,
        videoPart.getContentType(),
        ByteStreams.toByteArray(videoPart.getInputStream()),
        servlet.getBucketName());
    videoUrl = blobInfo.getMediaLink();
  }

  public String getVideoUrl() {
    return videoUrl;
  }

  public void setVideoUrl(String videoUrl) {
    this.videoUrl = videoUrl;
  }

  /**
   * @return the sub path of the video destination within the storage bucket.
   */
  private String getVideoPath(String contentType) {
    String fileType = "mp4";
    if (contentType.contains("/")) fileType = contentType.split("/")[1];
    return STORAGE_VIDEOS_PATH
        + getDirectorId()
        + "/"
        + getCreated().getSeconds()
        + "."
        + fileType;
  }
}
