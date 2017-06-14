package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.User;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 14/06/2017.
 */
public class AuthServletTest {
  private static final LocalServiceTestHelper HELPER =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private static final String PHONE_NUMBER = "+0123456789";
  private static final String DEVICE_ID = "my-iPhone";
  private static DatastoreService datastoreService;
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  private StringWriter responseWriter;
  private AuthServlet authServlet;

  /**
   * Starts the local Datastore emulator.
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    HELPER.setUp();
    datastoreService = DatastoreServiceFactory.getDatastoreService();
    authServlet = new AuthServlet();
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
  }
  /**
   * Stops the local Datastore emulator.
   */
  @After
  public void tearDown() throws Exception {
    HELPER.tearDown();
  }

  @Test
  public void createUser() throws Exception {
    User user = new User(PHONE_NUMBER, DEVICE_ID);
    // Mocks a request body with user.
    when(mockRequest.getReader()).thenReturn(
        new BufferedReader(new StringReader(Util.GSON.toJson(user))));
    // Sends the POST request
    authServlet.doPost(mockRequest, mockResponse);
    // Retrieves the saved user from datastore.
    Entity userEntity = datastoreService.prepare(new Query(User.DATASTORE_KIND)).asSingleEntity();
    // Assert the saved user matches the provided one, and has a join date.
    assertEquals(user.getDeviceId(), userEntity.getProperty(User.DATASTORE_DEVICE_ID));
    assertEquals(user.getPhoneNumber(), userEntity.getProperty(User.DATASTORE_PHONE_NUMBER));
    assertNotNull(userEntity.getProperty(User.DATASTORE_JOINED));
    // Assert the response contains a user ID, and matches the provided one.
    String response = responseWriter.toString();
    User respondedUser = Util.GSON.fromJson(response, User.class);
    assertEquals(user.getDeviceId(), respondedUser.getDeviceId());
    assertEquals(user.getPhoneNumber(), respondedUser.getPhoneNumber());
    assertNotNull(respondedUser.getId());
  }

  @Test
  public void similarUser_deviceId() throws Exception {
    User user = new User(null, DEVICE_ID);
    // Puts an entity into datastore, that is similar to user.
    Entity existingUserEntity = new Entity(User.DATASTORE_KIND);
    existingUserEntity.setProperty(User.DATASTORE_DEVICE_ID, user.getDeviceId());
    existingUserEntity.setProperty(User.DATASTORE_JOINED, new Date());
    datastoreService.put(existingUserEntity);
    // Mocks a request body with user.
    when(mockRequest.getReader()).thenReturn(
        new BufferedReader(new StringReader(Util.GSON.toJson(user))));
    // Sends the POST request
    authServlet.doPost(mockRequest, mockResponse);
    // Retrieves the saved user from datastore.
    List<Entity> savedEntities = datastoreService.prepare(new Query(User.DATASTORE_KIND)).asList(
        FetchOptions.Builder.withDefaults());
    // Assert no new entity was saved.
    assertEquals(1, savedEntities.size());
    // Assert the response contains a user ID, and matches the provided one.
    String response = responseWriter.toString();
    User respondedUser = Util.GSON.fromJson(response, User.class);
    assertEquals(user.getDeviceId(), respondedUser.getDeviceId());
    assertNotNull(respondedUser.getId());
  }

  @Test
  public void similarUser_phoneNumber() throws Exception {
    User user = new User(PHONE_NUMBER, null);
    // Puts an entity into datastore, that is similar to user.
    Entity existingUserEntity = new Entity(User.DATASTORE_KIND);
    existingUserEntity.setProperty(User.DATASTORE_PHONE_NUMBER, user.getPhoneNumber());
    existingUserEntity.setProperty(User.DATASTORE_JOINED, new Date());
    datastoreService.put(existingUserEntity);
    // Mocks a request body with user.
    when(mockRequest.getReader()).thenReturn(
        new BufferedReader(new StringReader(Util.GSON.toJson(user))));
    // Sends the POST request
    authServlet.doPost(mockRequest, mockResponse);
    // Retrieves the saved user from datastore.
    List<Entity> savedEntities = datastoreService.prepare(new Query(User.DATASTORE_KIND)).asList(
        FetchOptions.Builder.withDefaults());
    // Assert no new entity was saved.
    assertEquals(1, savedEntities.size());
    // Assert the response contains a user ID, and matches the provided one.
    String response = responseWriter.toString();
    User respondedUser = Util.GSON.fromJson(response, User.class);
    assertEquals(user.getPhoneNumber(), respondedUser.getPhoneNumber());
    assertNotNull(respondedUser.getId());
  }
}