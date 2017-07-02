package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.truethat.backend.common.TestUtil;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
import com.truethat.backend.storage.BaseStorageTestSuite;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.Date;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.truethat.backend.common.TestUtil.assertEqualsForEntityAndReactable;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 28/06/2017.
 */
public class StudioServletIntegrationTest extends BaseStorageTestSuite {
  private static final LocalServiceTestHelper HELPER =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private static final long DIRECTOR_ID = 123L;
  private static final Date CREATED = new Date();
  private static final String CONTENT_TYPE = "image/jpeg";
  private static final Reactable REACTABLE = new Scene(DIRECTOR_ID, CREATED, null);
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
  private DatastoreService datastoreService;
  private StudioServlet studioServlet;

  /**
   * Starts the local Datastore emulator.
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.initMocks(this);
    HELPER.setUp();
    datastoreService = DatastoreServiceFactory.getDatastoreService();
    studioServlet = new StudioServlet();
    // Setting mock server context.
    when(mockServletContext.getResourceAsStream(
        StudioServlet.CREDENTIALS_PATH + System.getenv("GOOGLE_CLOUD_PROJECT") + ".json"))
        .thenReturn(new FileInputStream(System.getenv("GOOGLE_APPLICATION_CREDENTIALS")));
    when(mockServletConfig.getServletContext()).thenReturn(mockServletContext);
    studioServlet.init(mockServletConfig);
    studioServlet.setBucketName(bucketName);
  }

  /**
   * Stops the local Datastore emulator.
   */
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    HELPER.tearDown();
  }

  @Test
  public void reactableSaved() throws Exception {
    // Initializing request mock
    String fileName = "src/test/resources/api/1x1_pixel.jpg";
    when(mockImagePart.getContentType()).thenReturn(CONTENT_TYPE);
    when(mockImagePart.getInputStream()).thenReturn(new FileInputStream(new File(fileName)));
    when(mockReactablePart.getInputStream()).thenReturn(
        TestUtil.toInputStream(Util.GSON.toJson(REACTABLE)));
    when(mockRequest.getPart(Scene.IMAGE_PART)).thenReturn(mockImagePart);
    when(mockRequest.getPart(Reactable.REACTABLE_PART)).thenReturn(mockReactablePart);
    StringWriter responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
    // Asserts that the reactable was saved into the Datastore.
    Entity savedEntity =
        datastoreService.prepare(new Query(Reactable.DATASTORE_KIND)).asSingleEntity();
    Scene scene = (Scene) Reactable.fromEntity(savedEntity);
    // Asserts that the scene's image is saved, and matches the uploaded one.
    TestUtil.assertUrl(scene.getImageSignedUrl(), HttpURLConnection.HTTP_OK,
        new FileInputStream(new File(fileName)));
    assertEqualsForEntityAndReactable(savedEntity, scene);
  }
}
