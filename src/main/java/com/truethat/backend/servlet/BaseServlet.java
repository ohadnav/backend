package com.truethat.backend.servlet;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.KeyFactory;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServlet;

/**
 * Proudly created by ohad on 24/08/2017.
 */
public abstract class BaseServlet extends HttpServlet {
  Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  SceneEnricher enricher = new SceneEnricher(datastore);
  private Map<String, KeyFactory> keyFactories = new HashMap<>();

  public Datastore getDatastore() {
    return datastore;
  }

  public void setDatastore(Datastore datastore) {
    this.datastore = datastore;
    enricher = new SceneEnricher(datastore);
    keyFactories = new HashMap<>();
  }

  public KeyFactory getKeyFactory(String kind) {
    if (!keyFactories.containsKey(kind)) {
      keyFactories.put(kind, datastore.newKeyFactory().setKind(kind));
    }

    return keyFactories.get(kind);
  }
}
