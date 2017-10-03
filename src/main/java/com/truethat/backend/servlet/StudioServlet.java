package com.truethat.backend.servlet;

import com.google.common.annotations.VisibleForTesting;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Edge;
import com.truethat.backend.model.Media;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import com.truethat.backend.storage.DefaultStorageClient;
import com.truethat.backend.storage.StorageClient;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;
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

  void setStorageClient(StorageClient storageClient) {
    this.storageClient = storageClient;
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
   * @param req multipart request that contains {@link Scene} metadata and {@link Media} files
   *            parts.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPost(req, resp);
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
    if (datastore.get(getKeyFactory(User.KIND).newKey(scene.getDirector().getId())) == null) {
      errorBuilder.append("director(i.e. a user) with ID ")
          .append(scene.getDirectorId())
          .append(" not found.");
      return false;
    }
    if (scene.getCreated() == null) {
      errorBuilder.append("missing created timestamp");
      return false;
    }
    // Validate media nodes
    Set<Long> mediaIds = new HashSet<>();
    for (Media media : scene.getMediaNodes()) {
      if (media.getId() == null) {
        errorBuilder.append("a media item is missing an ID.");
        return false;
      }
      if (mediaIds.contains(media.getId())) {
        errorBuilder.append("duplicate media IDs.");
        return false;
      }
      mediaIds.add(media.getId());
    }
    // Validate edges
    if (scene.getMediaNodes().size() <= 1 && (scene.getEdges() != null && !scene.getEdges()
        .isEmpty())) {
      errorBuilder.append("edges should be empty or null when no multiple media items exists.");
      return false;
    }
    // If there aren't multiple media items there is no need to validate edges.
    if (scene.getMediaNodes().size() <= 1) {
      return true;
    }
    if (scene.getMediaNodes().size() > 1 && scene.getEdges() == null) {
      errorBuilder.append("edges cannot be null when multiple media items exists.");
      return false;
    }
    if (scene.getMediaNodes().size() > 1 && scene.getEdges().isEmpty()) {
      errorBuilder.append("edges cannot be empty when multiple media items exists.");
      return false;
    }
    for (Edge edge : scene.getEdges()) {
      if (edge.getSourceId() == null) {
        errorBuilder.append("edge (")
            .append(edge)
            .append(") is missing a source ID.");
        return false;
      }
      if (edge.getTargetId() == null) {
        errorBuilder.append("edge (")
            .append(edge)
            .append(") is missing a target ID.");
        return false;
      }
      if (edge.getReaction() == null) {
        errorBuilder.append("edge (")
            .append(edge)
            .append(") is missing a reaction.");
        return false;
      }
      if (!mediaIds.contains(edge.getSourceId())) {
        errorBuilder.append("edge (")
            .append(edge)
            .append(") source ID has no matching media node.");
        return false;
      }
      if (!mediaIds.contains(edge.getTargetId())) {
        errorBuilder.append("edge (")
            .append(edge)
            .append(") target ID has no matching media node.");
        return false;
      }
    }
    // Ensures the edges represent a tree.
    boolean[] isReachable = new boolean[scene.getMediaNodes().size()];
    for (Edge edge : scene.getEdges()) {
      isReachable[edge.getTargetId().intValue()] = true;
    }
    int countRoots = 0;
    for (boolean b : isReachable) {
      if (!b) countRoots++;
    }
    if (countRoots > 1) {
      errorBuilder.append("flow tree has not than one root.");
      return false;
    }
    return true;
  }
}
