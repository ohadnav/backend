package com.truethat.backend.servlet;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.KeyFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Proudly created by ohad on 24/08/2017.
 */
public abstract class BaseServlet extends HttpServlet {
  private final Logger log = Logger.getLogger(getClass().getName());
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

  @Override public void init(ServletConfig config) throws ServletException {
    super.init(config);
    if (Boolean.parseBoolean(System.getenv("DEBUG"))) {
      log.info("Bringing up " + getClass().getSimpleName());
    }
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (Boolean.parseBoolean(System.getenv("DEBUG"))) {
      log.info(req.getMethod()
          + " "
          + req.getRequestURI()
          + "\nAgent: "
          + req.getHeader("User-Agent"));
    }
  }
}
