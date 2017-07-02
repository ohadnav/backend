package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.User;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Proudly created by ohad on 14/06/2017.
 */
public class AuthServletTest extends BaseServletTestSuite {
  @Test
  public void createUser() throws Exception {
    saveUser(defaultUser);
    // Retrieves the saved user from datastore.
    Entity userEntity = datastoreService.prepare(new Query(User.DATASTORE_KIND)).asSingleEntity();
    // Assert the saved user matches the provided one, and has a join date.
    assertUserAndEntityAreEquals(defaultUser, userEntity);
    assertNotNull(userEntity.getProperty(User.DATASTORE_JOINED));
    // Assert the response contains a user ID, and matches the provided one.
    String response = responseWriter.toString();
    User respondedUser = Util.GSON.fromJson(response, User.class);
    assertEquals(defaultUser, respondedUser);
  }

  @SuppressWarnings("Duplicates") @Test
  public void similarUser_deviceId() throws Exception {
    User expected = new User(null, DEVICE_ID, FIRST_NAME, LAST_NAME, NOW);
    saveUser(expected);
    saveUser(expected);
    // Retrieves the saved user from datastore.
    List<Entity> savedEntities = datastoreService.prepare(new Query(User.DATASTORE_KIND)).asList(
        FetchOptions.Builder.withDefaults());
    // Assert no new entity was saved.
    assertEquals(1, savedEntities.size());
    // Assert the response contains a user ID, and matches the provided one.
    String response = responseWriter.toString();
    User respondedUser = Util.GSON.fromJson(response, User.class);
    assertEquals(expected, respondedUser);
  }

  @SuppressWarnings("Duplicates") @Test
  public void similarUser_phoneNumber() throws Exception {
    User expected = new User(PHONE_NUMBER, null, FIRST_NAME, LAST_NAME, NOW);
    saveUser(expected);
    saveUser(expected);
    // Retrieves the saved user from datastore.
    List<Entity> savedEntities = datastoreService.prepare(new Query(User.DATASTORE_KIND)).asList(
        FetchOptions.Builder.withDefaults());
    // Assert no new entity was saved.
    assertEquals(1, savedEntities.size());
    // Assert the response contains a user ID, and matches the provided one.
    String response = responseWriter.toString();
    User respondedUser = Util.GSON.fromJson(response, User.class);
    assertEquals(expected, respondedUser);
  }

  @Test
  public void updateUser() throws Exception {
    User user = new User("old", DEVICE_ID, "old", "old", NOW);
    saveUser(user);
    // Retrieves the saved user from datastore.
    Entity userEntity = datastoreService.prepare(new Query(User.DATASTORE_KIND)).asSingleEntity();
    // Assert the saved user has old data.
    assertUserAndEntityAreEquals(user, userEntity);
    // Saves the first ID
    long id = Util.GSON.fromJson(responseWriter.toString(), User.class).getId();
    assertNotNull(id);
    user = new User(PHONE_NUMBER, DEVICE_ID, FIRST_NAME, LAST_NAME, NOW);
    saveUser(user);
    // Retrieves the all saved users.
    List<Entity> savedEntities = datastoreService.prepare(new Query(User.DATASTORE_KIND))
        .asList(FetchOptions.Builder.withDefaults());
    // Assert only one user was saved.
    assertEquals(1, savedEntities.size());
    // Assert the saved user matches the updated one.
    assertUserAndEntityAreEquals(user, savedEntities.get(0));
    // Assert the saved user has the same ID.
    assertEquals(id, Util.GSON.fromJson(responseWriter.toString(), User.class).getId());
  }

  private void assertUserAndEntityAreEquals(User user, Entity entity) {
    assertEquals(user.getDeviceId(), entity.getProperty(User.DATASTORE_DEVICE_ID));
    assertEquals(user.getPhoneNumber(), entity.getProperty(User.DATASTORE_PHONE_NUMBER));
    assertEquals(user.getFirstName(), entity.getProperty(User.DATASTORE_FIRST_NAME));
    assertEquals(user.getLastName(), entity.getProperty(User.DATASTORE_LAST_NAME));
  }
}