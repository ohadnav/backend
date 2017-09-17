package com.truethat.backend.model;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.common.annotations.VisibleForTesting;
import com.truethat.backend.servlet.BaseServlet;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Proudly created by ohad on 12/06/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/User.java</a>
 * @ios <a>https://github.com/true-that/ios/blob/master/TrueThat/Model/User.swift</a>
 */
@SuppressWarnings("unused") public class User extends BaseModel {
  /**
   * Datastore kind.
   */
  public static final String KIND = "User";
  // ----------------- Datastore column names -------------------------
  public static final String COLUMN_JOINED = "joined";

  public static final String COLUMN_DEVICE_ID = "deviceId";
  public static final String COLUMN_FIRST_NAME = "firstName";
  public static final String COLUMN_LAST_NAME = "lastName";

  /**
   * Time of account creation.
   */
  private Timestamp joined;
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

  public User(FullEntity entity) {
    super(entity);
    if (entity.contains(COLUMN_FIRST_NAME)) {
      firstName = entity.getString(COLUMN_FIRST_NAME);
    }
    if (entity.contains(COLUMN_LAST_NAME)) {
      lastName = entity.getString(COLUMN_LAST_NAME);
    }
    if (entity.contains(COLUMN_DEVICE_ID)) {
      deviceId = entity.getString(COLUMN_DEVICE_ID);
    }
    if (entity.contains(COLUMN_JOINED)) {
      joined = entity.getTimestamp(COLUMN_JOINED);
    }
  }

  @VisibleForTesting public User(@Nullable String deviceId,
      @Nullable String firstName, @Nullable String lastName, Timestamp joined) {
    this.deviceId = deviceId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.joined = joined;
  }

  @Override public FullEntity.Builder<IncompleteKey> toEntityBuilder(BaseServlet servlet) {
    FullEntity.Builder<IncompleteKey> builder = super.toEntityBuilder(servlet);
    if (deviceId != null) {
      builder.set(COLUMN_DEVICE_ID, deviceId);
    }
    if (firstName != null) {
      builder.set(COLUMN_FIRST_NAME, firstName);
    }
    if (lastName != null) {
      builder.set(COLUMN_LAST_NAME, lastName);
    }
    builder.set(COLUMN_JOINED, joined != null ? joined : Timestamp.now());
    return builder;
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

  @Override String getKind() {
    return KIND;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public Timestamp getJoined() {
    return joined;
  }

  public void setJoined(Timestamp joined) {
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

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Prepares this instance for enrichment, so that private data is not exposed.
   */
  public void deletePrivateData() {
    deviceId = null;
  }
}
