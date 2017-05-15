package com.truethat.backend.api;

import com.google.api.services.storage.model.StorageObject;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.truethat.backend.model.Scene;
import com.truethat.backend.storage.StorageBaseTest;
import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 10/05/2017.
 */
public class TheaterApiTest extends StorageBaseTest {
    private static final LocalServiceTestHelper HELPER                 =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    private static final long                   CREATOR_ID             = 123;
    // Maximum acceptable Datastore put time.
    private static final long                   MAX_DATASTORE_PUT_TIME = 10 * 1000; // 10 seconds
    private DatastoreService datastoreService;
    private TheaterApi       theaterApi;


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
        theaterApi = new TheaterApi();
        TheaterApi.setBucketName(bucketName);
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
        RandomAccessFile file = new RandomAccessFile("src/test/resources/api/1x1_pixel.jpg", "r");
        byte[] bytes = new byte[(int) file.length()];
        file.readFully(bytes);
        Image image = ImagesServiceFactory.makeImage(bytes);
        Scene toSave = new Scene(CREATOR_ID, image);
        Scene savedScene = theaterApi.saveScene(toSave);
        // Asserts that the scene's image is saved.
        final StorageObject foundImage = client.objects().get(bucketName, toSave.getImagePath()).execute();
        assertEquals(savedScene.getImageUrl(), foundImage.getSelfLink());
        // Asserts that the scene was saved into the Datastore.
        Entity savedEntity = datastoreService.get(KeyFactory.createKey(Scene.DATASTORE_KIND, savedScene.getSceneId()));
        assertTrue(new Date().getTime() - ((Date) savedEntity.getProperty(Scene.DATASTORE_CREATED)).getTime() <
                   MAX_DATASTORE_PUT_TIME);
        assertEquals(toSave.getCreatorId(), savedEntity.getProperty(Scene.DATASTORE_CREATOR_ID));
    }
}