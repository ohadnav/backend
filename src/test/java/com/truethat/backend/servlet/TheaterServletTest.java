package com.truethat.backend.servlet;

import com.google.gson.reflect.TypeToken;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Test;

import static com.truethat.backend.common.TestUtil.toBufferedReader;
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
  private TheaterServlet theaterServlet;

  @Override public void setUp() throws Exception {
    super.setUp();
    theaterServlet = new TheaterServlet();
    saveUser(director);
    saveUser(defaultUser);
    scene = new Scene(director.getId(), NOW, null);
  }

  @Test
  public void fetchReactables() throws Exception {
    prepareFetch();
    saveScene(scene);
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
    }.getType());
    assertEquals(1, respondedReactables.size());
    // Enriches scene
    ReactableEnricher.enrich(Collections.singletonList(scene), defaultUser);
    assertEquals(scene, respondedReactables.get(0));
  }

  @Test(expected = Exception.class)
  public void fetchReactables_missingUser() throws Exception {
    saveScene(scene);
    when(mockRequest.getReader()).thenReturn(null);
    theaterServlet.doPost(mockRequest, mockResponse);
  }

  @SuppressWarnings("Duplicates") @Test
  public void fetchReactables_emptyDatastore() throws Exception {
    // Not saving a scene.
    prepareFetch();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
        }.getType());
    assertTrue(respondedReactables.isEmpty());
  }

  @Test
  public void fetchReactables_multipleReactables() throws Exception {
    prepareFetch();
    // Add 11 scenes to datastore.
    for (int i = 0; i < TheaterServlet.FETCH_LIMIT + 1; i++) {
      saveScene(new Scene(director.getId(), new Date(NOW.getTime() + i), null));
    }
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> reactables = Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
    }.getType());
    // Asserts no more than TheaterServlet.FETCH_LIMIT are responded.
    assertEquals(TheaterServlet.FETCH_LIMIT, reactables.size());
    // Asserts the scenes are sorted by recency.
    for (int i = TheaterServlet.FETCH_LIMIT; i > 0; i--) {
      Scene scene = (Scene) reactables.get(TheaterServlet.FETCH_LIMIT - i);
      assertEquals(new Date(NOW.getTime() + i), scene.getCreated());
      // Should have image url
      assertNotNull(scene.getImageSignedUrl());
    }
  }

  private void prepareFetch() throws Exception {
    when(mockRequest.getReader()).thenReturn(toBufferedReader(Util.GSON.toJson(defaultUser)));
    resetResponseMock();
  }
}