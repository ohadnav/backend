package com.truethat.backend.servlet;

import com.google.cloud.datastore.Query;
import com.truethat.backend.model.Pose;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Short;
import javax.servlet.ServletException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 10/05/2017.
 */
public class StudioServletTest extends BaseServletTestSuite {
  private Pose pose;
  private Short aShort;

  @Override public void setUp() throws Exception {
    super.setUp();
    saveUser(defaultUser);
    pose = new Pose(defaultUser, NOW, null);
    aShort = new Short(defaultUser, NOW, null);
  }

  @Test
  public void poseSaved() throws Exception {
    savePose(pose);
    Pose savedPose = (Pose) Reactable.fromEntity(
        datastore.run(Query.newEntityQueryBuilder().setKind(Reactable.DATASTORE_KIND).build())
            .next());
    pose.setDirector(null);
    pose.setDirectorId(defaultUser.getId());
    assertEquals(pose, savedPose);
  }

  @Test
  public void shortSaved() throws Exception {
    saveShort(aShort);
    Short savedShort = (Short) Reactable.fromEntity(
        datastore.run(Query.newEntityQueryBuilder().setKind(Reactable.DATASTORE_KIND).build())
            .next());
    aShort.setDirector(null);
    aShort.setDirectorId(defaultUser.getId());
    assertEquals(aShort, savedShort);
  }

  @Test(expected = ServletException.class)
  public void poseNotSaved_noImage() throws Exception {
    preparePoseSave(pose);
    when(mockRequest.getPart(Pose.IMAGE_PART)).thenReturn(null);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = ServletException.class)
  public void reactableNotSaved_noReactable() throws Exception {
    preparePoseSave(pose);
    when(mockRequest.getPart(Reactable.REACTABLE_PART)).thenReturn(null);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = ServletException.class)
  public void reactableNotSaved_missingDirector() throws Exception {
    pose.setDirector(null);
    preparePoseSave(pose);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = ServletException.class)
  public void reactableNotSaved_missingDirectorId() throws Exception {
    pose.getDirector().setId(null);
    preparePoseSave(pose);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = ServletException.class)
  public void reactableNotSaved_directorNotFound() throws Exception {
    emptyDatastore(null);
    preparePoseSave(pose);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = ServletException.class)
  public void reactableNotSaved_missingCreated() throws Exception {
    pose.setCreated(null);
    preparePoseSave(pose);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }
}