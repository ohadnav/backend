package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Strings;
import com.truethat.backend.common.TestUtil;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Scene;
import com.truethat.backend.storage.StorageBaseTest;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 10/05/2017.
 */
public class StudioServletTest extends StorageBaseTest {
  private static final LocalServiceTestHelper HELPER =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private static final long DIRECTOR_ID = 123L;
  private static final long CREATED = new Date().getTime();
  private static final String CONTENT_TYPE = "image/jpeg";
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
  private Part mockDirectorPart;
  @Mock
  private Part mockCreatedPart;
  private StringWriter responseWriter;
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
    StudioServlet.setBucketName(bucketName);

    // Initializing request mock
    File file = new File("src/test/resources/api/1x1_pixel.jpg");
    when(mockImagePart.getContentType()).thenReturn(CONTENT_TYPE);
    when(mockImagePart.getInputStream()).thenReturn(new FileInputStream(file));
    when(mockCreatedPart.getInputStream()).thenReturn(
        TestUtil.toInputStream(String.valueOf(CREATED)));
    when(mockDirectorPart.getInputStream()).thenReturn(
        TestUtil.toInputStream(String.valueOf(DIRECTOR_ID)));
    when(mockRequest.getPart(Scene.IMAGE_PART)).thenReturn(mockImagePart);
    when(mockRequest.getPart(Scene.CREATED_PART)).thenReturn(mockCreatedPart);
    when(mockRequest.getPart(Scene.DIRECTOR_ID_PART)).thenReturn(mockDirectorPart);
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
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
  public void sceneSaved() throws Exception {
    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
    // Reads responded scene ID.
    String response = responseWriter.toString();
    Scene scene = Util.GSON.fromJson(response, Scene.class);
    assertEquals(DIRECTOR_ID, scene.getDirectorId());
    assertEquals(CREATED, scene.getCreated().getTime());
    // Asserts that the scene's image is saved. If it's not uploaded, then an exception should be thrown.
    client.objects().get(bucketName, scene.getImagePath()).execute();
    // Asserts that the scene was saved into the Datastore.
    Entity savedEntity =
        datastoreService.get(KeyFactory.createKey(Scene.DATASTORE_KIND, scene.getId()));
    assertEquals(scene.getDirectorId(), savedEntity.getProperty(Scene.DATASTORE_DIRECTOR_ID));
    assertEquals(scene.getCreated(), savedEntity.getProperty(Scene.DATASTORE_CREATED));
    assertNotNull(savedEntity.getProperty(Scene.DATASTORE_IMAGE_SIGNED_URL));
  }

  @Test
  public void sceneNotSaved_noImage() throws Exception {
    when(mockRequest.getPart(Scene.IMAGE_PART)).thenReturn(null);

    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
    // Asserts there is no response.
    assertTrue(Strings.isNullOrEmpty(responseWriter.toString()));
  }

  @Test
  public void sceneNotSaved_noDirectorId() throws Exception {
    when(mockRequest.getPart(Scene.DIRECTOR_ID_PART)).thenReturn(null);

    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
    // Asserts there is no response.
    assertTrue(Strings.isNullOrEmpty(responseWriter.toString()));
  }

  @Test
  public void sceneNotSaved_noCreated() throws Exception {
    when(mockRequest.getPart(Scene.CREATED_PART)).thenReturn(null);

    // Executes the POST request.
    studioServlet.doPost(mockRequest, mockResponse);
    // Asserts there is no response.
    assertTrue(Strings.isNullOrEmpty(responseWriter.toString()));
  }
}