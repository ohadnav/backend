package com.truethat.backend.model;

/**
 * Proudly created by ohad on 11/06/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/Emotion.java</a>
 * @ios <a>https://github.com/true-that/ios/blob/master/TrueThat/Model/Emotion.swift</a>
 */
// Next available - 5
public enum Emotion {
  HAPPY(1),
  FEAR(2),
  SURPRISE(3),
  DISGUST(4);

  private int code;

  Emotion(int code) {
    this.code = code;
  }

  public static Emotion fromCode(int code) {
    for (Emotion emotion : values()) {
      if (emotion.getCode() == code) {
        return emotion;
      }
    }
    throw new IllegalArgumentException("Illegal " + Emotion.class.getSimpleName() + " code.");
  }

  public int getCode() {
    return code;
  }
}
