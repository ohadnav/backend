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
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import com.truethat.backend.storage.DefaultStorageClient;
import com.truethat.backend.storage.DefaultUrlSigner;
import com.truethat.backend.storage.StorageClient;
import com.truethat.backend.storage.UrlSigner;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
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
  @VisibleForTesting
  static final String CREDENTIALS_PATH = "credentials/";
  @VisibleForTesting static final String USER_PARAM = "user";
  @VisibleForTesting
  static final int GET_LIMIT = 10;
  private static final Logger LOG = Logger.getLogger(StudioServlet.class.getName());
  private static final DatastoreService DATASTORE_SERVICE =
      DatastoreServiceFactory.getDatastoreService();
  private StorageClient storageClient;
  private UrlSigner urlSigner = new DefaultUrlSigner();
  private String bucketName = System.getenv("STUDIO_BUCKET");
  private String privateKey;

  public String getBucketName() {
    return bucketName;
  }

  @VisibleForTesting
  void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public DatastoreService getDatastoreService() {
    return DATASTORE_SERVICE;
  }

  public StorageClient getStorageClient() {
    return storageClient;
  }

  @VisibleForTesting void setStorageClient(StorageClient storageClient) {
    this.storageClient = storageClient;
  }

  public UrlSigner getUrlSigner() {
    return urlSigner;
  }

  void setUrlSigner(UrlSigner urlSigner) {
    this.urlSigner = urlSigner;
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
    // Initializes storage client
    try {
      storageClient = new DefaultStorageClient();
    } catch (IOException | GeneralSecurityException e) {
      e.printStackTrace();
      LOG.severe("Could not initialize storage client: " + e.getMessage());
      throw new ServletException("Could not initialize storage client: " + e.getMessage());
    }
  }

  /**
   * Getting the user's repertoire, i.e. the {@link Reactable}s he had created.
   *
   * @param req with the user ID
   */
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (req.getParameter(USER_PARAM) == null) {
      throw new IOException("Missing " + USER_PARAM + " parameter.");
    }
    User user = Util.GSON.fromJson(req.getParameter(USER_PARAM), User.class);
    Query query = new Query(Reactable.DATASTORE_KIND).setFilter(
        new Query.FilterPredicate(Reactable.DATASTORE_DIRECTOR_ID, Query.FilterOperator.EQUAL,
            user.getId())).addSort(Reactable.DATASTORE_CREATED,
        Query.SortDirection.DESCENDING);
    List<Entity> result =
        DATASTORE_SERVICE.prepare(query).asList(FetchOptions.Builder.withLimit(GET_LIMIT));
    List<Reactable> reactables =
        result.stream().map(Reactable::fromEntity).collect(Collectors.toList());
    ReactableEnricher.enrich(reactables, user);
    resp.getWriter().print(Util.GSON.toJson(reactables));
  }

  /**
   * Saves the {@link Reactable} within the request to storage and datastore, and response the saved
   * {@link Reactable} The request is expected to be multipart HTTP request with three parts: 1)
   * image 2) director ID as string 3) created timestamp as string. (i.e. '1234567890') <p> Part
   * names are found in {@link Scene}.
   *
   * @param req multipart request with the scene image and director ID.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      Part reactablePart = req.getPart(Reactable.REACTABLE_PART);
      if (reactablePart == null) throw new IOException("Missing reactable, how dare you?");
      Reactable toSave =
          Util.GSON.fromJson(new InputStreamReader(reactablePart.getInputStream()),
              Reactable.class);
      toSave.save(req, this);
      resp.getWriter().print(Util.GSON.toJson(toSave));
    } catch (Exception e) {
      e.printStackTrace();
      LOG.severe("Oh oh... " + e.getMessage());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      throw new ServletException(e.getMessage());
    }
  }
}
