package com.truethat.backend.servlet;

import com.google.cloud.datastore.Query;
import com.google.common.collect.Lists;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
import javax.servlet.ServletException;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 10/05/2017.
 */
public class StudioServletTest extends BaseServletTestSuite {
  private Scene scene;

  @Override public void setUp() throws Exception {
    super.setUp();
    saveUser(defaultUser);
    scene = new Scene(defaultUser, NOW, null);
  }

  @Test(expected = ServletException.class)
  public void sceneSaved() throws Exception {
    prepareSceneSave(scene);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
    // Asserts that the reactable was saved into the Datastore.
    Scene savedScene = (Scene) Lists.newArrayList(datastore.run(
        Query.newEntityQueryBuilder().setKind(Reactable.DATASTORE_KIND).build()))
        .stream()
        .map(Reactable::fromEntity)
        .collect(toList())
        .get(0);
    // Asserts that the scene's image is saved, and matches the uploaded one.
    assertEquals(scene, savedScene);
    assertNotNull(savedScene.getImageSignedUrl());
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

  @Test(expected = ServletException.class)
  public void reactableNotSaved_missingDirector() throws Exception {
    scene.setDirector(null);
    prepareSceneSave(scene);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = ServletException.class)
  public void reactableNotSaved_missingDirectorId() throws Exception {
    scene.getDirector().setId(null);
    prepareSceneSave(scene);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = ServletException.class)
  public void reactableNotSaved_directorNotFound() throws Exception {
    emptyDatastore(null);
    prepareSceneSave(scene);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = ServletException.class)
  public void reactableNotSaved_missingCreated() throws Exception {
    scene.setCreated(null);
    prepareSceneSave(scene);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }
}