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
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import org.junit.Test;

import static com.truethat.backend.common.TestUtil.assertEqualsForEntityAndReactableEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 01/06/2017.
 */
public class TheaterServletTest extends BaseServletTestSuite {
  private static final long DIRECTOR_ID = 10;
  private static final Date CREATED = new Date();
  private static final long USER_ID = 20;
  private static final long REACTABLE_ID = 7;
  private static final Date TIMESTAMP = new Date();
  private TheaterServlet theaterServlet = new TheaterServlet();

  @Test
  public void doGet() throws Exception {
    // Add a scene to datastore.
    saveScene((new Scene(DIRECTOR_ID, CREATED, null)));
    Scene scene = (Scene) Reactable.fromEntity(
        datastoreService.prepare(new Query(Reactable.DATASTORE_KIND)).asSingleEntity());
    // Sends the GET request
    theaterServlet.doGet(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
    }.getType());
    assertEquals(1, respondedReactables.size());
    assertEquals(scene, respondedReactables.get(0));
  }

  @Test
  public void doGet_multipleReactables() throws Exception {
    // Add 11 scenes to datastore.
    for (int i = 0; i < TheaterServlet.GET_LIMIT + 1; i++) {
      saveScene(new Scene(DIRECTOR_ID, new Date(CREATED.getTime() + i), null));
    }
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
      assertEquals(new Date(CREATED.getTime() + i), scene.getCreated());
      // Should have image url
      assertNotNull(scene.getImageSignedUrl());
    }
  }

  @Test
  public void doPost_viewEvent() throws Exception {
    ReactableEvent reactableEvent =
        new ReactableEvent(USER_ID, REACTABLE_ID, TIMESTAMP, EventType.REACTABLE_VIEW, null);
    // Mocks a ReactableEvent field.
    when(mockRequest.getReader()).thenReturn(
        new BufferedReader(new StringReader(Util.GSON.toJson(reactableEvent))));
    // Sends the POST request
    theaterServlet.doPost(mockRequest, mockResponse);
    // Retrieves the saves event from datastore.
    Entity savedEntity =
        datastoreService.prepare(new Query(ReactableEvent.DATASTORE_KIND)).asSingleEntity();
    assertEqualsForEntityAndReactableEvent(savedEntity, reactableEvent);
  }

  @Test
  public void doPost_reactionEvent() throws Exception {
    ReactableEvent reactableEvent =
        new ReactableEvent(USER_ID, REACTABLE_ID, TIMESTAMP, EventType.REACTABLE_REACTION,
            Emotion.HAPPY);
    // Mocks a ReactableEvent field.
    when(mockRequest.getReader()).thenReturn(
        new BufferedReader(new StringReader(Util.GSON.toJson(reactableEvent))));
    // Sends the POST request
    theaterServlet.doPost(mockRequest, mockResponse);
    // Retrieves the saves event from datastore.
    Entity savedEntity =
        datastoreService.prepare(new Query(ReactableEvent.DATASTORE_KIND)).asSingleEntity();
    assertEqualsForEntityAndReactableEvent(savedEntity, reactableEvent);
  }
}