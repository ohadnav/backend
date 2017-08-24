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
import com.truethat.backend.model.Pose;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.User;
import com.truethat.backend.storage.LocalStorageClient;
import com.truethat.backend.storage.LocalUrlSigner;
import com.truethat.backend.storage.StorageClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
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
  private InteractionServlet interactionServlet;
  ReactableEnricher enricher;
  @Mock
  private ServletConfig mockServletConfig;
  @Mock
  private ServletContext mockServletContext;
  @Mock private Part mockImagePart;
  @Mock private Part mockReactablePart;

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
    // Initialize Studio servlet
    when(mockServletContext.getResourceAsStream(
        StudioServlet.CREDENTIALS_PATH + System.getenv("__GCLOUD_PROJECT__") + ".json"))
        .thenReturn(new FileInputStream(System.getenv("GOOGLE_APPLICATION_CREDENTIALS")));
    when(mockServletConfig.getServletContext()).thenReturn(mockServletContext);
    studioServlet = new StudioServlet();
    studioServlet.setDatastore(datastore);
    studioServlet.init(mockServletConfig);
    enricher = new ReactableEnricher(datastore);
    // Setting up local services.
    studioServlet.setUrlSigner(new LocalUrlSigner());
    StorageClient storageClient = new LocalStorageClient();
    storageClient.addBucket(studioServlet.getBucketName());
    studioServlet.setStorageClient(storageClient);
    // Initializes user
    defaultUser = new User(DEVICE_ID, FIRST_NAME, LAST_NAME, NOW);
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

  @After
  public void tearDown() throws Exception {
    HELPER.reset();
  }

  /**
   * Prepares request and response mocks for {@link #savePose(Pose)}.
   *
   * @param reactable to save
   */
  void preparePoseSave(Reactable reactable) throws Exception {
    File file = new File("src/test/resources/api/1x1_pixel.jpg");
    when(mockImagePart.getContentType()).thenReturn("image/jpeg");
    when(mockImagePart.getInputStream()).thenReturn(new FileInputStream(file));
    when(mockReactablePart.getInputStream()).thenReturn(
        TestUtil.toInputStream(Util.GSON.toJson(reactable)));
    when(mockRequest.getPart(Pose.IMAGE_PART)).thenReturn(mockImagePart);
    when(mockRequest.getPart(Reactable.REACTABLE_PART)).thenReturn(mockReactablePart);
  }

  /**
   * Saves a pose to datastore and updates {@code pose} id.
   *
   * @param pose to save
   */
  void savePose(Pose pose) throws Exception {
    resetResponseMock();
    preparePoseSave(pose);
    studioServlet.doPost(mockRequest, mockResponse);
    // Updates the pose id.
    Pose respondedPose = Util.GSON.fromJson(responseWriter.toString(), Pose.class);
    pose.setId(respondedPose.getId());
    pose.setCreated(respondedPose.getCreated());
    pose.setImageSignedUrl(respondedPose.getImageSignedUrl());
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
