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
    scene = new Scene(defaultUser, NOW, Collections.singletonList(new Photo(0L, "")), null);
  }

  @Test
  public void photoSaved() throws Exception {
    saveScene(scene);
    Scene saved = new Scene(
        datastore.run(Query.newEntityQueryBuilder().setKind(Scene.KIND).build())
            .next());
    scene.setDirector(null);
    scene.setDirectorId(defaultUser.getId());
    assertEquals(scene, saved);
  }

  @Test
  public void videoSaved() throws Exception {
    scene = new Scene(defaultUser, NOW, Collections.singletonList(new Video(0L, "")), null);
    saveScene(scene);
    Scene saved = new Scene(
        datastore.run(Query.newEntityQueryBuilder().setKind(Scene.KIND).build())
            .next());
    scene.setDirector(null);
    scene.setDirectorId(defaultUser.getId());
    assertEquals(scene, saved);
  }

  @Test
  public void saveTree() throws Exception {
    scene = new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(0L, ""), new Photo(1L, ""), new Video(2L, "")),
        Arrays.asList(new Edge(0L, 1L, Emotion.HAPPY),
            new Edge(0L, 2L, Emotion.FEAR)));
    saveScene(scene);
    Scene saved = new Scene(
        datastore.run(Query.newEntityQueryBuilder().setKind(Scene.KIND).build())
            .next());
    scene.setDirector(null);
    scene.setDirectorId(defaultUser.getId());
    assertEquals(scene, saved);
  }

  @Test(expected = IOException.class)
  public void saveTreeInvalid_targetIdHasNoMatchingMedia() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(10L, ""), new Photo(20L, "")),
        Collections.singletonList(new Edge(10L, 1L, Emotion.HAPPY))));
  }

  @Test(expected = IOException.class)
  public void saveTreeInvalid_sourceIdHasNoMatchingMedia() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(10L, ""), new Photo(20L, "")),
        Collections.singletonList(new Edge(-1L, 1L, Emotion.HAPPY))));
  }

  @Test(expected = IOException.class)
  public void saveTreeInvalid_missingEdges() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(0L, ""), new Photo(1L, "")), null));
  }

  @Test(expected = IOException.class)
  public void saveTreeInvalid_invalidFlowTree() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(0L, ""), new Photo(1L, ""), new Video(2L, "")),
        Collections.singletonList(new Edge(0L, 1L, Emotion.HAPPY))));
  }

  @Test(expected = IOException.class)
  public void saveTreeInvalid_missingSourceIndex() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(0L, ""), new Photo(1L, ""), new Video(2L, "")),
        Arrays.asList(new Edge(0L, 1L, Emotion.HAPPY),
            new Edge(0L, 2L, Emotion.FEAR),
            new Edge(null, 2L, Emotion.FEAR))));
  }

  @Test(expected = IOException.class)
  public void saveTreeInvalid_missingTargetIndex() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(0L, ""), new Photo(1L, ""), new Video(2L, "")),
        Arrays.asList(new Edge(0L, 1L, Emotion.HAPPY),
            new Edge(0L, 2L, Emotion.FEAR),
            new Edge(0L, null, Emotion.FEAR))));
  }

  @Test(expected = IOException.class)
  public void saveTreeInvalid_missingEdgeReaction() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(0L, ""), new Photo(1L, ""), new Video(2L, "")),
        Arrays.asList(new Edge(0L, 1L, Emotion.HAPPY),
            new Edge(0L, 2L, Emotion.FEAR),
            new Edge(1L, 2L, null))));
  }

  @Test(expected = IOException.class)
  public void saveTreeInvalid_edgeTargetIndexEqualsSource() throws Exception {
    saveScene(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(0L, ""), new Photo(1L, ""), new Video(2L, "")),
        Collections.singletonList(new Edge(0L, 0L, Emotion.HAPPY))));
  }

  @Test(expected = IOException.class)
  public void sceneNotSaved_missingMediaPart() throws Exception {
    prepareSceneSave(new Scene(defaultUser, NOW,
        Arrays.asList(new Photo(0L, ""), new Photo(1L, "")), null));
    when(mockRequest.getPart(Media.MEDIA_PART_PREFIX) + "1").thenReturn(null);
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