package com.truethat.backend.servlet;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.KeyFactory;
import com.truethat.backend.model.InteractionEvent;
import com.truethat.backend.model.Scene;
import com.truethat.backend.model.User;
import javax.servlet.http.HttpServlet;

/**
 * Proudly created by ohad on 24/08/2017.
 */
public abstract class BaseServlet extends HttpServlet {
  Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  KeyFactory userKeyFactory = datastore.newKeyFactory().setKind(User.DATASTORE_KIND);
  KeyFactory sceneKeyFactory =
      datastore.newKeyFactory().setKind(Scene.DATASTORE_KIND);
  KeyFactory eventKeyFactory =
      datastore.newKeyFactory().setKind(InteractionEvent.DATASTORE_KIND);
  SceneEnricher enricher = new SceneEnricher(datastore);

  public Datastore getDatastore() {
    return datastore;
  }

  public void setDatastore(Datastore datastore) {
    this.datastore = datastore;
    userKeyFactory = datastore.newKeyFactory().setKind(User.DATASTORE_KIND);
    sceneKeyFactory = datastore.newKeyFactory().setKind(Scene.DATASTORE_KIND);
    enricher = new SceneEnricher(datastore);
    eventKeyFactory = datastore.newKeyFactory().setKind(InteractionEvent.DATASTORE_KIND);
  }

  public KeyFactory getSceneKeyFactory() {
    return sceneKeyFactory;
  }
}
