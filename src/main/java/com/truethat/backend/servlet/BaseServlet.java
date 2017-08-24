package com.truethat.backend.servlet;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.KeyFactory;
import com.truethat.backend.model.InteractionEvent;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.User;
import javax.servlet.http.HttpServlet;

/**
 * Proudly created by ohad on 24/08/2017.
 */
public abstract class BaseServlet extends HttpServlet {
  Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  KeyFactory userKeyFactory = datastore.newKeyFactory().setKind(User.DATASTORE_KIND);
  KeyFactory reactableKeyFactory =
      datastore.newKeyFactory().setKind(Reactable.DATASTORE_KIND);
  KeyFactory eventKeyFactory =
      datastore.newKeyFactory().setKind(InteractionEvent.DATASTORE_KIND);
  ReactableEnricher enricher = new ReactableEnricher(datastore);

  public Datastore getDatastore() {
    return datastore;
  }

  public void setDatastore(Datastore datastore) {
    this.datastore = datastore;
    userKeyFactory = datastore.newKeyFactory().setKind(User.DATASTORE_KIND);
    reactableKeyFactory = datastore.newKeyFactory().setKind(Reactable.DATASTORE_KIND);
    enricher = new ReactableEnricher(datastore);
    eventKeyFactory = datastore.newKeyFactory().setKind(InteractionEvent.DATASTORE_KIND);
  }

  public KeyFactory getReactableKeyFactory() {
    return reactableKeyFactory;
  }
}
