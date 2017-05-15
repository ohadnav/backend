package com.truethat.backend.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.services.storage.model.StorageObject;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.common.annotations.VisibleForTesting;
import com.truethat.backend.model.Scene;
import com.truethat.backend.storage.StorageUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Proudly created by ohad on 07/05/2017.
 */

@Api(
        name = "theater",
        version = "v1",
        namespace =
        @ApiNamespace(
                ownerDomain = "api",
                ownerName = "api.truethat.com"
        )
)
public class TheaterApi {
    private static final Logger log = Logger.getLogger(TheaterApi.class.getName());

    private static String           bucketName       = "truethat-theater";
    private static DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

    @VisibleForTesting
    static void setBucketName(String bucketName) {
        TheaterApi.bucketName = bucketName;
    }

    /**
     * Saves the scene to Datastore, and its image to Storage.
     *
     * @param scene to save.
     * @return the minified version of the saved scene, to return to the user.
     */
    @ApiMethod(name = "scene", path = "save", httpMethod = ApiMethod.HttpMethod.POST)
    public Scene saveScene(Scene scene) {
        Entity entity = new Entity(Scene.DATASTORE_KIND);
        entity.setProperty(Scene.DATASTORE_CREATED, new Date());
        entity.setProperty(Scene.DATASTORE_CREATOR_ID, scene.getCreatorId());
        datastoreService.put(entity);
        // Updates the scene with the generated key.
        scene.setSceneId(entity.getKey().getId());
        if (null != scene.getImage()) {
            try {
                final String imageExtension = scene.getImage().getFormat().name().toLowerCase();
                final StorageObject uploadedImage =
                        StorageUtil.uploadStream(scene.getImagePath(), "image/" + imageExtension,
                                                 new ByteArrayInputStream(scene.getImage().getImageData()),
                                                 bucketName);
                // Note that image URL is normally not stored on Datastore, as it is deductible from the scene.
                scene.setImageUrl(uploadedImage.getSelfLink());
            } catch (GeneralSecurityException e) {
                log.severe(
                        "Failed to save image for " + scene.getSceneId() + ", We have security thingy going on - " +
                        e.getMessage());
                return null;
            } catch (IOException e) {
                log.severe("Failed to save image for " + scene.getSceneId() + ", bad IO day - " + e.getMessage());
                return null;
            }
        }
        return scene.minify();
    }
}
