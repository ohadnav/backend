package com.truethat.backend.common;

import com.google.cloud.Timestamp;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.truethat.backend.external.GsonUTCDateAdapter;
import com.truethat.backend.external.RuntimeTypeAdapterFactory;
import com.truethat.backend.model.Pose;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.Short;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Proudly created by ohad on 01/06/2017.
 */
public class Util {
  public static final Gson GSON =
      new GsonBuilder()
          .registerTypeAdapter(Timestamp.class, new GsonUTCDateAdapter())
          .registerTypeAdapterFactory(
              RuntimeTypeAdapterFactory.of(Reactable.class)
                  .registerSubtype(Pose.class)
                  .registerSubtype(Short.class))
          .create();

  /**
   * Resets input stream and reads it.
   *
   * @param inputStream to read
   *
   * @return the string representation of {@code inputStream}
   */
  public static String inputStreamToString(InputStream inputStream) throws IOException {
    inputStream.reset();
    return CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
  }

  public static Date timestampToDate(Timestamp timestamp) {
    return new Date(timestamp.getSeconds() * 1000 + timestamp.getNanos() / 1000000);
  }
}
