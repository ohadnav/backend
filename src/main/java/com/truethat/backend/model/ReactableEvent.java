package com.truethat.backend.model;

import com.google.gson.annotations.SerializedName;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Proudly created by ohad on 11/06/2017.
 */
public class ReactableEvent {
    /**
     * HTTP request field names for {@link com.truethat.backend.servlet.TheaterServlet#doPost(HttpServletRequest, HttpServletResponse)}.
     *
     * @android <a>https://goo.gl/xsORJL</a>
     */
    public static final String USER_ID_FIELD = "user_id";
    public static final String SCENE_ID_FIELD = "scene_id";
    public static final String TIMESTAMP_FIELD = "timestamp";
    public static final String EVENT_CODE_FIELD = "event_code";
    public static final String REACTION_FIELD = "reaction";
    /**
     * Datastore kind.
     */
    public static final String DATASTORE_KIND       = "ReactableEvent";
    /**
     * Datastore column names.
     *
     * @android <a>https://goo.gl/xsORJL</a>
     */
    public static final String DATASTORE_TIMESTAMP  = "timestamp";
    public static final String DATASTORE_USER_ID    = "userId";
    public static final String DATASTORE_SCENE_ID   = "sceneId";
    public static final String DATASTORE_EVENT_CODE = "eventCode";
    public static final String DATASTORE_REACTION   = "reaction";

    /**
     * Event codes.
     *
     * @android <a>https://goo.gl/8B3Pgc</a>
     */
    public static final int REACTABLE_VIEW     = 100;
    public static final int REACTABLE_REACTION = 101;

    /**
     * ReactableEvent ID, as defined by its datastore key.
     */
    private long id;

    /**
     * Client UTC timestamp
     */
    private Date timestamp;

    /**
     * ID of the user that triggered the event.
     */
    @SerializedName("user_id")
    private long userId;

    /**
     * For {@link #REACTABLE_REACTION}, must be null for irrelevant events (such as {@link #REACTABLE_VIEW}).
     */
    private Emotion reaction;

    /**
     * Of the {@link Scene} that was interacted with.
     */
    @SerializedName("scene_id")
    private long sceneId;

    public ReactableEvent(Date timestamp, long userId, Emotion reaction, long sceneId) {
        this.timestamp = timestamp;
        this.userId = userId;
        this.reaction = reaction;
        this.sceneId = sceneId;
    }
}
