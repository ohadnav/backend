package com.truethat.backend.model;

import com.google.appengine.api.datastore.Entity;
import com.google.common.annotations.VisibleForTesting;
import java.util.Date;
import javax.annotation.Nullable;

/**
 * Proudly created by ohad on 12/06/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/User.java</a>
 */
@SuppressWarnings("unused") public class User {
  /**
   * Datastore kind.
   */
  public static final String DATASTORE_KIND = "User";
  // ----------------- Datastore column names -------------------------
  public static final String DATASTORE_JOINED = "joined";

  public static final String DATASTORE_PHONE_NUMBER = "phoneNumber";
  public static final String DATASTORE_DEVICE_ID = "deviceId";
  public static final String DATASTORE_FIRST_NAME = "firstName";
  public static final String DATASTORE_LAST_NAME = "lastName";

  private Date joined;

  private String phoneNumber;

  private String deviceId;

  private String firstName;

  private String lastName;
  /**
   * Client ID, should match datastore key.
   */
  @SuppressWarnings({"unused", "FieldCanBeLocal"}) private Long id;

  @VisibleForTesting public User(@Nullable String phoneNumber, @Nullable String deviceId,
      @Nullable String firstName, @Nullable String lastName) {
    this.phoneNumber = phoneNumber;
    this.deviceId = deviceId;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  @VisibleForTesting public User(Long id) {
    this.id = id;
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
    if (firstName != null) {
      entity.setProperty(DATASTORE_FIRST_NAME, firstName);
    }
    if (lastName != null) {
      entity.setProperty(DATASTORE_LAST_NAME, lastName);
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

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }
}
