package com.truethat.backend.servlet;

import com.google.cloud.Timestamp;
import com.google.gson.reflect.TypeToken;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Photo;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import com.truethat.backend.model.Video;
import java.util.Arrays;
import java.util.Collections;
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
      new User(DEVICE_ID + "-2", PHONE_NUMBER + "2", FIRST_NAME, LAST_NAME, NOW);
  private Scene scene;
  private TheaterServlet theaterServlet;

  @Override public void setUp() throws Exception {
    super.setUp();
    theaterServlet = new TheaterServlet();
    theaterServlet.setDatastore(datastore);
    saveUser(director);
    saveUser(defaultUser);
    scene = new Scene(director, NOW, Collections.singletonList(new Photo(0L, "")), null);
  }

  @Test
  public void fetchScenes() throws Exception {
    prepareFetch();
    saveScene(scene);
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Scene> respondedScenes =
        Util.GSON.fromJson(response, new TypeToken<List<Scene>>() {
        }.getType());
    assertEquals(1, respondedScenes.size());
    // Enriches scene
    enricher.enrichScenes(Collections.singletonList(scene), defaultUser);
    assertEquals(scene, respondedScenes.get(0));
  }

  @Test
  public void timeLimitFilter() throws Exception {
    prepareFetch();
    scene.setCreated(Timestamp.ofTimeSecondsAndNanos(Timestamp.now().getSeconds() - 86400 - 1, 0));
    saveScene(scene);
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Scene> respondedScenes =
        Util.GSON.fromJson(response, new TypeToken<List<Scene>>() {
        }.getType());
    assertEquals(0, respondedScenes.size());
  }

  @Test
  public void sameUserFilter() throws Exception {
    prepareFetch();
    scene.setDirector(defaultUser);
    saveScene(scene);
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Scene> respondedScenes =
        Util.GSON.fromJson(response, new TypeToken<List<Scene>>() {
        }.getType());
    assertEquals(0, respondedScenes.size());
  }

  @Test
  public void fetchMultipleTypes() throws Exception {
    prepareFetch();
    saveScene(scene);
    Scene videoScene = new Scene(director, NOW, Collections.singletonList(new Video(0L, "")), null);
    saveScene(videoScene);
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Scene> respondedScenes =
        Util.GSON.fromJson(response, new TypeToken<List<Scene>>() {
        }.getType());
    assertEquals(2, respondedScenes.size());
    // Enriches scenes
    enricher.enrichScenes(Arrays.asList(scene, videoScene), defaultUser);
    assertEquals(scene, respondedScenes.get(0));
    assertEquals(videoScene, respondedScenes.get(1));
  }

  @Test
  public void dontFetchOwnScenes() throws Exception {
    prepareFetch();
    saveScene(scene);
    when(mockRequest.getReader()).thenReturn(toBufferedReader(Util.GSON.toJson(director)));
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Scene> respondedScenes = Util.GSON.fromJson(response,
        new TypeToken<List<Scene>>() {
        }.getType());
    assertTrue(respondedScenes.isEmpty());
  }

  @Test
  public void validateScenes() throws Exception {
    prepareFetch();
    saveScene(scene);
    datastore.delete(userKeyFactory.newKey(director.getId()));
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Scene> respondedScenes =
        Util.GSON.fromJson(response, new TypeToken<List<Scene>>() {
        }.getType());
    assertEquals(0, respondedScenes.size());
  }

  @Test(expected = Exception.class)
  public void fetchScenes_missingUser() throws Exception {
    saveScene(scene);
    when(mockRequest.getReader()).thenReturn(null);
    theaterServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = Exception.class)
  public void fetchScenes_userNotFound() throws Exception {
    saveScene(scene);
    datastore.delete(userKeyFactory.newKey(defaultUser.getId()));
    when(mockRequest.getReader()).thenReturn(null);
    theaterServlet.doPost(mockRequest, mockResponse);
  }

  @SuppressWarnings("Duplicates") @Test
  public void fetchScenes_emptyDatastore() throws Exception {
    // Not saving a scene.
    prepareFetch();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Scene> respondedScenes =
        Util.GSON.fromJson(response, new TypeToken<List<Scene>>() {
        }.getType());
    assertTrue(respondedScenes.isEmpty());
  }

  @Test
  public void fetchScenes_multipleScenes() throws Exception {
    prepareFetch();
    // Add 11 scenes to datastore.
    for (int i = 0; i < TheaterServlet.FETCH_LIMIT + 1; i++) {
      saveScene(new Scene(director,
          Timestamp.ofTimeSecondsAndNanos(NOW.getSeconds() + i, NOW.getNanos()),
          Collections.singletonList(new Photo(0L, "")),
          null));
    }
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Scene> respondedScenes =
        Util.GSON.fromJson(response, new TypeToken<List<Scene>>() {
        }.getType());
    // Asserts no more than TheaterServlet.FETCH_LIMIT are responded.
    assertEquals(TheaterServlet.FETCH_LIMIT, respondedScenes.size());
    long recentTimestamp = respondedScenes.get(0).getCreated().getSeconds();
    // Asserts the scenes are sorted by recency.
    for (int i = 0; i < TheaterServlet.FETCH_LIMIT; i++) {
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