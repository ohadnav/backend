package com.truethat.backend.api;

import com.google.api.services.storage.model.StorageObject;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Strings;
import com.truethat.backend.model.Scene;
import com.truethat.backend.storage.StorageBaseTest;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 10/05/2017.
 */
public class StudioServletTest extends StorageBaseTest {
    private static final LocalServiceTestHelper HELPER                 =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    private static final Long                   CREATOR_ID             = 123L;
    private static final String                 CONTENT_TYPE           = "image/jpeg";
    // Maximum acceptable Datastore put time.
    private static final long                   MAX_DATASTORE_PUT_TIME = 10 * 1000; // 10 seconds
    @Mock
    private HttpServletRequest  mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private Part                mockPart;
    private StringWriter        responseWriter;
    private DatastoreService    datastoreService;
    private StudioServlet       studioServlet;


    /**
     * Starts the local Datastore emulator.
     *
     * @throws IOException          if there are errors starting the local Datastore
     * @throws InterruptedException if there are errors starting the local Datastore
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        HELPER.setUp();
        datastoreService = DatastoreServiceFactory.getDatastoreService();
        studioServlet = new StudioServlet();
        StudioServlet.setBucketName(bucketName);

        MockitoAnnotations.initMocks(this);
    }


    /**
     * Stops the local Datastore emulator.
     *
     * @throws IOException          if there are errors stopping the local Datastore
     * @throws InterruptedException if there are errors stopping the local Datastore
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        HELPER.tearDown();
    }

    @Test
    public void sceneSaved() throws Exception {
        Scene scene = new Scene(CREATOR_ID);
        // Initializing request mock
        when(mockRequest.getParameter(StudioServlet.SCENE_CREATOR_ID_PARAM)).thenReturn(CREATOR_ID.toString());
        File file = new File("src/test/resources/api/1x1_pixel.jpg");
        when(mockPart.getContentType()).thenReturn(CONTENT_TYPE);
        when(mockPart.getInputStream()).thenReturn(new FileInputStream(file));
        when(mockRequest.getPart(StudioServlet.SCENE_IMAGE_PART)).thenReturn(mockPart);
        responseWriter = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Executes the POST request.
        studioServlet.doPost(mockRequest, mockResponse);
        // Reads responded scene ID.
        String response = responseWriter.toString();
        scene.setSceneId(Long.parseLong(response));
        // Asserts that the scene's image is saved.
        final StorageObject foundImage = client.objects().get(bucketName, scene.getImagePath()).execute();
        assertFalse(Strings.isNullOrEmpty(foundImage.getSelfLink()));
        // Asserts that the scene was saved into the Datastore.
        Entity savedEntity = datastoreService.get(KeyFactory.createKey(Scene.DATASTORE_KIND, scene.getSceneId()));
        assertTrue(new Date().getTime() - ((Date) savedEntity.getProperty(Scene.DATASTORE_CREATED)).getTime() <
                   MAX_DATASTORE_PUT_TIME);
        assertEquals(scene.getCreatorId(), savedEntity.getProperty(Scene.DATASTORE_CREATOR_ID));
    }

    @Test(expected = IOException.class)
    public void sceneNotSaved_noImage() throws Exception {
        // Initializing request mock.
        when(mockRequest.getParameter(StudioServlet.SCENE_CREATOR_ID_PARAM)).thenReturn(CREATOR_ID.toString());
        responseWriter = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Executes the POST request.
        studioServlet.doPost(mockRequest, mockResponse);
        // Asserts the request had failed
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, mockResponse.getStatus());
        // Asserts scene ID is not responded.
        assertTrue(Strings.isNullOrEmpty(responseWriter.toString()));
    }

    @Test(expected = IOException.class)
    public void sceneNotSaved_noCreatorId() throws Exception {
        // Initializing request mock.
        File file = new File("src/test/resources/api/1x1_pixel.jpg");
        when(mockPart.getContentType()).thenReturn(CONTENT_TYPE);
        when(mockPart.getInputStream()).thenReturn(new FileInputStream(file));
        when(mockRequest.getPart(StudioServlet.SCENE_IMAGE_PART)).thenReturn(mockPart);
        responseWriter = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Executes the POST request.
        studioServlet.doPost(mockRequest, mockResponse);
        // Asserts the request had failed
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, mockResponse.getStatus());
        // Asserts scene ID is not responded.
        assertTrue(Strings.isNullOrEmpty(responseWriter.toString()));
    }
}