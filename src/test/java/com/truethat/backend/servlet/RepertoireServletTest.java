package com.truethat.backend.servlet;

import com.google.cloud.Timestamp;
import com.google.gson.reflect.TypeToken;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Photo;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static com.truethat.backend.common.TestUtil.toBufferedReader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 03/07/2017.
 */
public class RepertoireServletTest extends BaseServletTestSuite {
  private Scene scene;
  private RepertoireServlet repertoireServlet;
  private User otherUser =
      new User(DEVICE_ID + "-2", FIRST_NAME, LAST_NAME, NOW);

  @Override public void setUp() throws Exception {
    super.setUp();
    repertoireServlet = new RepertoireServlet();
    repertoireServlet.setDatastore(datastore);
    saveUser(defaultUser);
    scene = new Scene(defaultUser, NOW, Collections.singletonList(new Photo("")), null);
  }

  @Test(expected = Exception.class)
  public void fetchScenes_missingUser() throws Exception {
    when(mockRequest.getReader()).thenReturn(null);
    repertoireServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = Exception.class)
  public void fetchScenes_userNotFound() throws Exception {
    datastore.delete(userKeyFactory.newKey(defaultUser.getId()));
    when(mockRequest.getReader()).thenReturn(null);
    repertoireServlet.doPost(mockRequest, mockResponse);
  }

  @Test
  public void fetchRepertoire() throws Exception {
    // Add a scene to datastore.
    saveScene(scene);
    // Sends the GET request
    prepareFetch();
    repertoireServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Scene> respondedScenes =
        Util.GSON.fromJson(response, new TypeToken<List<Scene>>() {
        }.getType());
    assertEquals(1, respondedScenes.size());
    // Enriches scene.
    enricher.enrichScenes(Collections.singletonList(scene), defaultUser);
    assertEquals(scene, respondedScenes.get(0));
  }

  @Test
  public void distinctDirectorFilter() throws Exception {
    saveUser(otherUser);
    // Add a scene from a different director
    scene.setDirector(otherUser);
    saveScene(scene);
    // Sends the GET request
    prepareFetch();
    repertoireServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Scene> respondedScenes =
        Util.GSON.fromJson(response, new TypeToken<List<Scene>>() {
        }.getType());
    assertEquals(0, respondedScenes.size());
  }

  @Test
  public void recencyFilter() throws Exception {
    // Add a scene from an old timestamp
    scene.setCreated(Timestamp.ofTimeSecondsAndNanos(Timestamp.now().getSeconds() - 86400 - 1, 0));
    saveScene(scene);
    // Sends the GET request
    prepareFetch();
    repertoireServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Scene> respondedScenes =
        Util.GSON.fromJson(response, new TypeToken<List<Scene>>() {
        }.getType());
    assertEquals(0, respondedScenes.size());
  }

  @SuppressWarnings("Duplicates") @Test
  public void fetchRepertoire_emptyRepertoire() throws Exception {
    // Sends the GET request
    prepareFetch();
    repertoireServlet.doPost(mockRequest, mockResponse);
  }

  @Test public void fetchRepertoire_multipleScenes() throws Exception {
    // Save scenes
    for (int i = 0; i < RepertoireServlet.FETCH_LIMIT + 1; i++) {
      saveScene(new Scene(defaultUser,
          Timestamp.ofTimeSecondsAndNanos(NOW.getSeconds() + i, NOW.getNanos()),
          Collections.singletonList(new Photo(
              "")),
          null));
    }
    prepareFetch();
    repertoireServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Scene> respondedScenes =
        Util.GSON.fromJson(response, new TypeToken<List<Scene>>() {
        }.getType());
    // Asserts no more than StudioServlet.FETCH_LIMIT are responded.
    assertEquals(RepertoireServlet.FETCH_LIMIT, respondedScenes.size());
    long recentTimestamp = respondedScenes.get(0).getCreated().getSeconds();
    // Asserts the scenes are sorted by recency.
    for (int i = 0; i < RepertoireServlet.FETCH_LIMIT; i++) {
      Scene scene = respondedScenes.get(i);
      assertEquals(recentTimestamp - i, scene.getCreated().getSeconds());
      // Should have image url
      assertNotNull(scene.getMediaNodes().get(0).getUrl());
    }
  }

  private void prepareFetch() throws Exception {
    when(mockRequest.getReader()).thenReturn(toBufferedReader(Util.GSON.toJson(defaultUser)));
    resetResponseMock();
  }
}