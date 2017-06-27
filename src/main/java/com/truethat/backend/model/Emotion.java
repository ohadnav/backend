package com.truethat.backend.model;

/**
 * Proudly created by ohad on 11/06/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/Emotion.java</a>
 */
@SuppressWarnings("unused") public enum Emotion {
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
