package com.truethat.backend.servlet;

import com.google.cloud.datastore.Query;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
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
    scene = new Scene(defaultUser, NOW, null);
  }

  @Test
  public void sceneSaved() throws Exception {
    saveScene(scene);
    Scene savedScene = (Scene) Reactable.fromEntity(
        datastore.run(Query.newEntityQueryBuilder().setKind(Reactable.DATASTORE_KIND).build())
            .next());
    scene.setDirector(null);
    scene.setDirectorId(defaultUser.getId());
    assertEquals(scene, savedScene);
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