package com.truethat.backend.servlet;

import com.truethat.backend.common.Util;
import com.truethat.backend.model.EventType;
import com.truethat.backend.model.InteractionEvent;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    InteractionEvent interactionEvent = Util.GSON.fromJson(req.getReader(), InteractionEvent.class);
    if (interactionEvent == null) throw new IOException("Missing interaction event");
    StringBuilder errorBuilder = new StringBuilder();
    if (!isEventValid(interactionEvent, errorBuilder)) {
      throw new IOException(
          "Invalid interaction event: " + errorBuilder + " in " + interactionEvent);
    }
    // Puts the event in the datastore and responds it to the client.
    resp.getWriter()
        .print(Util.GSON.toJson(
            new InteractionEvent(
                datastore.add(interactionEvent.toEntityBuilder(eventKeyFactory).build()))));
  }

  /**
   * @return whether the event has a valid data.
   */
  @SuppressWarnings("RedundantIfStatement") private boolean isEventValid(
      InteractionEvent interactionEvent, StringBuilder errorBuilder) {
    if (interactionEvent.getTimestamp() == null) {
      errorBuilder.append("missing timestamp.");
      return false;
    }
    if (interactionEvent.getEventType() == null) {
      errorBuilder.append("missing event type.");
      return false;
    }
    if (interactionEvent.getEventType() == EventType.REACTABLE_VIEW
        && interactionEvent.getReaction() != null) {
      errorBuilder.append("reaction exists for view event.");
      return false;
    }
    if (interactionEvent.getEventType() == EventType.REACTABLE_REACTION
        && interactionEvent.getReaction() == null) {
      errorBuilder.append("missing reaction for reaction event.");
      return false;
    }
    if (interactionEvent.getUserId() == null) {
      errorBuilder.append("missing user ID.");
      return false;
    }
    if (datastore.get(userKeyFactory.newKey(interactionEvent.getUserId())) == null) {
      errorBuilder.append("user with ID ")
          .append(interactionEvent.getUserId())
          .append(" not found.");
      return false;
    }
    if (interactionEvent.getReactableId() == null) {
      errorBuilder.append("missing reactable ID.");
      return false;
    }
    if (datastore.get(reactableKeyFactory.newKey(interactionEvent.getReactableId())) == null) {
      errorBuilder.append("reactable with ID ")
          .append(interactionEvent.getUserId())
          .append(" not found.");
      return false;
    }
    return true;
  }
}
