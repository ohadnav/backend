package com.truethat.backend.servlet;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.common.collect.Lists;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.User;
import java.util.List;
import org.junit.Test;

import static com.truethat.backend.common.TestUtil.toBufferedReader;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    Entity userEntity = datastore.get(userKeyFactory.newKey(defaultUser.getId()));
    // Assert the saved user matches the provided one, and has a join date.
    assertNotNull(userEntity);
    assertEquals(defaultUser, new User(userEntity));
    assertTrue(userEntity.contains(User.DATASTORE_JOINED));
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
    List<User> savedUsers = Lists.newArrayList(
        datastore.run(Query.newEntityQueryBuilder().setKind(User.DATASTORE_KIND).build()))
        .stream()
        .map(User::new)
        .collect(toList());
    // Assert only a single entity was saved.
    assertEquals(1, savedUsers.size());
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
    User savedUser = Lists.newArrayList(
        datastore.run(Query.newEntityQueryBuilder().setKind(User.DATASTORE_KIND).build()))
        .stream()
        .map(User::new)
        .collect(toList())
        .get(0);
    // Assert the saved user has old data.
    assertEquals(user, savedUser);
    // Saves the first ID
    User firstUser = Util.GSON.fromJson(responseWriter.toString(), User.class);
    assertTrue(firstUser.hasId());
    user = new User(DEVICE_ID, FIRST_NAME, LAST_NAME, NOW);
    saveUser(user);
    // Retrieves the all saved users.
    List<User> savedUsers = Lists.newArrayList(
        datastore.run(Query.newEntityQueryBuilder().setKind(User.DATASTORE_KIND).build()))
        .stream()
        .map(User::new)
        .collect(toList());
    // Assert only one user was saved.
    assertEquals(1, savedUsers.size());
    // Assert the saved user matches the updated one.
    assertEquals(user, savedUsers.get(0));
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
    List<User> savedUsers = Lists.newArrayList(
        datastore.run(Query.newEntityQueryBuilder().setKind(User.DATASTORE_KIND).build()))
        .stream()
        .map(User::new)
        .collect(toList());
    assertEquals(1, savedUsers.size());
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
    assertFalse(datastore.run(Query.newEntityQueryBuilder().setKind(User.DATASTORE_KIND).build())
        .hasNext());
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
    List<User> savedUsers = Lists.newArrayList(
        datastore.run(Query.newEntityQueryBuilder().setKind(User.DATASTORE_KIND).build()))
        .stream()
        .map(User::new)
        .collect(toList());
    // Assert only one user was saved.
    assertEquals(1, savedUsers.size());
    // Assert the saved user matches the updated one.
    assertEquals(defaultUser, savedUsers.get(0));
  }
}