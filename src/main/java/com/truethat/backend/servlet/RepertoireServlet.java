package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.common.annotations.VisibleForTesting;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.User;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Proudly created by ohad on 03/07/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/RepertoireAPI.java</a>
 */
@WebServlet(value = "/repertoire", name = "Repertoire")
public class RepertoireServlet extends HttpServlet {
  @VisibleForTesting static final int FETCH_LIMIT = 10;
  private static final DatastoreService DATASTORE_SERVICE =
      DatastoreServiceFactory.getDatastoreService();

  /**
   * Getting the user's repertoire, i.e. the {@link Reactable}s he had created.
   *
   * @param req with {@link User} in its body.
   */
  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User user = Util.GSON.fromJson(req.getReader(), User.class);
    if (user == null) throw new IOException("Missing user");
    Query query = new Query(Reactable.DATASTORE_KIND).setFilter(
        new Query.FilterPredicate(Reactable.DATASTORE_DIRECTOR_ID, Query.FilterOperator.EQUAL,
            user.getId())).addSort(Reactable.DATASTORE_CREATED,
        Query.SortDirection.DESCENDING);
    List<Entity> result =
        DATASTORE_SERVICE.prepare(query).asList(FetchOptions.Builder.withLimit(FETCH_LIMIT));
    List<Reactable> reactables =
        result.stream().map(Reactable::fromEntity).collect(Collectors.toList());
    ReactableEnricher.enrich(reactables, user);
    resp.getWriter().print(Util.GSON.toJson(reactables));
  }
}
