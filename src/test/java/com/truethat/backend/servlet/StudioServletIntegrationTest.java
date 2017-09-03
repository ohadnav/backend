package com.truethat.backend.servlet;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.google.common.collect.Lists;
import com.truethat.backend.common.TestUtil;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Pose;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Short;
import com.truethat.backend.model.User;
import com.truethat.backend.storage.BaseStorageTestSuite;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
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
  private Part mockReactablePart;
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
  public void savePose() throws Exception {
    // Saves pose director to datastore.
    saveUser(director);
    initResponseMock();
    // Saves pose
    Pose pose = new Pose(director, Timestamp.now(), null);
    // Initializing request mock
    String fileName = "src/test/resources/servlet/1x1_pixel.jpg";
    when(mockFilePart.getContentType()).thenReturn("image/jpeg");
    when(mockFilePart.getInputStream()).thenReturn(new FileInputStream(new File(fileName)));
    when(mockReactablePart.getInputStream()).thenReturn(
        TestUtil.toInputStream(Util.GSON.toJson(pose)));
    when(mockRequest.getPart(Pose.IMAGE_PART)).thenReturn(mockFilePart);
    when(mockRequest.getPart(Reactable.REACTABLE_PART)).thenReturn(mockReactablePart);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
    // Asserts that the reactable was saved into the Datastore.
    Pose savedPose = (Pose) Lists.newArrayList(datastore.run(
        Query.newEntityQueryBuilder().setKind(Reactable.DATASTORE_KIND).build()))
        .stream()
        .map(Reactable::fromEntity)
        .collect(toList())
        .get(0);
    // Asserts that the pose's image is saved, and matches the uploaded one.
    TestUtil.assertUrl(savedPose.getImageUrl(), HttpURLConnection.HTTP_OK,
        new FileInputStream(new File(fileName)));
    pose.setDirector(null);
    pose.setDirectorId(director.getId());
    pose.setId(savedPose.getId());
    pose.setImageUrl(savedPose.getImageUrl());
    assertEquals(pose, savedPose);
  }

  @Test
  public void saveShort() throws Exception {
    saveUser(director);
    initResponseMock();
    // Saves pose
    Short aShort = new Short(director, Timestamp.now(), null);
    // Initializing request mock
    String fileName = "src/test/resources/servlet/wink.mp4";
    when(mockFilePart.getContentType()).thenReturn("video/mp4");
    when(mockFilePart.getInputStream()).thenReturn(new FileInputStream(new File(fileName)));
    when(mockReactablePart.getInputStream()).thenReturn(
        TestUtil.toInputStream(Util.GSON.toJson(aShort)));
    when(mockRequest.getPart(Short.VIDEO_PART)).thenReturn(mockFilePart);
    when(mockRequest.getPart(Reactable.REACTABLE_PART)).thenReturn(mockReactablePart);
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
    // Asserts that the reactable was saved into the Datastore.
    Short savedShort = (Short) Lists.newArrayList(datastore.run(
        Query.newEntityQueryBuilder().setKind(Reactable.DATASTORE_KIND).build()))
        .stream()
        .map(Reactable::fromEntity)
        .collect(toList())
        .get(0);
    aShort.setDirector(null);
    aShort.setDirectorId(director.getId());
    aShort.setId(savedShort.getId());
    aShort.setVideoUrl(savedShort.getVideoUrl());
    assertEquals(aShort, savedShort);
    // Asserts that the short's video is saved. We dont assert the uploaded file matches the
    // original one, as it streamed to the client, and so cannot be fully matched.
    TestUtil.assertUrl(savedShort.getVideoUrl(), HttpURLConnection.HTTP_OK, null);
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
