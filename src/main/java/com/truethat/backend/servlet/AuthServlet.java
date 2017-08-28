package com.truethat.backend.servlet;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.google.common.base.Strings;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.User;
import java.io.IOException;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Proudly created by ohad on 12/06/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/AuthApi.java</a>
 * @ios <a>https://github.com/true-that/ios/blob/master/TrueThat/Model/Network/AuthApi.swift</a>
 */

@WebServlet(value = "/auth", name = "Auth")
public class AuthServlet extends BaseServlet {

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User user = Util.GSON.fromJson(req.getReader(), User.class);
    User respondedUser = null;
    // Validating input
    if (user == null) throw new IOException("Missing user");
    FullEntity userEntity = user.toEntityBuilder(userKeyFactory).build();
    // If ID is missing, then it is a sign up or a sign in.
    if (user.getId() == null) {
      Entity similarUserEntity = similarUser(user);
      if (similarUserEntity != null) {
        // If a similar user was found, then don't create a new one in datastore,
        // and use its ID for the response.
        Entity mergedEntity = merge(similarUserEntity, user).build();
        datastore.update(mergedEntity);
        respondedUser = new User(mergedEntity);
      } else {
        // Validate input.
        StringBuilder errorBuilder = new StringBuilder();
        if (!isValidUser(user, errorBuilder)) {
          throw new IOException("Invalid user: " + errorBuilder);
        }
        // Put a new entity in datastore.
        respondedUser = new User(datastore.add(userEntity));
      }
    } else {
      // Otherwise, it is a routine authentication.
      Entity existingUser = findUser(user);
      //noinspection StatementWithEmptyBody
      if (existingUser == null) {
        // Auth failed
      } else {
        Entity mergedEntity = merge(existingUser, user).build();
        if (!mergedEntity.equals(existingUser)) {
          // Should update existing user.
          datastore.update(mergedEntity);
        }
        respondedUser = new User(mergedEntity);
      }
    }
    if (respondedUser != null) {
      resp.getWriter().print(Util.GSON.toJson(respondedUser));
    }
  }

  /**
   * Creates a query that looks for similar users. Similar users share the same device ID or user
   * ID.
   *
   * @param user that is being authenticated.
   *
   * @return a query that looks for similar users. Null if no query filters could be derived from
   * {@code userEntity}.
   */
  private @Nullable Entity similarUser(User user) {
    Entity similarUserEntity = null;
    if (!Strings.isNullOrEmpty(user.getDeviceId())) {
      Query<Entity> query = Query.newEntityQueryBuilder().setKind(User.DATASTORE_KIND)
          .setFilter(StructuredQuery.PropertyFilter.eq(User.DATASTORE_DEVICE_ID,
              user.getDeviceId()))
          .build();
      QueryResults<Entity> existingUsers = datastore.run(query);
      if (existingUsers.hasNext()) {
        similarUserEntity = existingUsers.next();
      }
    }
    return similarUserEntity;
  }

  private @Nullable Entity findUser(User user) {
    if (user.getId() != null) {
      return datastore.get(userKeyFactory.newKey(user.getId()));
    }
    return null;
  }

  /**
   * Merges data from {@code fromClient} into {@code existing}. Data from client is preferred over
   * existing one.
   *
   * @param existing   {@link User} entity that is found in datastore
   * @param fromClient that was provided by the client for authentication.
   *
   * @return an entity builder with the merged data.
   */
  private Entity.Builder merge(Entity existing, User fromClient) {
    Entity.Builder builder = Entity.newBuilder(existing);
    if (!Strings.isNullOrEmpty(fromClient.getFirstName())) {
      builder.set(User.DATASTORE_FIRST_NAME, fromClient.getFirstName());
    }
    if (!Strings.isNullOrEmpty(fromClient.getLastName())) {
      builder.set(User.DATASTORE_LAST_NAME, fromClient.getLastName());
    }
    if (!Strings.isNullOrEmpty(fromClient.getDeviceId())) {
      builder.set(User.DATASTORE_DEVICE_ID, fromClient.getDeviceId());
    }
    return builder;
  }

  private boolean isValidUser(User user, StringBuilder errorBuilder) {
    if (user.getFirstName() == null) {
      errorBuilder.append("missing first name");
      return false;
    } else if (user.getFirstName().length() <= 1) {
      errorBuilder.append(user.getFirstName()).append(" is too short for a first name.");
      return false;
    } else if (!user.getFirstName().matches("[a-z]*")) {
      errorBuilder.append("first name ")
          .append(user.getFirstName())
          .append(" has illegal characters (only lowercase english characters are allowed).");
      return false;
    }
    if (user.getLastName() == null) {
      errorBuilder.append("missing last name");
      return false;
    } else if (user.getLastName().length() <= 1) {
      errorBuilder.append(user.getLastName()).append(" is too short for a last name.");
      return false;
    } else if (!user.getLastName().matches("[a-z\\s]*")) {
      errorBuilder.append("last name ")
          .append(user.getLastName())
          .append(" has illegal characters (only lowercase english characters are allowed).");
      return false;
    }
    return true;
  }
}
