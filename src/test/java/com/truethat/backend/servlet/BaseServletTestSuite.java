package com.truethat.backend.servlet;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.truethat.backend.common.TestUtil;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.InteractionEvent;
import com.truethat.backend.model.Media;
import com.truethat.backend.model.Photo;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import com.truethat.backend.storage.LocalStorageClient;
import com.truethat.backend.storage.StorageClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeoutException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.threeten.bp.Duration;

import static com.truethat.backend.common.TestUtil.toBufferedReader;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 28/06/2017.
 */
public class BaseServletTestSuite {
  static final String DEVICE_ID = "my-iPhone";
  static final String FIRST_NAME = "django";
  static final String LAST_NAME = "the unchained";
  static final Timestamp NOW = Timestamp.now();
  private static final LocalDatastoreHelper HELPER = LocalDatastoreHelper.create(1.0);

  User defaultUser;
  Datastore datastore;
  KeyFactory userKeyFactory;
  KeyFactory eventKeyFactory;
  @Mock HttpServletRequest mockRequest;
  @Mock HttpServletResponse mockResponse;
  StringWriter responseWriter;
  StudioServlet studioServlet;
  AuthServlet authServlet;
  SceneEnricher enricher;
  private InteractionServlet interactionServlet;
  @Mock private Part mockFilePart;
  @Mock private Part mockScenePart;

  @BeforeClass
  public static void beforeClass() throws IOException, InterruptedException {
    HELPER.start();
  }

  @AfterClass
  public static void afterClass() throws IOException, InterruptedException, TimeoutException {
    HELPER.stop(Duration.ofMinutes(1));
  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    // Initialize datastore
    HELPER.reset();
    datastore = HELPER.getOptions().getService();
    userKeyFactory = datastore.newKeyFactory().setKind(User.DATASTORE_KIND);
    eventKeyFactory = datastore.newKeyFactory().setKind(InteractionEvent.DATASTORE_KIND);
    emptyDatastore(null);
    // Initialize Servlets
    resetResponseMock();
    interactionServlet = new InteractionServlet();
    interactionServlet.setDatastore(datastore);
    authServlet = new AuthServlet();
    authServlet.setDatastore(datastore);
    studioServlet = new StudioServlet();
    studioServlet.setDatastore(datastore);
    enricher = new SceneEnricher(datastore);
    // Setting up local services.
    StorageClient storageClient = new LocalStorageClient();
    storageClient.addBucket(studioServlet.getBucketName());
    studioServlet.setStorageClient(storageClient);
    // Initializes user
    defaultUser = new User(DEVICE_ID, FIRST_NAME, LAST_NAME, NOW);
  }

  @After
  public void tearDown() throws Exception {
    HELPER.reset();
  }

  /**
   * Removes all entities from datastore.
   *
   * @param kind to empty
   */
  void emptyDatastore(String kind) {
    StructuredQuery.Builder<Key> queryBuilder = Query.newKeyQueryBuilder();
    if (!Strings.isNullOrEmpty(kind)) {
      queryBuilder.setKind(kind);
    }
    QueryResults<Key> result = datastore.run(queryBuilder.build());
    datastore.delete(Iterators.toArray(result, Key.class));
  }

  /**
   * Prepares request and response mocks for {@link #saveScene(Scene)}.
   *
   * @param scene to save
   */
  void prepareSceneSave(Scene scene) throws Exception {
    File file;
    if (scene.getMedia() instanceof Photo) {
      file = new File("src/test/resources/servlet/1x1_pixel.jpg");
      when(mockFilePart.getContentType()).thenReturn("image/jpeg");
    } else {
      file = new File("src/test/resources/servlet/wink.mp4");
      when(mockFilePart.getContentType()).thenReturn("video/mp4");
    }
    when(mockFilePart.getInputStream()).thenReturn(new FileInputStream(file));
    when(mockScenePart.getInputStream()).thenReturn(
        TestUtil.toInputStream(Util.GSON.toJson(scene)));
    when(mockRequest.getPart(Media.MEDIA_PART)).thenReturn(mockFilePart);
    when(mockRequest.getPart(Scene.SCENE_PART)).thenReturn(mockScenePart);
  }

  /**
   * Saves a pose to datastore and updates {@code pose} id.
   *
   * @param scene to save
   */
  void saveScene(Scene scene) throws Exception {
    resetResponseMock();
    prepareSceneSave(scene);
    studioServlet.doPost(mockRequest, mockResponse);
    // Updates the pose id.
    Scene responded = Util.GSON.fromJson(responseWriter.toString(), Scene.class);
    scene.setId(responded.getId());
    scene.setCreated(responded.getCreated());
    scene.getMedia().setUrl(responded.getMedia().getUrl());
  }

  /**
   * Saves a {@link InteractionEvent} to datastore and updates {@code interactionEvent} id.
   *
   * @param interactionEvent to save
   */
  void saveInteraction(InteractionEvent interactionEvent) throws Exception {
    resetResponseMock();
    when(mockRequest.getReader()).thenReturn(
        toBufferedReader(Util.GSON.toJson(interactionEvent)));
    interactionServlet.doPost(mockRequest, mockResponse);
    // Updates the pose id.
    InteractionEvent response =
        Util.GSON.fromJson(responseWriter.toString(), InteractionEvent.class);
    interactionEvent.setId(response.getId());
    interactionEvent.setTimestamp(response.getTimestamp());
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
