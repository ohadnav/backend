package com.truethat.backend.storage;

import javax.annotation.Nonnull;

/**
 * Proudly created by ohad on 28/06/2017.
 */
public class LocalUrlSigner implements UrlSigner {
  private static final String SIGNATURE = "?signed";

  @Override public String sign(@Nonnull String objectPath)
      throws Exception {
    return objectPath + SIGNATURE;
  }
}
