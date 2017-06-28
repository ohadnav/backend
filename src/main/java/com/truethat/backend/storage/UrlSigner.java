package com.truethat.backend.storage;

import javax.annotation.Nonnull;

/**
 * Proudly created by ohad on 28/06/2017.
 */
public interface UrlSigner {
  String sign(@Nonnull String privateKey, @Nonnull String objectPath) throws Exception;
}
