package com.truethat.backend.storage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

/**
 * Proudly created by ohad on 30/05/2017.
 */
public class DefaultUrlSigner implements UrlSigner {
  // Base url of google cloud storage objects
  static final String BASE_GOOGLE_CLOUD_STORAGE_URL = "https://storage.googleapis.com";
  // Google Service Account.
  private static final String CLIENT_ACCOUNT =
      System.getenv("__GCLOUD_PROJECT__") + "@appspot.gserviceaccount.com";
  // Default expiration is set to 24 hours
  private static final long EXPIRATION_MILLIS = TimeUnit.DAYS.toMillis(1);

  private PrivateKey privateKey;

  public DefaultUrlSigner(String privateKey)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    this.privateKey = getPrivateKey(privateKey);
  }

  /**
   * Prepares the signing request.
   *
   * @param objectPath          relative URL to BASE_GOOGLE_CLOUD_STORAGE_URL. Must begin with "/"
   * @param expiryTimeInSeconds the signature expiration time in UTC seconds.
   * @return the signing request with the proper expiration time.
   */
  private static String getSignRequest(String objectPath, long expiryTimeInSeconds) {
    return "GET" + "\n"
        + "" + "\n"
        + "" + "\n"
        + expiryTimeInSeconds + "\n"
        + objectPath;
  }

  /**
   * Builds the signed URL format as required by Google.
   *
   * @param fullObjectPath       full (unauthorized) URL to the object.
   * @param signature            as provided by {@link #getSignedString(String)}
   * @param expiredTimeInSeconds the signature expiration time in UTC seconds.
   * @return the signed URL, which can be used by the end user.
   */
  private static String buildUrl(String fullObjectPath, String signature,
      long expiredTimeInSeconds) {
    return fullObjectPath
        + "?GoogleAccessId=" + CLIENT_ACCOUNT
        + "&Expires=" + expiredTimeInSeconds
        + "&Signature=" + signature;
  }

  /**
   * Builds the signed URL format as required by Google.
   *
   * @param objectPath relative URL to BASE_GOOGLE_CLOUD_STORAGE_URL.
   * @return the signed URL, which can be used by the end user.
   */
  public String sign(@Nonnull String objectPath)
      throws Exception {
    // Prefixes "/" to objectPath if needed.
    objectPath = objectPath.startsWith("/") ? objectPath : "/" + objectPath;

    // Expiry time is calculated in seconds as required by Google.
    long expiryTimeInSeconds = (System.currentTimeMillis() + EXPIRATION_MILLIS) / 1000;
    String fullObjectPath = BASE_GOOGLE_CLOUD_STORAGE_URL + objectPath;
    String signRequest = getSignRequest(objectPath, expiryTimeInSeconds);
    String signature = getSignedString(signRequest);

    // URL encode the signed string so that we can add this URL
    signature = URLEncoder.encode(signature, "UTF-8");

    return buildUrl(fullObjectPath, signature, expiryTimeInSeconds);
  }

  /**
   * Use SHA256withRSA to sign the request
   *
   * @param signRequest signing request as required by Google.
   *
   * @return the signed request string.
   */
  private String getSignedString(String signRequest)
      throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException,
      SignatureException {
    Signature privateSignature = Signature.getInstance("SHA256withRSA");
    privateSignature.initSign(privateKey);
    privateSignature.update(signRequest.getBytes("UTF-8"));
    byte[] signed = privateSignature.sign();
    return Base64.getEncoder().encodeToString(signed);
  }

  /**
   * @param privateKey as found in the credentials json resource.
   *
   * @return private key object from unencrypted PKCS#8 file content.
   */
  private PrivateKey getPrivateKey(String privateKey)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    // Remove extra characters in private key.
    String realPrivateKey = privateKey.replaceAll("-----END PRIVATE KEY-----", "")
        .replaceAll("-----BEGIN PRIVATE KEY-----", "")
        .replaceAll("\n", "");
    byte[] decoded = Base64.getDecoder().decode(realPrivateKey);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePrivate(spec);
  }
}
