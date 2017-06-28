package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.truethat.backend.common.TestUtil;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
import com.truethat.backend.storage.LocalStorageClient;
import com.truethat.backend.storage.LocalUrlSigner;
import com.truethat.backend.storage.StorageClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 28/06/2017.
 */
public class BaseServletTestSuite {
  DatastoreService datastoreService;
  @Mock HttpServletRequest mockRequest;
  @Mock HttpServletResponse mockResponse;
  StringWriter responseWriter;
  StudioServlet studioServlet;
  private LocalServiceTestHelper localServiceTestHelper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  @Mock
  private ServletConfig mockServletConfig;
  @Mock
  private ServletContext mockServletContext;
  @Mock private Part mockImagePart;
  @Mock private Part mockReactablePart;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    datastoreService = DatastoreServiceFactory.getDatastoreService();
    // Setting mock server context.
    when(mockServletContext.getResourceAsStream(
        StudioServlet.CREDENTIALS_PATH + System.getenv("GOOGLE_CLOUD_PROJECT") + ".json"))
        .thenReturn(new FileInputStream(System.getenv("GOOGLE_APPLICATION_CREDENTIALS")));
    when(mockServletConfig.getServletContext()).thenReturn(mockServletContext);
    studioServlet = new StudioServlet();
    studioServlet.init(mockServletConfig);
    // Setting up local services.
    studioServlet.setUrlSigner(new LocalUrlSigner());
    StorageClient storageClient = new LocalStorageClient();
    storageClient.addBucket(studioServlet.getBucketName());
    studioServlet.setStorageClient(storageClient);
    localServiceTestHelper.setUp();
  }

  @After
  public void tearDown() throws Exception {
    localServiceTestHelper.tearDown();
  }

  /**
   * Prepares request and response mocks for {@link #saveScene(Scene)}.
   * @param reactable to save
   */
  void prepareReactableSave(Reactable reactable) throws Exception {
    File file = new File("src/test/resources/api/1x1_pixel.jpg");
    when(mockImagePart.getContentType()).thenReturn("image/jpeg");
    when(mockImagePart.getInputStream()).thenReturn(new FileInputStream(file));
    when(mockReactablePart.getInputStream()).thenReturn(
        TestUtil.toInputStream(Util.GSON.toJson(reactable)));
    when(mockRequest.getPart(Scene.IMAGE_PART)).thenReturn(mockImagePart);
    when(mockRequest.getPart(Reactable.REACTABLE_PART)).thenReturn(mockReactablePart);
  }

  /**
   * Saves a scene to datastore
   * @param scene to save
   */
  void saveScene(Scene scene) throws Exception {
    prepareReactableSave(scene);
    studioServlet.doPost(mockRequest, mockResponse);
    // Resets response mock.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
  }
}
