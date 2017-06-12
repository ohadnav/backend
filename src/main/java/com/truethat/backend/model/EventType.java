package com.truethat.backend.model;

/**
 * Proudly created by ohad on 12/06/2017.
 *
 * @android <a>https://goo.gl/8B3Pgc</a>
 */
public enum EventType {
  /**
   * User viewed a reactable.
   */
  REACTABLE_VIEW(1),

  /**
   * User reacted to a reactable.
   */
  REACTABLE_REACTION(2);

  private int code;

  EventType(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
