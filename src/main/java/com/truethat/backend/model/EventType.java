package com.truethat.backend.model;

/**
 * Proudly created by ohad on 12/06/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/EventType.java</a>
 * @ios <a>https://github.com/true-that/ios/blob/master/TrueThat/Model/EventType.swift</a>
 */
// Next available - 4
@SuppressWarnings("unused") public enum EventType {
  /**
   * User viewed a scene.
   */
  VIEW(1),

  /**
   * User reacted to a scene.
   */
  REACTION(2),
  /**
   * User reported a scene or a media as inappropriate.
   */
  REPORT(3);

  private int code;

  EventType(int code) {
    this.code = code;
  }

  public static EventType fromCode(int code) {
    for (EventType eventType : values()) {
      if (eventType.getCode() == code) {
        return eventType;
      }
    }
    throw new IllegalArgumentException("Illegal " + EventType.class.getSimpleName() + " code.");
  }

  public int getCode() {
    return code;
  }
}
