package com.truethat.backend.common;

import com.google.appengine.api.datastore.Query;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.truethat.backend.external.GsonUTCDateAdapter;
import com.truethat.backend.external.RuntimeTypeAdapterFactory;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Scene;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

/**
 * Proudly created by ohad on 01/06/2017.
 */
public class Util {
  public static final Gson GSON =
      new GsonBuilder()
          .registerTypeAdapter(Date.class, new GsonUTCDateAdapter())
          .registerTypeAdapterFactory(
              RuntimeTypeAdapterFactory.of(Reactable.class).registerSubtype(Scene.class))
          .create();

  public static String inputStreamToString(InputStream inputStream) throws IOException {
    return CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
  }

  public static void setFilter(Query query, List<Query.Filter> filters,
      Query.CompositeFilterOperator filterOperator) {
    if (filters.size() == 1) {
      query.setFilter(filters.get(0));
    } else if (filters.size() > 1) {
      query.setFilter(filterOperator.of(filters));
    }
  }
}
