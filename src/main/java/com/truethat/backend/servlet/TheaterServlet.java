package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.common.annotations.VisibleForTesting;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.ReactableEvent;
import com.truethat.backend.model.Scene;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Proudly created by ohad on 01/06/2017.
 *
 * @android <a>https://goo.gl/xsORJL</a>
 */
@WebServlet(value = "/theater", name = "Theater")
public class TheaterServlet extends HttpServlet {
  @VisibleForTesting
  static final int SCENES_LIMIT = 10;
  private static final DatastoreService DATASTORE_SERVICE =
      DatastoreServiceFactory.getDatastoreService();

  /**
   * Retrieves scenes from the Datastore.
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Query query = new Query(Scene.DATASTORE_KIND).addSort(Scene.DATASTORE_CREATED,
        Query.SortDirection.DESCENDING);
    List<Entity> result =
        DATASTORE_SERVICE.prepare(query).asList(FetchOptions.Builder.withLimit(SCENES_LIMIT));
    List<Scene> scenes = result.stream().map(Scene::new).collect(Collectors.toList());
    resp.getWriter().print(Util.GSON.toJson(scenes));
  }

  /**
   * Saves events to Datastore.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    ReactableEvent reactableEvent =
        Util.GSON.fromJson(req.getParameter(ReactableEvent.EVENT_FIELD), ReactableEvent.class);
    DATASTORE_SERVICE.put(reactableEvent.toEntity());
  }
}
