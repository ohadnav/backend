package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import com.truethat.backend.storage.StorageUtil;
import com.truethat.backend.storage.UrlSigner;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Proudly created by ohad on 07/05/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/StudioAPI.java</a>
 */

@WebServlet(value = "/studio", name = "Studio")
@MultipartConfig
public class StudioServlet extends HttpServlet {
  @VisibleForTesting static final String USER_PARAM = "user";
  @VisibleForTesting
  static final String CREDENTIALS_PATH = "credentials/";
  @VisibleForTesting
  static final int SCENES_LIMIT = 10;
  private static final Logger LOG = Logger.getLogger(StudioServlet.class.getName());
  private static final DatastoreService DATASTORE_SERVICE =
      DatastoreServiceFactory.getDatastoreService();
  private static String bucketName = System.getenv("STUDIO_BUCKET");
  private String privateKey;

  @VisibleForTesting
  static void setBucketName(String bucketName) {
    StudioServlet.bucketName = bucketName;
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    // Reads credentials file.
    InputStream credentialsStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(CREDENTIALS_PATH + System.getenv("GOOGLE_CLOUD_PROJECT") + ".json");
    try {
      String credentialsString = Util.inputStreamToString(credentialsStream);
      JsonObject credentials =
          new GsonBuilder().create()
              .fromJson(credentialsString, JsonElement.class)
              .getAsJsonObject();
      privateKey = credentials.get("private_key").getAsString();
    } catch (IOException e) {
      LOG.severe("Could not get private key: " + e.getMessage());
      e.printStackTrace();
      throw new ServletException("Could not get private key: " + e.getMessage());
    }
  }

  /**
   * Saves the scene within the request to storage and datastore. The request is expected to be
   * multipart HTTP request with three parts: 1) image 2) director ID as string 3) created timestamp
   * as string. (i.e. '1234567890') <p> Part names are found in {@link Scene}.
   *
   * @param req multipart request with the scene image and director ID.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      Scene saved = saveScene(req);
      resp.getWriter().print(Util.GSON.toJson(saved));
    } catch (Exception e) {
      LOG.severe("Oh oh... " + e.getMessage());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  /**
   * Getting the user's repertoire, i.e. the {@link Scene}s he had created.
   *
   * @param req with the user ID
   */
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User user = Util.GSON.fromJson(req.getParameter(USER_PARAM), User.class);
    Query query = new Query(Scene.DATASTORE_KIND).setFilter(
        new Query.FilterPredicate(Scene.DATASTORE_DIRECTOR_ID, Query.FilterOperator.EQUAL,
            user.getId())).addSort(Scene.DATASTORE_CREATED,
        Query.SortDirection.DESCENDING);
    List<Entity> result =
        DATASTORE_SERVICE.prepare(query).asList(FetchOptions.Builder.withLimit(SCENES_LIMIT));
    List<Scene> scenes = result.stream().map(Scene::new).collect(Collectors.toList());
    resp.getWriter().print(Util.GSON.toJson(scenes));
  }

  /**
   * Saves the scene to Datastore, and its image to Storage.
   *
   * @param req with the scene data to save.
   */
  private Scene saveScene(HttpServletRequest req)
      throws IOException, ServletException, GeneralSecurityException {
    Part imagePart = req.getPart(Scene.IMAGE_PART);
    Part directorPart = req.getPart(Scene.DIRECTOR_ID_PART);
    Part createdPart = req.getPart(Scene.CREATED_PART);
    if (imagePart == null) throw new IOException("Missing scene image.");
    if (directorPart == null) throw new IOException("Missing scene director ID.");
    if (createdPart == null) throw new IOException("Missing scene created timestamp.");
    Scene scene = new Scene(Long.parseLong(Util.inputStreamToString(directorPart.getInputStream())),
        new Date(Long.parseLong(Util.inputStreamToString(createdPart.getInputStream()))));
    // Saves the image to storage.
    // TODO(ohad): couple storage upload success with datastore saving success.
    StorageUtil.uploadStream(scene.getImagePath(),
        imagePart.getContentType(),
        imagePart.getInputStream(),
        bucketName);
    scene.setImageSignedUrl(
        UrlSigner.getSignedUrl(privateKey, bucketName + "/" + scene.getImagePath()));
    // Saves the scene to Datastore.
    Entity entity = new Entity(Scene.DATASTORE_KIND);
    entity.setProperty(Scene.DATASTORE_CREATED, scene.getCreated());
    entity.setProperty(Scene.DATASTORE_DIRECTOR_ID, scene.getDirectorId());
    entity.setProperty(Scene.DATASTORE_IMAGE_SIGNED_URL, scene.getImageSignedUrl());
    DATASTORE_SERVICE.put(entity);
    // Updates the scene with the generated key.
    scene.setId(entity.getKey().getId());

    return scene;
  }
}
