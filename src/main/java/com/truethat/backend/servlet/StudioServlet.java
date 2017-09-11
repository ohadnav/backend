package com.truethat.backend.servlet;

import com.google.common.annotations.VisibleForTesting;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Edge;
import com.truethat.backend.model.Media;
import com.truethat.backend.model.Scene;
import com.truethat.backend.storage.DefaultStorageClient;
import com.truethat.backend.storage.StorageClient;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Proudly created by ohad on 07/05/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/StudioApi.java</a>
 * @ios <a>https://github.com/true-that/ios/blob/master/TrueThat/Model/Network/StudioApi.swift</a>
 */

@WebServlet(value = "/studio", name = "Studio")
@MultipartConfig
public class StudioServlet extends BaseServlet {
  private StorageClient storageClient;
  private String bucketName = System.getenv("STUDIO_BUCKET");

  public String getBucketName() {
    return bucketName;
  }

  @VisibleForTesting
  void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public StorageClient getStorageClient() {
    return storageClient;
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    // Initializes storage client
    try {
      storageClient = new DefaultStorageClient();
    } catch (IOException | GeneralSecurityException e) {
      e.printStackTrace();
      throw new ServletException("Could not initialize storage client: " + e.getMessage());
    }
  }

  /**
   * Saves the {@link Scene} within the request to storage and datastore, and response the saved
   * {@link Scene} The request is expected to be multipart HTTP request with multiple parts of the
   * {@link Scene} and its {@link Media} items.
   *
   * @param req multipart request that contains {@link Scene} metadata and {@link Media} files parts.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      Part scenePart = req.getPart(Scene.SCENE_PART);
      if (scenePart == null) throw new IOException("Missing scene, how dare you?");
      Scene scene =
          Util.GSON.fromJson(new InputStreamReader(scenePart.getInputStream()),
              Scene.class);
      StringBuilder errorBuilder = new StringBuilder();
      if (!isValidScene(scene, errorBuilder)) {
        throw new IOException(
            "Scene is invalid: " + errorBuilder + ", input: " + scene);
      }
      scene.save(req, this);
      resp.getWriter().print(Util.GSON.toJson(scene));
    } catch (Exception e) {
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      if (e instanceof IOException) {
        throw (IOException) e;
      } else {
        throw new ServletException(e);
      }
    }
  }

  void setStorageClient(StorageClient storageClient) {
    this.storageClient = storageClient;
  }

  /**
   * @return whether the scene has a valid data, and can be saved.
   */
  @SuppressWarnings("RedundantIfStatement") private boolean isValidScene(Scene scene,
      StringBuilder errorBuilder) {
    // Make sure ths director exists
    if (scene.getDirector() == null) {
      errorBuilder.append("missing director.");
      return false;
    }
    if (scene.getDirector().getId() == null) {
      errorBuilder.append("missing director ID.");
      return false;
    }
    if (datastore.get(userKeyFactory.newKey(scene.getDirector().getId())) == null) {
      errorBuilder.append("director(i.e. a user) with ID ")
          .append(scene.getDirectorId())
          .append(" not found.");
      return false;
    }
    if (scene.getCreated() == null) {
      errorBuilder.append("missing created timestamp");
      return false;
    }
    // Validate edges items
    if (scene.getMediaItems().size() <= 1 && (scene.getEdges() != null && !scene.getEdges()
        .isEmpty())) {
      errorBuilder.append("edges should be empty or null when no multiple media items exists.");
      return false;
    }
    // If there aren't multiple media items there is no need to validate edges.
    if (scene.getMediaItems().size() <= 1) {
      return true;
    }
    if (scene.getMediaItems().size() > 1 && scene.getEdges() == null) {
      errorBuilder.append("edges cannot be null when multiple media items exists.");
      return false;
    }
    if (scene.getMediaItems().size() > 1 && scene.getEdges().isEmpty()) {
      errorBuilder.append("edges cannot be empty when multiple media items exists.");
      return false;
    }
    for (Edge edge : scene.getEdges()) {
      if (edge.getSourceIndex() == null) {
        errorBuilder.append("edge (")
            .append(edge)
            .append(") is missing a source index.");
        return false;
      }
      if (edge.getTargetIndex() == null) {
        errorBuilder.append("edge (")
            .append(edge)
            .append(") is missing a target index.");
        return false;
      }
      if (edge.getReaction() == null) {
        errorBuilder.append("edge (")
            .append(edge)
            .append(") is missing a reaction.");
        return false;
      }
      if (edge.getSourceIndex() >= edge.getTargetIndex()) {
        errorBuilder.append("edge (")
            .append(edge)
            .append(") source index must be smaller than target index.");
        return false;
      }
      if (edge.getSourceIndex() < 0) {
        errorBuilder.append("edge (")
            .append(edge)
            .append(") source index mustn't be negative");
        return false;
      }
      if (edge.getTargetIndex() >= scene.getMediaItems().size()) {
        errorBuilder.append("edge (")
            .append(edge)
            .append(") target index must be smaller than number of media items.");
        return false;
      }
    }
    boolean foundRootNode = false;
    for (Edge edge : scene.getEdges()) {
      if (edge.getSourceIndex() == 0) {
        foundRootNode = true;
        break;
      }
    }
    if (!foundRootNode) {
      errorBuilder.append("missing root media note, 0-th media is expected to be that node.");
      return false;
    }
    boolean[] isReachable = new boolean[scene.getMediaItems().size()];
    isReachable[0] = true;
    for (Edge edge : scene.getEdges()) {
      isReachable[edge.getTargetIndex().intValue()] = true;
    }
    for (int i = 0; i < isReachable.length; i++) {
      if (!isReachable[i]) {
        errorBuilder.append(i).append("-th media item is unreachable.");
        return false;
      }
    }
    return true;
  }
}
