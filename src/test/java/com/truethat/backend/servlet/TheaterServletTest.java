package com.truethat.backend.servlet;

import com.google.cloud.Timestamp;
import com.google.gson.reflect.TypeToken;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Pose;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.User;
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
      new User(DEVICE_ID + "-2", FIRST_NAME, LAST_NAME, NOW);
  private Pose pose;
  private TheaterServlet theaterServlet;

  @Override public void setUp() throws Exception {
    super.setUp();
    theaterServlet = new TheaterServlet();
    theaterServlet.setDatastore(datastore);
    saveUser(director);
    saveUser(defaultUser);
    pose = new Pose(director, NOW, null);
  }

  @Test
  public void fetchReactables() throws Exception {
    prepareFetch();
    savePose(pose);
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
        }.getType());
    assertEquals(1, respondedReactables.size());
    // Enriches pose
    enricher.enrichReactables(Collections.singletonList(pose), defaultUser);
    assertEquals(pose, respondedReactables.get(0));
  }

  @Test
  public void dontFetchOwnReactables() throws Exception {
    prepareFetch();
    savePose(pose);
    when(mockRequest.getReader()).thenReturn(toBufferedReader(Util.GSON.toJson(director)));
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables = Util.GSON.fromJson(response,
        new TypeToken<List<Reactable>>() {
        }.getType());
    assertTrue(respondedReactables.isEmpty());
  }

  @Test
  public void validateReactables() throws Exception {
    prepareFetch();
    savePose(pose);
    datastore.delete(userKeyFactory.newKey(director.getId()));
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
        }.getType());
    assertEquals(0, respondedReactables.size());
  }

  @Test(expected = Exception.class)
  public void fetchReactables_missingUser() throws Exception {
    savePose(pose);
    when(mockRequest.getReader()).thenReturn(null);
    theaterServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = Exception.class)
  public void fetchReactables_userNotFound() throws Exception {
    savePose(pose);
    datastore.delete(userKeyFactory.newKey(defaultUser.getId()));
    when(mockRequest.getReader()).thenReturn(null);
    theaterServlet.doPost(mockRequest, mockResponse);
  }

  @SuppressWarnings("Duplicates") @Test
  public void fetchReactables_emptyDatastore() throws Exception {
    // Not saving a pose.
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
    // Add 11 poses to datastore.
    for (int i = 0; i < TheaterServlet.FETCH_LIMIT + 1; i++) {
      savePose(new Pose(director,
          Timestamp.ofTimeSecondsAndNanos(NOW.getSeconds() + i, NOW.getNanos()), null));
    }
    resetResponseMock();
    // Sends the GET request
    theaterServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
    }.getType());
    // Asserts no more than TheaterServlet.FETCH_LIMIT are responded.
    assertEquals(TheaterServlet.FETCH_LIMIT, respondedReactables.size());
    long recentTimestamp = respondedReactables.get(0).getCreated().getSeconds();
    // Asserts the poses are sorted by recency.
    for (int i = 0; i < TheaterServlet.FETCH_LIMIT; i++) {
      Pose pose = (Pose) respondedReactables.get(i);
      assertEquals(recentTimestamp - i, pose.getCreated().getSeconds());
      // Should have image url
      assertNotNull(pose.getImageSignedUrl());
    }
  }

  private void prepareFetch() throws Exception {
    when(mockRequest.getReader()).thenReturn(toBufferedReader(Util.GSON.toJson(defaultUser)));
    resetResponseMock();
  }
}