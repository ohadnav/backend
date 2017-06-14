package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Proudly created by ohad on 12/06/2017.
 *
 * @android https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/auth/AuthAPI.java
 */

@WebServlet(value = "/auth", name = "Auth")
public class AuthServlet extends HttpServlet {
  private static final DatastoreService DATASTORE_SERVICE =
      DatastoreServiceFactory.getDatastoreService();

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User user = Util.GSON.fromJson(req.getReader(), User.class);
    Entity userEntity = user.toEntity();
    // Entity whose key is ultimately responded.
    Entity toRespondEntity = userEntity;
    boolean shouldCreateNewUser = true;
    Query similarUsersQuery = similarUsersQuery(userEntity);
    if (similarUsersQuery != null) {
      Iterator<Entity> existingUsers =
          DATASTORE_SERVICE.prepare(similarUsersQuery).asIterable().iterator();
      if (existingUsers.hasNext()) {
        // If a similar user was found, then don't create a new one in datastore,
        // and use its ID for the response.
        toRespondEntity = existingUsers.next();
        shouldCreateNewUser = false;
      }
    }
    if (shouldCreateNewUser) {
      DATASTORE_SERVICE.put(toRespondEntity);
    }
    // Updates user ID, and responds it to client.
    user.setId(toRespondEntity.getKey().getId());
    resp.getWriter().print(Util.GSON.toJson(user));
  }

  /**
   * Creates a query that looks for similar users. Similar users share the same device ID or phone
   * number. For the time being, these two attributes are assumed to be unique.
   *
   * @param userEntity that be authenticated.
   * @return a query that looks for similar users. Null if no query filters could be derived from
   * {@code userEntity}.
   */
  private @Nullable Query similarUsersQuery(Entity userEntity) {
    Query query = new Query(User.DATASTORE_KIND);
    List<Query.Filter> filters = new ArrayList<>();
    if (userEntity.hasProperty(User.DATASTORE_DEVICE_ID)) {
      filters.add(new Query.FilterPredicate(User.DATASTORE_DEVICE_ID, Query.FilterOperator.EQUAL,
          userEntity.getProperty(User.DATASTORE_DEVICE_ID)));
    }
    if (userEntity.hasProperty(User.DATASTORE_PHONE_NUMBER)) {
      filters.add(new Query.FilterPredicate(User.DATASTORE_PHONE_NUMBER, Query.FilterOperator.EQUAL,
          userEntity.getProperty(User.DATASTORE_PHONE_NUMBER)));
    }
    if (filters.size() == 1) {
      query.setFilter(filters.get(0));
    } else if (filters.size() > 1) {
      query.setFilter(Query.CompositeFilterOperator.or(filters));
    } else {
      // No filters could be created.
      query = null;
    }
    return query;
  }
}
