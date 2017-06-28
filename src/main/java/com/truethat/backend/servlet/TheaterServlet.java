package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.common.annotations.VisibleForTesting;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.ReactableEvent;
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
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/TheaterAPI.java</a>
 */
@WebServlet(value = "/theater", name = "Theater")
public class TheaterServlet extends HttpServlet {
  @VisibleForTesting
  static final int GET_LIMIT = 10;
  private static final DatastoreService DATASTORE_SERVICE =
      DatastoreServiceFactory.getDatastoreService();

  /**
   * Retrieves scenes from the Datastore.
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Query query = new Query(Reactable.DATASTORE_KIND).addSort(Reactable.DATASTORE_CREATED,
        Query.SortDirection.DESCENDING);
    List<Entity> result =
        DATASTORE_SERVICE.prepare(query).asList(FetchOptions.Builder.withLimit(GET_LIMIT));
    List<Reactable> reactables =
        result.stream().map(Reactable::fromEntity).collect(Collectors.toList());
    resp.getWriter().print(Util.GSON.toJson(reactables));
  }

  /**
   * Saves events to Datastore.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    ReactableEvent reactableEvent = Util.GSON.fromJson(req.getReader(), ReactableEvent.class);
    DATASTORE_SERVICE.put(reactableEvent.toEntity());
  }
}
