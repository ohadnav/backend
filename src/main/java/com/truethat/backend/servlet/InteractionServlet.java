package com.truethat.backend.servlet;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.KeyFactory;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.InteractionEvent;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Proudly created by ohad on 03/07/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/InteractionAPI.java</a>
 */
@WebServlet(value = "/interaction", name = "InteractionEvent")
public class InteractionServlet extends HttpServlet {
  private Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private KeyFactory eventKeyFactory =
      datastore.newKeyFactory().setKind(InteractionEvent.DATASTORE_KIND);

  public InteractionServlet setDatastore(Datastore datastore) {
    this.datastore = datastore;
    eventKeyFactory = datastore.newKeyFactory().setKind(InteractionEvent.DATASTORE_KIND);
    return this;
  }

  /**
   * Saves events to Datastore, and response the saved {@link InteractionEvent}.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    InteractionEvent interactionEvent = Util.GSON.fromJson(req.getReader(), InteractionEvent.class);
    if (interactionEvent == null) throw new IOException("Missing interaction event");
    if (!interactionEvent.isValid()) {
      throw new IOException("Invalid interaction event: " + Util.GSON.toJson(interactionEvent));
    }
    // Puts the event in the datastore and responds it to the client.
    resp.getWriter()
        .print(Util.GSON.toJson(
            new InteractionEvent(
                datastore.add(interactionEvent.toEntityBuilder(eventKeyFactory).build()))));
  }
}
