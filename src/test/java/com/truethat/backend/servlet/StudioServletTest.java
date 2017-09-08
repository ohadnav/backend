package com.truethat.backend.servlet;

import com.google.cloud.datastore.Query;
import com.truethat.backend.model.Media;
import com.truethat.backend.model.Photo;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.Video;
import javax.servlet.ServletException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 10/05/2017.
 */
public class StudioServletTest extends BaseServletTestSuite {
  private Scene scene;

  @Override public void setUp() throws Exception {
    super.setUp();
    saveUser(defaultUser);
    scene = new Scene(defaultUser, NOW, new Photo(""));
  }

  @Test
  public void poseSaved() throws Exception {
    saveScene(scene);
    Scene saved = new Scene(
        datastore.run(Query.newEntityQueryBuilder().setKind(Scene.DATASTORE_KIND).build())
            .next());
    scene.setDirector(null);
    scene.setDirectorId(defaultUser.getId());
    assertEquals(scene, saved);
  }

  @Test
  public void shortSaved() throws Exception {
    scene = new Scene(defaultUser, NOW, new Video(""));
    saveScene(scene);
    Scene saved = new Scene(
        datastore.run(Query.newEntityQueryBuilder().setKind(Scene.DATASTORE_KIND).build())
            .next());
    scene.setDirector(null);
    scene.setDirectorId(defaultUser.getId());
    assertEquals(scene, saved);
  }

  @Test(expected = ServletException.class)
  public void poseNotSaved_noImage() throws Exception {
    prepareSceneSave(scene);
    when(mockRequest.getPart(Media.MEDIA_PART)).thenReturn(null);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = ServletException.class)
  public void sceneNotSaved_noScene() throws Exception {
    prepareSceneSave(scene);
    when(mockRequest.getPart(Scene.SCENE_PART)).thenReturn(null);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = ServletException.class)
  public void sceneNotSaved_missingDirector() throws Exception {
    scene.setDirector(null);
    prepareSceneSave(scene);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = ServletException.class)
  public void sceneNotSaved_missingDirectorId() throws Exception {
    scene.getDirector().setId(null);
    prepareSceneSave(scene);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = ServletException.class)
  public void sceneNotSaved_directorNotFound() throws Exception {
    emptyDatastore(null);
    prepareSceneSave(scene);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = ServletException.class)
  public void sceneNotSaved_missingCreated() throws Exception {
    scene.setCreated(null);
    prepareSceneSave(scene);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }
}