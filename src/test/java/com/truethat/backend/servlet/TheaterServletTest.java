package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.gson.reflect.TypeToken;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Emotion;
import com.truethat.backend.model.EventType;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.ReactableEvent;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Test;

import static com.truethat.backend.common.TestUtil.assertEqualsForEntityAndReactableEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 01/06/2017.
 */
public class TheaterServletTest extends BaseServletTestSuite {
  private User director =
      new User(PHONE_NUMBER + "-2", DEVICE_ID + "-2", FIRST_NAME, LAST_NAME, NOW);
  private Scene scene;

  @Override public void setUp() throws Exception {
    super.setUp();
    saveUser(director);
    saveUser(defaultUser);
    scene = new Scene(director.getId(), NOW, null);
  }

  @Test
  public void doGet() throws Exception {
    prepareGet();
    saveScene(scene);
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doGet(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
    }.getType());
    assertEquals(1, respondedReactables.size());
    // Enriches scene
    ReactableEnricher.enrich(Collections.singletonList(scene), defaultUser);
    assertEquals(scene, respondedReactables.get(0));
  }

  @Test(expected = IOException.class)
  public void doGet_missingUser() throws Exception {
    saveScene(scene);
    when(mockRequest.getParameter(StudioServlet.USER_PARAM)).thenReturn(null);
    theaterServlet.doGet(mockRequest, mockResponse);
  }

  @SuppressWarnings("Duplicates") @Test
  public void doGet_emptyDatastore() throws Exception {
    // Not saving a scene.
    prepareGet();
    // Sends the GET request
    theaterServlet.doGet(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
        }.getType());
    assertTrue(respondedReactables.isEmpty());
  }

  @Test
  public void doGet_multipleReactables() throws Exception {
    prepareGet();
    // Add 11 scenes to datastore.
    for (int i = 0; i < TheaterServlet.GET_LIMIT + 1; i++) {
      saveScene(new Scene(director.getId(), new Date(NOW.getTime() + i), null));
    }
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doGet(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> reactables = Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
    }.getType());
    // Asserts no more than TheaterServlet.GET_LIMIT are responded.
    assertEquals(TheaterServlet.GET_LIMIT, reactables.size());
    // Asserts the scenes are sorted by recency.
    for (int i = TheaterServlet.GET_LIMIT; i > 0; i--) {
      Scene scene = (Scene) reactables.get(TheaterServlet.GET_LIMIT - i);
      assertEquals(new Date(NOW.getTime() + i), scene.getCreated());
      // Should have image url
      assertNotNull(scene.getImageSignedUrl());
    }
  }

  @Test
  public void doPost_viewEvent() throws Exception {
    saveScene(scene);
    ReactableEvent reactableEvent =
        new ReactableEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTABLE_VIEW, null);
    // Saves the event.
    saveReactableEvent(reactableEvent);
    // Retrieves the saves event from datastore.
    Entity savedEntity =
        datastoreService.prepare(new Query(ReactableEvent.DATASTORE_KIND)).asSingleEntity();
    assertEqualsForEntityAndReactableEvent(savedEntity, reactableEvent);
  }

  @Test
  public void doPost_reactionEvent() throws Exception {
    saveScene(scene);
    ReactableEvent reactableEvent =
        new ReactableEvent(defaultUser.getId(), scene.getId(), NOW, EventType.REACTABLE_REACTION,
            Emotion.HAPPY);
    // Saves the event.
    saveReactableEvent(reactableEvent);
    // Retrieves the saves event from datastore.
    Entity savedEntity =
        datastoreService.prepare(new Query(ReactableEvent.DATASTORE_KIND)).asSingleEntity();
    assertEqualsForEntityAndReactableEvent(savedEntity, reactableEvent);
  }

  private void prepareGet() throws Exception {
    when(mockRequest.getParameter(TheaterServlet.USER_PARAM)).thenReturn(
        Util.GSON.toJson(defaultUser));
    resetResponseMock();
  }
}