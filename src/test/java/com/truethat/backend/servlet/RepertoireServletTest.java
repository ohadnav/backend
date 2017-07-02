package com.truethat.backend.servlet;

import com.google.gson.reflect.TypeToken;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Test;

import static com.truethat.backend.common.TestUtil.toBufferedReader;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 03/07/2017.
 */
public class RepertoireServletTest extends BaseServletTestSuite {
  private Scene scene;
  private RepertoireServlet repertoireServlet;

  @Override public void setUp() throws Exception {
    super.setUp();
    repertoireServlet = new RepertoireServlet();
    saveUser(defaultUser);
    scene = new Scene(defaultUser.getId(), NOW, null);
  }

  @Test(expected = Exception.class)
  public void fetchRepertoire_missingUser() throws Exception {
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
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
        }.getType());
    assertEquals(1, respondedReactables.size());
    // Enriches scene.
    ReactableEnricher.enrich(Collections.singletonList(scene), defaultUser);
    assertEquals(scene, respondedReactables.get(0));
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
      saveScene(new Scene(defaultUser.getId(), new Date(NOW.getTime() + i), null));
    }
    prepareFetch();
    repertoireServlet.doPost(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
        }.getType());
    // Asserts no more than StudioServlet.FETCH_LIMIT are responded.
    assertEquals(RepertoireServlet.FETCH_LIMIT, respondedReactables.size());
    // Asserts the reactables are sorted by recency.
    for (int i = RepertoireServlet.FETCH_LIMIT; i > 0; i--) {
      assertEquals(NOW.getTime() + i,
          respondedReactables.get(RepertoireServlet.FETCH_LIMIT - i).getCreated().getTime());
    }
  }

  private void prepareFetch() throws Exception {
    when(mockRequest.getReader()).thenReturn(toBufferedReader(Util.GSON.toJson(defaultUser)));
    resetResponseMock();
  }
}