package com.truethat.backend.servlet;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityValue;
import com.google.cloud.datastore.Key;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.EventType;
import com.truethat.backend.model.InteractionEvent;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.stream.Collectors.toList;

/**
 * Proudly created by ohad on 03/07/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/InteractionApi.java</a>
 * @ios <a>https://github.com/true-that/ios/blob/master/TrueThat/Model/Network/InteractionApi.swift</a>
 */
@WebServlet(value = "/interaction", name = "InteractionEvent")
public class InteractionServlet extends BaseServlet {
  /**
   * Saves events to Datastore, and response the saved {@link InteractionEvent}.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPost(req, resp);
    InteractionEvent interactionEvent = Util.GSON.fromJson(req.getReader(), InteractionEvent.class);
    if (interactionEvent == null) throw new IOException("Missing interaction event");
    StringBuilder errorBuilder = new StringBuilder();
    if (!isValidEvent(interactionEvent, errorBuilder)) {
      throw new IOException(
          "Invalid interaction event: " + errorBuilder + " in " + interactionEvent);
    }
    // Puts the event in the datastore and responds it to the client.
    resp.getWriter()
        .print(Util.GSON.toJson(
            new InteractionEvent(
                datastore.add(interactionEvent.toEntityBuilder(this).build()))));
  }

  /**
   * @return whether the event has a valid data.
   */
  @SuppressWarnings("RedundantIfStatement") private boolean isValidEvent(
      InteractionEvent interactionEvent, StringBuilder errorBuilder) {
    if (interactionEvent.getTimestamp() == null) {
      errorBuilder.append("missing timestamp.");
      return false;
    }
    if (interactionEvent.getEventType() == null) {
      errorBuilder.append("missing event type.");
      return false;
    }
    if (interactionEvent.getEventType() == EventType.VIEW
        && interactionEvent.getReaction() != null) {
      errorBuilder.append("reaction exists for view event.");
      return false;
    }
    if (interactionEvent.getEventType() == EventType.REACTION
        && interactionEvent.getReaction() == null) {
      errorBuilder.append("missing reaction for reaction event.");
      return false;
    }
    if (interactionEvent.getUserId() == null) {
      errorBuilder.append("missing user ID.");
      return false;
    }
    if (datastore.get(getKeyFactory(User.KIND).newKey(interactionEvent.getUserId())) == null) {
      errorBuilder.append("user with ID ")
          .append(interactionEvent.getUserId())
          .append(" not found.");
      return false;
    }
    if (interactionEvent.getSceneId() == null) {
      errorBuilder.append("missing scene ID.");
      return false;
    }
    if (interactionEvent.getMediaId() == null) {
      errorBuilder.append("missing media ID.");
      return false;
    }
    Entity entity = datastore.get(getKeyFactory(Scene.KIND).newKey(interactionEvent.getSceneId()));
    if (entity == null) {
      errorBuilder.append("scene with ID ")
          .append(interactionEvent.getUserId())
          .append(" not found.");
      return false;
    } else if (entity.getList(Scene.COLUMN_MEDIA)
        .stream()
        .filter(value -> Objects.equals(
            ((Key) ((EntityValue) value).get().getKey()).getId(), interactionEvent.getMediaId()))
        .collect(toList())
        .isEmpty()) {
      errorBuilder.append("media ID ")
          .append(interactionEvent.getMediaId())
          .append(" is not part of scene ")
          .append(new Scene(entity));
      return false;
    }
    return true;
  }
}
