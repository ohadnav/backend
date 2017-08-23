package com.truethat.backend.external;

import com.google.cloud.Timestamp;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.truethat.backend.common.Util;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Proudly created by ohad on 27/06/2017 for TrueThat.
 * <p>
 * Big thanks to <a>https://github.com/google/gson/issues/281</a>
 */

public class GsonUTCDateAdapter implements JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {

  private final DateFormat dateFormat;

  public GsonUTCDateAdapter() {
    //This is the format I need
    dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Override public synchronized JsonElement serialize(Timestamp timestamp, Type type,
      JsonSerializationContext jsonSerializationContext) {
    return new JsonPrimitive(dateFormat.format(Util.timestampToDate(timestamp)));
  }

  @Override public synchronized Timestamp deserialize(JsonElement jsonElement, Type type,
      JsonDeserializationContext jsonDeserializationContext) {
    try {
      return Timestamp.of(dateFormat.parse(jsonElement.getAsString()));
    } catch (ParseException e) {
      throw new JsonParseException(e);
    }
  }
}