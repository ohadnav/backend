package com.truethat.backend.model;

import com.google.appengine.api.datastore.Entity;
import com.google.common.annotations.VisibleForTesting;
import java.util.Date;
import javax.annotation.Nullable;

/**
 * Proudly created by ohad on 12/06/2017.
 *
 * @android https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/auth/User.java
 */
public class User {
  /**
   * Datastore kind.
   */
  public static final String DATASTORE_KIND = "User";
  /**
   * Datastore column names.
   */
  public static final String DATASTORE_JOINED = "joined";

  public static final String DATASTORE_PHONE_NUMBER = "phoneNumber";
  public static final String DATASTORE_DEVICE_ID = "deviceId";
  public static final String DATASTORE_NAME = "name";

  private Date joined;

  private String phoneNumber;

  private String deviceId;

  private String name;
  /**
   * Client ID, should match datastore key.
   */
  @SuppressWarnings({"unused", "FieldCanBeLocal"}) private Long id;

  @VisibleForTesting public User(@Nullable String phoneNumber, @Nullable String deviceId) {
    this.phoneNumber = phoneNumber;
    this.deviceId = deviceId;
  }

  public Entity toEntity() {
    Entity entity = new Entity(User.DATASTORE_KIND);
    // Current date is set, as mobile frontend does not use that field.
    entity.setProperty(DATASTORE_JOINED, new Date());
    if (phoneNumber != null) {
      entity.setProperty(DATASTORE_PHONE_NUMBER, phoneNumber);
    }
    if (deviceId != null) {
      entity.setProperty(DATASTORE_DEVICE_ID, deviceId);
    }
    if (name !=null) {
      entity.setProperty(DATASTORE_NAME, name);
    }
    return entity;
  }

  public Long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getDeviceId() {
    return deviceId;
  }
}
