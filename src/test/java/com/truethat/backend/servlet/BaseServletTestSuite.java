package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.truethat.backend.common.TestUtil;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Interaction;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import com.truethat.backend.storage.LocalStorageClient;
import com.truethat.backend.storage.LocalUrlSigner;
import com.truethat.backend.storage.StorageClient;
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
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.truethat.backend.common.TestUtil.toBufferedReader;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 28/06/2017.
 */
public class BaseServletTestSuite {
  static final String PHONE_NUMBER = "+0123456789";
  static final String DEVICE_ID = "my-iPhone";
  static final String FIRST_NAME = "django";
  static final String LAST_NAME = "unchained";
  static final Date NOW = new Date();
  User defaultUser;
  DatastoreService datastoreService;
  @Mock HttpServletRequest mockRequest;
  @Mock HttpServletResponse mockResponse;
  StringWriter responseWriter;
  StudioServlet studioServlet;
  private InteractionServlet interactionServlet;
  private AuthServlet authServlet;
  private LocalServiceTestHelper localServiceTestHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
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
    // Initialize Servlets
    resetResponseMock();
    interactionServlet = new InteractionServlet();
    authServlet = new AuthServlet();
    // Initialize Studio servlet
    when(mockServletContext.getResourceAsStream(
        StudioServlet.CREDENTIALS_PATH + System.getenv("__GCLOUD_PROJECT__") + ".json"))
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
    // Initializes user
    defaultUser = new User(PHONE_NUMBER, DEVICE_ID, FIRST_NAME, LAST_NAME, NOW);
  }

  @After
  public void tearDown() throws Exception {
    localServiceTestHelper.tearDown();
  }

  /**
   * Prepares request and response mocks for {@link #saveScene(Scene)}.
   *
   * @param reactable to save
   */
  void prepareSceneSave(Reactable reactable) throws Exception {
    File file = new File("src/test/resources/api/1x1_pixel.jpg");
    when(mockImagePart.getContentType()).thenReturn("image/jpeg");
    when(mockImagePart.getInputStream()).thenReturn(new FileInputStream(file));
    when(mockReactablePart.getInputStream()).thenReturn(
        TestUtil.toInputStream(Util.GSON.toJson(reactable)));
    when(mockRequest.getPart(Scene.IMAGE_PART)).thenReturn(mockImagePart);
    when(mockRequest.getPart(Reactable.REACTABLE_PART)).thenReturn(mockReactablePart);
  }

  /**
   * Saves a scene to datastore and updates {@code scene} id.
   *
   * @param scene to save
   */
  void saveScene(Scene scene) throws Exception {
    resetResponseMock();
    prepareSceneSave(scene);
    studioServlet.doPost(mockRequest, mockResponse);
    // Updates the scene id.
    Scene respondedScene = Util.GSON.fromJson(responseWriter.toString(), Scene.class);
    scene.setId(respondedScene.getId());
    scene.setCreated(respondedScene.getCreated());
    scene.setImageSignedUrl(respondedScene.getImageSignedUrl());
  }

  /**
   * Saves a {@link Interaction} to datastore and updates {@code interaction} id.
   *
   * @param interaction to save
   */
  void saveInteraction(Interaction interaction) throws Exception {
    resetResponseMock();
    when(mockRequest.getReader()).thenReturn(
        toBufferedReader(Util.GSON.toJson(interaction)));
    interactionServlet.doPost(mockRequest, mockResponse);
    // Updates the scene id.
    Interaction response = Util.GSON.fromJson(responseWriter.toString(), Interaction.class);
    interaction.setId(response.getId());
    interaction.setTimestamp(response.getTimestamp());
  }

  /**
   * Creates a new {@link User} in the datastore, and updates {@code user} id.
   *
   * @param user from which to create the user
   */
  void saveUser(User user) throws Exception {
    resetResponseMock();
    // Mocks a request body with user.
    when(mockRequest.getReader()).thenReturn(toBufferedReader(Util.GSON.toJson(user)));
    // Sends the POST request
    authServlet.doPost(mockRequest, mockResponse);
    // Updates the user id.
    User response = Util.GSON.fromJson(responseWriter.toString(), User.class);
    user.setId(response.getId());
    user.setJoined(response.getJoined());

  }

  void resetResponseMock() throws Exception {
    // Resets response mock.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
  }
}
