package com.truethat.backend.servlet;

import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
import javax.servlet.ServletException;
import org.junit.Test;

import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 10/05/2017.
 */
public class StudioServletTest extends BaseServletTestSuite {
  private Scene scene;

  @Override public void setUp() throws Exception {
    super.setUp();
    saveUser(defaultUser);
    scene = new Scene(defaultUser.getId(), NOW, null);
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
}