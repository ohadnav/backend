package com.truethat.backend.servlet;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.User;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.stream.Collectors.toList;

/**
 * Proudly created by ohad on 01/06/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/TheaterAPI.java</a>
 */
@WebServlet(value = "/theater", name = "Theater")
public class TheaterServlet extends HttpServlet {
  @VisibleForTesting
  static final int FETCH_LIMIT = 10;
  private Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private ReactableEnricher enricher = new ReactableEnricher(datastore);

  public TheaterServlet setDatastore(Datastore datastore) {
    this.datastore = datastore;
    enricher = new ReactableEnricher(datastore);
    return this;
  }

  /**
   * Retrieves {@link Reactable}s from the Datastore.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User user = Util.GSON.fromJson(req.getReader(), User.class);
    if (user == null) throw new IOException("Missing user");
    Query<Entity> queryGt = Query.newEntityQueryBuilder().setKind(Reactable.DATASTORE_KIND)
        .setFilter(PropertyFilter.gt(Reactable.DATASTORE_DIRECTOR_ID, user.getId()))
        .build();
    Query<Entity> queryLt = Query.newEntityQueryBuilder().setKind(Reactable.DATASTORE_KIND)
        .setFilter(PropertyFilter.lt(Reactable.DATASTORE_DIRECTOR_ID, user.getId()))
        .build();
    List<Reactable> reactables = Lists.newArrayList(datastore.run(queryGt))
        .stream()
        .map(Reactable::fromEntity)
        .collect(toList());
    reactables.addAll(Lists.newArrayList(datastore.run(queryLt))
        .stream()
        .map(Reactable::fromEntity)
        .collect(toList()));
    // Sort by recency
    reactables.sort(Comparator.comparing(Reactable::getCreated).reversed());
    reactables = reactables.subList(0, Math.min(FETCH_LIMIT, reactables.size()));
    enricher.enrichReactables(reactables, user);
    resp.getWriter().print(Util.GSON.toJson(reactables));
  }
}
