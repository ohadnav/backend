package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.gson.reflect.TypeToken;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 10/05/2017.
 */
public class StudioServletTest extends BaseServletTestSuite {
  private static final long DIRECTOR_ID = 123L;
  private static final User USER = new User(DIRECTOR_ID);
  private static final Date CREATED = new Date();
  private static final Reactable REACTABLE = new Scene(DIRECTOR_ID, CREATED, null);

  @Test
  public void sceneNotSaved_noImage() throws Exception {
    prepareReactableSave(REACTABLE);
    when(mockRequest.getPart(Scene.IMAGE_PART)).thenReturn(null);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
    // No entity should have been saved.
    Iterator<Entity> entities =
        datastoreService.prepare(new Query(Reactable.DATASTORE_KIND)).asIterator();
    assertFalse(entities.hasNext());
  }

  @Test
  public void reactableNotSaved_noReactable() throws Exception {
    prepareReactableSave(REACTABLE);
    when(mockRequest.getPart(Reactable.REACTABLE_PART)).thenReturn(null);

    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
    // No entity should have been saved.
    Iterator<Entity> entities =
        datastoreService.prepare(new Query(Reactable.DATASTORE_KIND)).asIterator();
    assertFalse(entities.hasNext());
  }

  @Test
  public void doGet() throws Exception {
    // Add a scene to datastore.
    saveScene((new Scene(DIRECTOR_ID, CREATED, null)));
    Scene scene = (Scene) Reactable.fromEntity(
        datastoreService.prepare(new Query(Reactable.DATASTORE_KIND)).asSingleEntity());
    // Sends the GET request
    prepareGet();
    studioServlet.doGet(mockRequest, mockResponse);
    String response = responseWriter.toString();
    List<Reactable> respondedReactables =
        Util.GSON.fromJson(response, new TypeToken<List<Reactable>>() {
        }.getType());
    assertEquals(1, respondedReactables.size());
    assertEquals(scene, respondedReactables.get(0));
  }

  @Test public void getRepertoire() throws Exception {
    // Save reactables
    for (int i = 0; i < StudioServlet.GET_LIMIT + 1; i++) {
      saveScene(new Scene(DIRECTOR_ID, new Date(CREATED.getTime() + i), null));
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
      assertEquals(CREATED.getTime() + i,
          respondedReactables.get(StudioServlet.GET_LIMIT - i).getCreated().getTime());
    }
  }

  private void prepareGet() throws Exception {
    when(mockRequest.getParameter(StudioServlet.USER_PARAM)).thenReturn(Util.GSON.toJson(USER));
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
  }
}