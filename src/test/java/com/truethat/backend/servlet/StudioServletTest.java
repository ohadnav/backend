package com.truethat.backend.servlet;

import com.google.cloud.datastore.Query;
import com.truethat.backend.model.Edge;
import com.truethat.backend.model.Emotion;
import com.truethat.backend.model.Media;
import com.truethat.backend.model.Photo;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.Video;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
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
    scene = new Scene(defaultUser, NOW, Collections.singletonList(new Photo("")), null);
  }

  @Test
  public void photoSaved() throws Exception {
    saveScene(scene);
    Scene saved = new Scene(
        datastore.run(Query.newEntityQueryBuilder().setKind(Scene.DATASTORE_KIND).build())
            .next());
    scene.setDirector(null);
    scene.setDirectorId(defaultUser.getId());
    assertEquals(scene, saved);
  }

  @Test
  public void videoSaved() throws Exception {
    scene = new Scene(defaultUser, NOW, Collections.singletonList(new Video("")), null);
    saveScene(scene);
    Scene saved = new Scene(
        datastore.run(Query.newEntityQueryBuilder().setKind(Scene.DATASTORE_KIND).build())
            .next());
    scene.setDirector(null);
    scene.setDirectorId(defaultUser.getId());
    assertEquals(scene, saved);
  }

  @Test
  public void saveGraph() throws Exception {
    scene = new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(""), new Photo(""), new Video("")),
        Arrays.asList(new Edge(0L, 1L, Emotion.HAPPY),
            new Edge(0L, 2L, Emotion.SAD)));
    saveScene(scene);
    Scene saved = new Scene(
        datastore.run(Query.newEntityQueryBuilder().setKind(Scene.DATASTORE_KIND).build())
            .next());
    scene.setDirector(null);
    scene.setDirectorId(defaultUser.getId());
    assertEquals(scene, saved);
  }

  @Test(expected = IOException.class)
  public void saveGraphInvalid_outOfRangeTargetIndex() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(""), new Photo("")),
        Collections.singletonList(new Edge(0L, 3L, Emotion.HAPPY))));
  }

  @Test(expected = IOException.class)
  public void saveGraphInvalid_outOfRangeSourceIndex() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(""), new Photo("")),
        Collections.singletonList(new Edge(-1L, 1L, Emotion.HAPPY))));
  }

  @Test(expected = IOException.class)
  public void saveGraphInvalid_missingEdges() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(""), new Photo("")), null));
  }

  @Test(expected = IOException.class)
  public void saveGraphInvalid_unreachableMedia() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(""), new Photo(""), new Video("")),
        Collections.singletonList(new Edge(0L, 1L, Emotion.HAPPY))));
  }

  @Test(expected = IOException.class)
  public void saveGraphInvalid_edgeTargetIndexIsSmaller() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(""), new Photo(""), new Video("")),
        Collections.singletonList(new Edge(1L, 0L, Emotion.HAPPY))));
  }

  @Test(expected = IOException.class)
  public void saveGraphInvalid_missingSourceIndex() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(""), new Photo(""), new Video("")),
        Arrays.asList(new Edge(0L, 1L, Emotion.HAPPY),
            new Edge(0L, 2L, Emotion.SAD),
            new Edge(null, 2L, Emotion.SAD))));
  }

  @Test(expected = IOException.class)
  public void saveGraphInvalid_missingTargetIndex() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(""), new Photo(""), new Video("")),
        Arrays.asList(new Edge(0L, 1L, Emotion.HAPPY),
            new Edge(0L, 2L, Emotion.SAD),
            new Edge(0L, null, Emotion.SAD))));
  }

  @Test(expected = IOException.class)
  public void saveGraphInvalid_missingEdgeReaction() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(""), new Photo(""), new Video("")),
        Arrays.asList(new Edge(0L, 1L, Emotion.HAPPY),
            new Edge(0L, 2L, Emotion.SAD),
            new Edge(1L, 2L, null))));
  }

  @Test(expected = IOException.class)
  public void saveGraphInvalid_edgeTargetIndexEqualsSource() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(""), new Photo(""), new Video("")),
        Collections.singletonList(new Edge(0L, 0L, Emotion.HAPPY))));
  }

  @Test(expected = IOException.class)
  public void sceneNotSaved_missingMediaPart() throws Exception {
    prepareSceneSave(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(""), new Photo("")), null));
    when(mockRequest.getPart(Media.MEDIA_PART_PREFIX) + "_1").thenReturn(null);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = IOException.class)
  public void sceneNotSaved_noScene() throws Exception {
    prepareSceneSave(scene);
    when(mockRequest.getPart(Scene.SCENE_PART)).thenReturn(null);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = IOException.class)
  public void sceneNotSaved_missingDirector() throws Exception {
    scene.setDirector(null);
    prepareSceneSave(scene);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = IOException.class)
  public void sceneNotSaved_missingDirectorId() throws Exception {
    scene.getDirector().setId(null);
    prepareSceneSave(scene);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = IOException.class)
  public void sceneNotSaved_directorNotFound() throws Exception {
    emptyDatastore(null);
    prepareSceneSave(scene);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = IOException.class)
  public void sceneNotSaved_missingCreated() throws Exception {
    scene.setCreated(null);
    prepareSceneSave(scene);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
  }
}