package com.truethat.backend.model;

/**
 * Proudly created by ohad on 11/06/2017.
 *
 * @android <a>https://goo.gl/Qjv4gr</a>
 */
public enum Emotion {
  HAPPY(1),
  SAD(2);

  private int code;

  Emotion(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
