package com.truethat.backend.model;

import com.google.api.client.util.Key;
import com.google.appengine.api.images.Image;

/**
 * Proudly created by ohad on 08/05/2017.
 */
public class Scene {
    public static final String DATASTORE_KIND       = "Scene";
    // Column names within Datastore.
    public static final String DATASTORE_CREATED    = "created";
    public static final String DATASTORE_CREATOR_ID = "creatorId";
    public static final String DATASTORE_IMAGE_URL  = "imageUrl";
    // Sub path for scene images within the storage bucket.
    public static final String STORAGE_IMAGES_PATH  = "scene/images/";

    @Key
    private Long   sceneId;
    // ID of Scene creator.
    private Long   creatorId;
    private Image  image;
    private String imageUrl;

    public Scene(Long creatorId, Image image) {
        this.creatorId = creatorId;
        this.image = image;
        this.imageUrl = null;
    }

    private Scene(Long sceneId, Long creatorId, String imageUrl) {
        this.sceneId = sceneId;
        this.creatorId = creatorId;
        this.imageUrl = imageUrl;
        this.image = null;
    }

    public Long getSceneId() {
        return sceneId;
    }

    public Scene setSceneId(Long sceneId) {
        this.sceneId = sceneId;
        return this;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public Image getImage() {
        return image;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Scene setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public Scene minify() {
        return new Scene(this.sceneId, this.creatorId, this.imageUrl);
    }

    /**
     * @return the sub path of the image destination within the storage bucket. If image is null, then an empty string
     * is returned.
     */
    public String getImagePath() {
        if (image == null) return "";
        return STORAGE_IMAGES_PATH + creatorId + "/" + sceneId + "." + image.getFormat().name().toLowerCase();
    }
}
