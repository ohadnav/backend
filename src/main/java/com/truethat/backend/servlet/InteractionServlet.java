package com.truethat.backend.servlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Interaction;
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
@WebServlet(value = "/interaction", name = "Interaction")
public class InteractionServlet extends HttpServlet {
  private static final DatastoreService DATASTORE_SERVICE =
      DatastoreServiceFactory.getDatastoreService();

  /**
   * Saves events to Datastore, and response the saved {@link Interaction}.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Interaction interaction = Util.GSON.fromJson(req.getReader(), Interaction.class);
    Entity toPut = interaction.toEntity();
    DATASTORE_SERVICE.put(toPut);
    interaction.setId(toPut.getKey().getId());
    resp.getWriter().print(Util.GSON.toJson(interaction));
  }
}
