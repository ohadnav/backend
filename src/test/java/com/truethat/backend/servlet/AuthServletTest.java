package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.User;
import java.util.List;
import org.junit.Test;

import static com.truethat.backend.common.TestUtil.toBufferedReader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

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

  @Test
  public void similarUser_deviceId() throws Exception {
    saveUser(defaultUser);
    defaultUser.setId(null);
    saveUser(defaultUser);
    // Retrieves the saved user from datastore.
    List<Entity> savedEntities = datastoreService.prepare(new Query(User.DATASTORE_KIND)).asList(
        FetchOptions.Builder.withDefaults());
    // Assert no new entity was saved.
    assertEquals(1, savedEntities.size());
    // Assert the response contains a user ID, and matches the provided one.
    String response = responseWriter.toString();
    User respondedUser = Util.GSON.fromJson(response, User.class);
    assertEquals(defaultUser, respondedUser);
  }

  @Test
  public void updateUser() throws Exception {
    User user = new User(DEVICE_ID, "old", "old", NOW);
    saveUser(user);
    // Retrieves the saved user from datastore.
    Entity userEntity = datastoreService.prepare(new Query(User.DATASTORE_KIND)).asSingleEntity();
    // Assert the saved user has old data.
    assertUserAndEntityAreEquals(user, userEntity);
    // Saves the first ID
    User firstUser = Util.GSON.fromJson(responseWriter.toString(), User.class);
    assertTrue(firstUser.hasId());
    user = new User(DEVICE_ID, FIRST_NAME, LAST_NAME, NOW);
    saveUser(user);
    // Retrieves the all saved users.
    List<Entity> savedEntities = datastoreService.prepare(new Query(User.DATASTORE_KIND))
        .asList(FetchOptions.Builder.withDefaults());
    // Assert only one user was saved.
    assertEquals(1, savedEntities.size());
    // Assert the saved user matches the updated one.
    assertUserAndEntityAreEquals(user, savedEntities.get(0));
    // Assert the saved user has the same ID.
    assertEquals(firstUser.getId(),
        Util.GSON.fromJson(responseWriter.toString(), User.class).getId());
  }

  @Test
  public void authenticateUser_successfulAuth() throws Exception {
    saveUser(defaultUser);
    resetResponseMock();
    // Auth again with the same user.
    when(mockRequest.getReader()).thenReturn(toBufferedReader(Util.GSON.toJson(defaultUser)));
    authServlet.doPost(mockRequest, mockResponse);
    // Should be successful
    User response = Util.GSON.fromJson(responseWriter.toString(), User.class);
    assertEquals(defaultUser, response);
    // Should not save additional entity
    List<Entity> savedEntities = datastoreService.prepare(new Query(User.DATASTORE_KIND))
        .asList(FetchOptions.Builder.withDefaults());
    assertEquals(1, savedEntities.size());
  }

  @Test
  public void authenticateUser_authFailed() throws Exception {
    defaultUser.setId(-1L);
    resetResponseMock();
    when(mockRequest.getReader()).thenReturn(toBufferedReader(Util.GSON.toJson(defaultUser)));
    // Auth again with the same user.
    authServlet.doPost(mockRequest, mockResponse);
    // Should fail
    // We cant test for status code :-(
    assertTrue(responseWriter.toString().isEmpty());
    // Should not save any users
    List<Entity> savedEntities = datastoreService.prepare(new Query(User.DATASTORE_KIND))
        .asList(FetchOptions.Builder.withDefaults());
    assertTrue(savedEntities.isEmpty());
  }

  @Test
  public void authenticateUser_updateData() throws Exception {
    saveUser(defaultUser);
    resetResponseMock();
    defaultUser.setFirstName(defaultUser.getFirstName() + "ush");
    when(mockRequest.getReader()).thenReturn(toBufferedReader(Util.GSON.toJson(defaultUser)));
    // Auth again with the same user.
    authServlet.doPost(mockRequest, mockResponse);
    // Should update data
    List<Entity> savedEntities = datastoreService.prepare(new Query(User.DATASTORE_KIND))
        .asList(FetchOptions.Builder.withDefaults());
    // Assert only one user was saved.
    assertEquals(1, savedEntities.size());
    // Assert the saved user matches the updated one.
    assertUserAndEntityAreEquals(defaultUser, savedEntities.get(0));
  }

  private void assertUserAndEntityAreEquals(User user, Entity entity) {
    assertEquals(user.getDeviceId(), entity.getProperty(User.DATASTORE_DEVICE_ID));
    assertEquals(user.getFirstName(), entity.getProperty(User.DATASTORE_FIRST_NAME));
    assertEquals(user.getLastName(), entity.getProperty(User.DATASTORE_LAST_NAME));
  }
}