package com.truethat.backend.api;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.common.annotations.VisibleForTesting;
import com.truethat.backend.model.Scene;
import com.truethat.backend.storage.StorageUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Proudly created by ohad on 07/05/2017.
 */

@WebServlet(value = "/studio", name = "Studio")
@MultipartConfig
public class StudioServlet extends HttpServlet {
    static final         String           SCENE_IMAGE_PART       = "scene_image";
    static final         String           SCENE_CREATOR_ID_PARAM = "creator_id";
    private static final Logger           LOG                    = Logger.getLogger(StudioServlet.class.getName());
    private static final DatastoreService DATASTORE_SERVICE      = DatastoreServiceFactory.getDatastoreService();

    private static String bucketName = "truethat-studio";

    @VisibleForTesting
    static void setBucketName(String bucketName) {
        StudioServlet.bucketName = bucketName;
    }

    /**
     * Saves the scene within the request to storage and datastore.
     *
     * @param req multipart request with the scene image and creator ID.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Scene saved = saveScene(req);
            resp.getWriter().print(saved.getSceneId());
        } catch (GeneralSecurityException e) {
            LOG.severe("Bad security while saving scene: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Security exception thrown.");
        }
    }

    /**
     * Saves the scene to Datastore, and its image to Storage.
     *
     * @param req with the scene data to save.
     */
    private Scene saveScene(HttpServletRequest req) throws IOException, ServletException, GeneralSecurityException {
        Part imagePart = req.getPart(SCENE_IMAGE_PART);
        if (imagePart == null) throw new IOException("Missing scene image.");
        if (req.getParameter(SCENE_CREATOR_ID_PARAM) == null) throw new IOException("Missing scene creator ID.");
        Scene scene = new Scene(Long.parseLong(req.getParameter(SCENE_CREATOR_ID_PARAM)));
        // Saves the scene to Datastore.
        Entity entity = new Entity(Scene.DATASTORE_KIND);
        entity.setProperty(Scene.DATASTORE_CREATED, new Date());
        entity.setProperty(Scene.DATASTORE_CREATOR_ID, scene.getCreatorId());
        DATASTORE_SERVICE.put(entity);
        // Updates the scene with the generated key.
        scene.setSceneId(entity.getKey().getId());
        // Saves the image to storage.
        StorageUtil.uploadStream(scene.getImagePath(), imagePart.getContentType(),
                                 imagePart.getInputStream(),
                                 bucketName);
        return scene;
    }
}
