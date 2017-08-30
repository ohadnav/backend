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
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 03/07/2017.
 */
public class RepertoireServletTest extends BaseServletTestSuite {
  private Pose pose;
  private RepertoireServlet repertoireServlet;
  private User otherUser =
      new User(DEVICE_ID + "-2", FIRST_NAME, LAST_NAME, NOW);

  @Override public void setUp() throws Exception {
    super.setUp();
    repertoireServlet = new RepertoireServlet();
    repertoireServlet.setDatastore(datastore);
    saveUser(defaultUser);
    pose = new Pose(defaultUser, NOW, null);
  }

  @Test(expected = Exception.class)
  public void fetchReactables_missingUser() throws Exception {
    when(mockRequest.getReader()).thenReturn(null);
    repertoireServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = Exception.class)
  public void fetchReactables_userNotFound() throws Exception {
    datastore.delete(userKeyFactory.newKey(defaultUser.getId()));
    when(mockRequest.getReader()).thenReturn(null);
    repertoireServlet.doPost(mockRequest, mockResponse);
  }

  @Test
  public void fetchRepertoire() throws Exception {
    // Add a pose to datastore.
    savePose(pose);
    // Sends the GET request
    prepareFetch();
    repertoireServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
        }.getType());
    assertEquals(1, respondedReactables.size());
    // Enriches pose.
    enricher.enrichReactables(Collections.singletonList(pose), defaultUser);
    assertEquals(pose, respondedReactables.get(0));
  }

  @Test
  public void distinctDirectorFilter() throws Exception {
    saveUser(otherUser);
    // Add a pose from a different director
    pose.setDirector(otherUser);
    savePose(pose);
    // Sends the GET request
    prepareFetch();
    repertoireServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
        }.getType());
    assertEquals(0, respondedReactables.size());
  }

  @Test
  public void recencyFilter() throws Exception {
    // Add a pose from an old timestamp
    pose.setCreated(Timestamp.ofTimeSecondsAndNanos(Timestamp.now().getSeconds() - 86400 - 1, 0));
    savePose(pose);
    // Sends the GET request
    prepareFetch();
    repertoireServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
        }.getType());
    assertEquals(0, respondedReactables.size());
  }

  @SuppressWarnings("Duplicates") @Test
  public void fetchRepertoire_emptyRepertoire() throws Exception {
    // Sends the GET request
    prepareFetch();
    repertoireServlet.doPost(mockRequest, mockResponse);
  }

  @Test public void fetchRepertoire_multipleReactables() throws Exception {
    // Save reactables
    for (int i = 0; i < RepertoireServlet.FETCH_LIMIT + 1; i++) {
      savePose(new Pose(defaultUser,
          Timestamp.ofTimeSecondsAndNanos(NOW.getSeconds() + i, NOW.getNanos()), null));
    }
    prepareFetch();
    repertoireServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
        }.getType());
    // Asserts no more than StudioServlet.FETCH_LIMIT are responded.
    assertEquals(RepertoireServlet.FETCH_LIMIT, respondedReactables.size());
    long recentTimestamp = respondedReactables.get(0).getCreated().getSeconds();
    // Asserts the reactables are sorted by recency.
    for (int i = 0; i < RepertoireServlet.FETCH_LIMIT; i++) {
      Pose pose = (Pose) respondedReactables.get(i);
      assertEquals(recentTimestamp - i, pose.getCreated().getSeconds());
      // Should have image url
      assertNotNull(pose.getImageUrl());
    }
  }

  private void prepareFetch() throws Exception {
    when(mockRequest.getReader()).thenReturn(toBufferedReader(Util.GSON.toJson(defaultUser)));
    resetResponseMock();
  }
}