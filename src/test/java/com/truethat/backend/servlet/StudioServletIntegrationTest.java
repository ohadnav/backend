package com.truethat.backend.servlet;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.google.common.collect.Lists;
import com.truethat.backend.common.TestUtil;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Media;
import com.truethat.backend.model.Photo;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import com.truethat.backend.model.Video;
import com.truethat.backend.storage.BaseStorageTestSuite;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.threeten.bp.Duration;

import static com.truethat.backend.common.TestUtil.toBufferedReader;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 28/06/2017.
 */
public class StudioServletIntegrationTest extends BaseStorageTestSuite {
  private static final LocalDatastoreHelper HELPER = LocalDatastoreHelper.create(1.0);

  @Mock
  private ServletConfig mockServletConfig;
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private Part mockFilePart;
  @Mock
  private Part mockScenePart;
  private StringWriter responseWriter;
  private StudioServlet studioServlet;
  private AuthServlet authServlet;
  private Datastore datastore;
  private User director;

  @BeforeClass
  public static void beforeClass() throws IOException, InterruptedException {
    HELPER.start();
  }

  @AfterClass
  public static void afterClass() throws IOException, InterruptedException, TimeoutException {
    HELPER.stop(Duration.ofMinutes(1));
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.initMocks(this);
    // Initialize datastore
    HELPER.reset();
    datastore = HELPER.getOptions().getService();
    // Initialize servlet
    studioServlet = new StudioServlet();
    authServlet = new AuthServlet();
    studioServlet.setDatastore(datastore);
    authServlet.setDatastore(datastore);
    studioServlet.init(mockServletConfig);
    studioServlet.setBucketName(bucketName);
    director = new User("my-iphone", "taylor", "swift", Timestamp.now());
  }

  @Test
  public void savePhoto() throws Exception {
    // Saves scene director to datastore.
    saveUser(director);
    initResponseMock();
    // Saves scene
    Scene scene =
        new Scene(director, Timestamp.now(), Collections.singletonList(new Photo("")), null);
    // Initializing request mock
    String fileName = "src/test/resources/servlet/1x1_pixel.jpg";
    when(mockFilePart.getContentType()).thenReturn("image/jpg");
    when(mockFilePart.getInputStream()).thenReturn(new FileInputStream(new File(fileName)));
    when(mockScenePart.getInputStream()).thenReturn(
        TestUtil.toInputStream(Util.GSON.toJson(scene)));
    when(mockRequest.getPart(Media.MEDIA_PART_PREFIX + "_0")).thenReturn(mockFilePart);
    when(mockRequest.getPart(Scene.SCENE_PART)).thenReturn(mockScenePart);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
    // Asserts that the scene was saved into the Datastore.
    Scene saved = Lists.newArrayList(datastore.run(
        Query.newEntityQueryBuilder().setKind(Scene.DATASTORE_KIND).build()))
        .stream()
        .map(Scene::new)
        .collect(toList())
        .get(0);
    // Asserts that the scene's image is saved, and matches the uploaded one.
    TestUtil.assertUrl(saved.getMediaItems().get(0).getUrl(), HttpURLConnection.HTTP_OK,
        new FileInputStream(new File(fileName)));
    scene.setDirector(null);
    scene.setDirectorId(director.getId());
    scene.setId(saved.getId());
    scene.getMediaItems().get(0).setUrl(saved.getMediaItems().get(0).getUrl());
    assertEquals(scene, saved);
  }

  @Test
  public void saveVideo() throws Exception {
    saveUser(director);
    initResponseMock();
    // Saves scene
    Scene scene =
        new Scene(director, Timestamp.now(), Collections.singletonList(new Video("")), null);
    // Initializing request mock
    String fileName = "src/test/resources/servlet/wink.mp4";
    when(mockFilePart.getContentType()).thenReturn("video/mp4");
    when(mockFilePart.getInputStream()).thenReturn(new FileInputStream(new File(fileName)));
    when(mockScenePart.getInputStream()).thenReturn(
        TestUtil.toInputStream(Util.GSON.toJson(scene)));
    when(mockRequest.getPart(Media.MEDIA_PART_PREFIX + "_0")).thenReturn(mockFilePart);
    when(mockRequest.getPart(Scene.SCENE_PART)).thenReturn(mockScenePart);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
    // Asserts that the scene was saved into the Datastore.
    Scene saved = Lists.newArrayList(datastore.run(
        Query.newEntityQueryBuilder().setKind(Scene.DATASTORE_KIND).build()))
        .stream()
        .map(Scene::new)
        .collect(toList())
        .get(0);
    scene.setDirector(null);
    scene.setDirectorId(director.getId());
    scene.setId(saved.getId());
    scene.getMediaItems().get(0).setUrl(saved.getMediaItems().get(0).getUrl());
    assertEquals(scene, saved);
    // Asserts that the video is saved. We dont assert the uploaded file matches the
    // original one, as it streamed to the client, and so cannot be fully matched.
    TestUtil.assertUrl(saved.getMediaItems().get(0).getUrl(), HttpURLConnection.HTTP_OK, null);
  }

  private void initResponseMock() throws Exception {
    // Resets response mock.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
  }

  /**
   * Saves user to datastore.
   *
   * @param user to save
   */
  private void saveUser(User user) throws Exception {
    initResponseMock();
    // Mocks a request body with user.
    when(mockRequest.getReader()).thenReturn(toBufferedReader(Util.GSON.toJson(user)));
    // Sends the POST request
    authServlet.doPost(mockRequest, mockResponse);
    // Updates the user id.
    User response = Util.GSON.fromJson(responseWriter.toString(), User.class);
    user.setId(response.getId());
  }
}
