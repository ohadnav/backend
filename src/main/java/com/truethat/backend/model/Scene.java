package com.truethat.backend.model;

import com.google.appengine.api.datastore.Entity;
import com.google.gson.annotations.SerializedName;
import com.truethat.backend.common.Util;

import java.util.Date;
import java.util.Objects;

/**
 * Proudly created by ohad on 08/05/2017.
 */
public class Scene {
    /**
     * Multipart HTTP request part names, as used by {@link com.truethat.backend.api.StudioServlet}
     */
    public static final String IMAGE_PART                 = "image";
    public static final String DIRECTOR_ID_PART           = "director_id";
    public static final String CREATED_PART               = "created";
    // Datastore kind
    public static final String DATASTORE_KIND             = "Scene";
    // Column names within Datastore.
    public static final String DATASTORE_CREATED          = "created";
    public static final String DATASTORE_DIRECTOR_ID      = "directorId";
    public static final String DATASTORE_IMAGE_SIGNED_URL = "imageSignedUrl";
    // Sub path for scene images within the storage bucket.
    private static final String STORAGE_IMAGES_PATH        = "scene/images/";

    private static final String DEFAULT_IMAGE_TYPE = "jpg";

    // Scene ID, i.e datastore key.
    private long id;
    // ID of the Scene director (i.e. its creator).
    @SerializedName("director_id")
    private long directorId;

    // Client created UTC timestamp
    private Date created;

    // Authenticated query string for the scene image, which is stored in Google Storage.
    @SerializedName("image_signed_url")
    private String imageSignedUrl;

    public Scene(Entity entity) {
        id = entity.getKey().getId();
        directorId = (Long) entity.getProperty(Scene.DATASTORE_DIRECTOR_ID);
        created = (Date) entity.getProperty(Scene.DATASTORE_CREATED);
        imageSignedUrl = (String) entity.getProperty(Scene.DATASTORE_IMAGE_SIGNED_URL);
    }

    public Scene(Long directorId, Date created) {
        this.directorId = directorId;
        this.created = created;
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getDirectorId() {
        return directorId;
    }

    public Date getCreated() {
        return created;
    }

    public String getImageSignedUrl() {
        return imageSignedUrl;
    }

    public void setImageSignedUrl(String imageSignedUrl) {
        this.imageSignedUrl = imageSignedUrl;
    }

    /**
     * @return the sub path of the image destination within the storage bucket.
     */
    public String getImagePath() {
        return STORAGE_IMAGES_PATH + directorId + "/" + created.getTime() + "." + DEFAULT_IMAGE_TYPE;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!Scene.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Scene other = (Scene) obj;
        return id == other.id && directorId == other.directorId &&
               Objects.equals(imageSignedUrl, other.imageSignedUrl) && created.equals(other.created);
    }

    @Override
    public String toString() {
        return Util.GSON.toJson(this);
    }
}
