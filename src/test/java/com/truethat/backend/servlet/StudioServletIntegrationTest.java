package com.truethat.backend.servlet;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.google.common.collect.Lists;
import com.truethat.backend.common.TestUtil;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
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
import javax.servlet.ServletContext;
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
  private ServletContext mockServletContext;
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private Part mockImagePart;
  @Mock
  private Part mockReactablePart;
  private StringWriter responseWriter;
  private StudioServlet studioServlet;
  private AuthServlet authServlet;
  private Datastore datastore;

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
    // Setting mock server context.
    when(mockServletContext.getResourceAsStream(
        StudioServlet.CREDENTIALS_PATH + System.getenv("__GCLOUD_PROJECT__") + ".json"))
        .thenReturn(new FileInputStream(System.getenv("GOOGLE_APPLICATION_CREDENTIALS")));
    when(mockServletConfig.getServletContext()).thenReturn(mockServletContext);
    studioServlet.init(mockServletConfig);
    studioServlet.setBucketName(bucketName);
  }

  @Test
  public void saveScene() throws Exception {
    initResponseMock();
    // Saves scene director to datastore.
    User user = new User("my-iphone", "taylor", "davis", Timestamp.now());
    // Mocks a request body with user.
    when(mockRequest.getReader()).thenReturn(toBufferedReader(Util.GSON.toJson(user)));
    // Sends the POST request
    authServlet.doPost(mockRequest, mockResponse);
    // Updates the user id.
    User response = Util.GSON.fromJson(responseWriter.toString(), User.class);
    user.setId(response.getId());
    initResponseMock();
    // Saves scene
    Scene scene = new Scene(user, Timestamp.now(), null);
    // Initializing request mock
    String fileName = "src/test/resources/api/1x1_pixel.jpg";
    when(mockImagePart.getContentType()).thenReturn("image/jpeg");
    when(mockImagePart.getInputStream()).thenReturn(new FileInputStream(new File(fileName)));
    when(mockReactablePart.getInputStream()).thenReturn(
        TestUtil.toInputStream(Util.GSON.toJson(scene)));
    when(mockRequest.getPart(Scene.IMAGE_PART)).thenReturn(mockImagePart);
    when(mockRequest.getPart(Reactable.REACTABLE_PART)).thenReturn(mockReactablePart);
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
    TestUtil.assertUrl(savedScene.getImageSignedUrl(), HttpURLConnection.HTTP_OK,
        new FileInputStream(new File(fileName)));
    scene.setDirector(null);
    scene.setDirectorId(user.getId());
    scene.setId(savedScene.getId());
    scene.setImageSignedUrl(savedScene.getImageSignedUrl());
    assertEquals(scene, savedScene);
  }

  private void initResponseMock() throws Exception {
    // Resets response mock.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
  }
}
