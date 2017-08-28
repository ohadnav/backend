package com.truethat.backend.servlet;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.KeyFactory;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.stream.Collectors.toList;

/**
 * Proudly created by ohad on 01/06/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/TheaterApi.java</a>
 * @ios <a>https://github.com/true-that/ios/blob/master/TrueThat/Model/Network/TheaterApi.swift</a>
 */
@WebServlet(value = "/theater", name = "Theater")
public class TheaterServlet extends BaseServlet {
  @VisibleForTesting
  static final int FETCH_LIMIT = 10;

  @SuppressWarnings("RedundantIfStatement")
  static boolean isValidUser(Datastore datastore, KeyFactory userKeyFactory, User user,
      StringBuilder errorBuilder) {
    if (user.getId() == null) {
      errorBuilder.append("missing user ID.");
      return false;
    }
    if (datastore.get(userKeyFactory.newKey(user.getId())) == null) {
      errorBuilder.append("user with ID ")
          .append(user.getId())
          .append(" not found.");
      return false;
    }
    return true;
  }

  private static boolean isValidReactable(Reactable reactable) {
    return reactable.getDirector() != null;
  }

  /**
   * Retrieves {@link Reactable}s from the Datastore.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User user = Util.GSON.fromJson(req.getReader(), User.class);
    if (user == null) throw new IOException("Missing user.");
    StringBuilder errorBuilder = new StringBuilder();
    if (!isValidUser(datastore, userKeyFactory, user, errorBuilder)) {
      throw new IOException("Invalid user: " + errorBuilder);
    }
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
    reactables = reactables.stream().filter(TheaterServlet::isValidReactable).collect(toList());
    resp.getWriter().print(Util.GSON.toJson(reactables));
  }
}
