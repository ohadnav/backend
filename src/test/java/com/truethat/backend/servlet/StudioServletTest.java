package com.truethat.backend.servlet;

import com.google.gson.reflect.TypeToken;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 10/05/2017.
 */
public class StudioServletTest extends BaseServletTestSuite {
  private User director;
  private Scene scene;

  @Override public void setUp() throws Exception {
    super.setUp();
    director = new User(PHONE_NUMBER + "-2", DEVICE_ID + "-2", FIRST_NAME, LAST_NAME, NOW);
    saveUser(director);
    saveUser(defaultUser);
    scene = new Scene(director.getId(), NOW, null);
  }

  @Test(expected = ServletException.class)
  public void sceneNotSaved_noImage() throws Exception {
    prepareSceneSave(scene);
    when(mockRequest.getPart(Scene.IMAGE_PART)).thenReturn(null);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = ServletException.class)
  public void reactableNotSaved_noReactable() throws Exception {
    prepareSceneSave(scene);
    when(mockRequest.getPart(Reactable.REACTABLE_PART)).thenReturn(null);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = IOException.class)
  public void doRepertoire_missingUser() throws Exception {
    when(mockRequest.getParameter(StudioServlet.USER_PARAM)).thenReturn(null);
    studioServlet.doGet(mockRequest, mockResponse);
  }

  @Test
  public void getRepertoire() throws Exception {
    // Add a scene to datastore.
    saveScene(scene);
    // Sends the GET request
    prepareGet();
    studioServlet.doGet(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
        }.getType());
    assertEquals(1, respondedReactables.size());
    // Enriches scene.
    ReactableEnricher.enrich(Collections.singletonList(scene), defaultUser);
    assertEquals(scene, respondedReactables.get(0));
  }

  @SuppressWarnings("Duplicates") @Test
  public void getRepertoire_emptyRepertoire() throws Exception {
    // Sends the GET request
    prepareGet();
    studioServlet.doGet(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
        }.getType());
    assertTrue(respondedReactables.isEmpty());
  }

  @Test public void getRepertoire_multipleReactables() throws Exception {
    // Save reactables
    for (int i = 0; i < StudioServlet.GET_LIMIT + 1; i++) {
      saveScene(new Scene(director.getId(), new Date(NOW.getTime() + i), null));
    }
    prepareGet();
    studioServlet.doGet(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
    }.getType());
    // Asserts no more than StudioServlet.GET_LIMIT are responded.
    assertEquals(StudioServlet.GET_LIMIT, respondedReactables.size());
    // Asserts the reactables are sorted by recency.
    for (int i = StudioServlet.GET_LIMIT; i > 0; i--) {
      assertEquals(NOW.getTime() + i,
          respondedReactables.get(StudioServlet.GET_LIMIT - i).getCreated().getTime());
    }
  }

  private void prepareGet() throws Exception {
    when(mockRequest.getParameter(StudioServlet.USER_PARAM)).thenReturn(Util.GSON.toJson(director));
    resetResponseMock();
  }
}