package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.User;
import java.io.IOException;
import java.util.Iterator;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Proudly created by ohad on 12/06/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/AuthAPI.java</a>
 */

@WebServlet(value = "/auth", name = "Auth")
public class AuthServlet extends HttpServlet {
  private static final DatastoreService DATASTORE_SERVICE =
      DatastoreServiceFactory.getDatastoreService();

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User user = Util.GSON.fromJson(req.getReader(), User.class);
    if (user == null) throw new IOException("Missing user");
    Entity userEntity = user.toEntity();
    // If ID is missing, then it is a sign up or a sign in.
    if (!user.hasId()) {
      // Entity whose key is ultimately responded.
      Entity toPut = userEntity;
      Entity similarUserEntity = similarUser(userEntity);
      boolean shouldCreateNewUser = true;
      if (similarUserEntity != null) {
        // If a similar user was found, then don't create a new one in datastore,
        // and use its ID for the response.
        toPut = similarUserEntity;
        shouldCreateNewUser = false;
      }
      if (shouldCreateNewUser || updateIfShould(toPut, userEntity)) {
        DATASTORE_SERVICE.put(toPut);
      }
      resp.getWriter().print(Util.GSON.toJson(new User(toPut)));
    } else {
      // Otherwise, it is a routine authentication.
      Entity existingUser = findUser(user);
      //noinspection StatementWithEmptyBody
      if (existingUser == null) {
        // Auth failed
      } else {
        if (updateIfShould(existingUser, userEntity)) {
          DATASTORE_SERVICE.put(existingUser);
        }
        resp.getWriter().print(Util.GSON.toJson(new User(existingUser)));
      }
    }
  }

  /**
   * Creates a query that looks for similar users. Similar users share the same device ID or user
   * ID.
   *
   * @param userEntity that is being authenticated.
   * @return a query that looks for similar users. Null if no query filters could be derived from
   * {@code userEntity}.
   */
  private @Nullable Entity similarUser(Entity userEntity) {
    Entity similarUserEntity = null;
    if (userEntity.hasProperty(User.DATASTORE_DEVICE_ID)) {
      Query query = new Query(User.DATASTORE_KIND);
      query.setFilter(
          new Query.FilterPredicate(User.DATASTORE_DEVICE_ID, Query.FilterOperator.EQUAL,
              userEntity.getProperty(User.DATASTORE_DEVICE_ID)));
      Iterator<Entity> existingUsers = DATASTORE_SERVICE.prepare(query).asIterable().iterator();
      if (existingUsers.hasNext()) {
        similarUserEntity = existingUsers.next();
      }
    }
    return similarUserEntity;
  }

  private @Nullable Entity findUser(User user) {
    Query query = new Query(User.DATASTORE_KIND);
    query.setFilter(
        new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.EQUAL,
            KeyFactory.createKey(User.DATASTORE_KIND, user.getId())));
    return DATASTORE_SERVICE.prepare(query).asSingleEntity();
  }

  /**
   * Updates {@code existing} with fresh data from {@code fromClient}. More technically, looks for
   * non-null fields in {@code fromClient} that have different values than {@code existing}.
   *
   * @param existing   {@link User} entity that is found in datastore
   * @param fromClient {@link User} entity that was provided by the client for authentication.
   * @return whether changes were applied
   */
  private boolean updateIfShould(Entity existing, Entity fromClient) {
    boolean updated = false;
    if (fromClient.getProperty(User.DATASTORE_FIRST_NAME) != null &&
        existing.getProperty(User.DATASTORE_FIRST_NAME) != fromClient.getProperty(
            User.DATASTORE_FIRST_NAME)) {
      existing.setProperty(User.DATASTORE_FIRST_NAME,
          fromClient.getProperty(User.DATASTORE_FIRST_NAME));
      updated = true;
    }
    if (fromClient.getProperty(User.DATASTORE_LAST_NAME) != null &&
        existing.getProperty(User.DATASTORE_LAST_NAME) != fromClient.getProperty(
            User.DATASTORE_LAST_NAME)) {
      existing.setProperty(User.DATASTORE_LAST_NAME,
          fromClient.getProperty(User.DATASTORE_LAST_NAME));
      updated = true;
    }
    if (fromClient.getProperty(User.DATASTORE_DEVICE_ID) != null &&
        existing.getProperty(User.DATASTORE_DEVICE_ID) != fromClient.getProperty(
            User.DATASTORE_DEVICE_ID)) {
      existing.setProperty(User.DATASTORE_DEVICE_ID,
          fromClient.getProperty(User.DATASTORE_DEVICE_ID));
      updated = true;
    }
    return updated;
  }
}
