package com.truethat.backend.model;

import com.google.appengine.api.datastore.Entity;
import com.google.common.annotations.VisibleForTesting;
import com.truethat.backend.common.Util;
import java.util.Date;
import java.util.Objects;
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

  public static final String DATASTORE_DEVICE_ID = "deviceId";
  public static final String DATASTORE_FIRST_NAME = "firstName";
  public static final String DATASTORE_LAST_NAME = "lastName";

  /**
   * Time of account creation.
   */
  private Date joined;
  /**
   * Psuedo-unique ID of user's device
   */
  private String deviceId;
  /**
   * How her mom calls her.
   */
  private String firstName;
  /**
   * How his commander calls him.
   */
  private String lastName;
  /**
   * Client ID, that matches datastore key.
   */
  @SuppressWarnings({"unused", "FieldCanBeLocal"}) private Long id;

  public User(Entity entity) {
    if (entity.getProperty(DATASTORE_FIRST_NAME) != null) {
      firstName = (String) entity.getProperty(DATASTORE_FIRST_NAME);
    }
    if (entity.getProperty(DATASTORE_LAST_NAME) != null) {
      lastName = (String) entity.getProperty(DATASTORE_LAST_NAME);
    }
    if (entity.getProperty(DATASTORE_DEVICE_ID) != null) {
      deviceId = (String) entity.getProperty(DATASTORE_DEVICE_ID);
    }
    if (entity.getProperty(DATASTORE_JOINED) != null) {
      joined = (Date) entity.getProperty(DATASTORE_JOINED);
    }
    id = entity.getKey().getId();
  }

  @VisibleForTesting public User(@Nullable String deviceId,
      @Nullable String firstName, @Nullable String lastName, Date joined) {
    this.deviceId = deviceId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.joined = joined;
  }

  @VisibleForTesting public User(long id) {
    this.id = id;
  }

  public Entity toEntity() {
    Entity entity = new Entity(User.DATASTORE_KIND);
    // Current date is set, as mobile frontend does not use that field.
    entity.setProperty(DATASTORE_JOINED, new Date());
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

  public boolean hasId() {
    return id != null;
  }
  public long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public Date getJoined() {
    return joined;
  }

  public void setJoined(Date joined) {
    this.joined = joined;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;

    User user = (User) o;

    if (joined != null ? !joined.equals(user.joined) : user.joined != null) return false;
    if (deviceId != null ? !deviceId.equals(user.deviceId) : user.deviceId != null) return false;
    if (firstName != null ? !firstName.equals(user.firstName) : user.firstName != null) {
      return false;
    }
    if (lastName != null ? !lastName.equals(user.lastName) : user.lastName != null) return false;
    return Objects.equals(id, user.id);
  }

  @Override public String toString() {
    return Util.GSON.toJson(this);
  }
}
