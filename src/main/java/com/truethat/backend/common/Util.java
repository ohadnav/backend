package com.truethat.backend.common;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Proudly created by ohad on 01/06/2017.
 */
public class Util {
    public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();

    public static String inputStreamToString(InputStream inputStream) throws IOException {
        return CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
    }
}
