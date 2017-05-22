package com.truethat.backend.model;

/**
 * Proudly created by ohad on 08/05/2017.
 */
public class Scene {
    public static final  String DATASTORE_KIND       = "Scene";
    // Column names within Datastore.
    public static final  String DATASTORE_CREATED    = "created";
    public static final  String DATASTORE_CREATOR_ID = "creatorId";
    // Sub path for scene images within the storage bucket.
    private static final String STORAGE_IMAGES_PATH  = "scene/images/";

    private static final String DEFAULT_IMAGE_TYPE = "jpg";

    private Long   sceneId;
    // ID of Scene creator.
    private Long   creatorId;

    public Scene(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getSceneId() {
        return sceneId;
    }

    public void setSceneId(Long sceneId) {
        this.sceneId = sceneId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    /**
     * @return the sub path of the image destination within the storage bucket. If image is null, then an empty string
     * is returned.
     */
    public String getImagePath() {
        return STORAGE_IMAGES_PATH + creatorId + "/" + sceneId + "." + DEFAULT_IMAGE_TYPE;
    }
}
