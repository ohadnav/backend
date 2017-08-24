package com.truethat.backend.model;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Proudly created by ohad on 12/06/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/User.java</a>
 */
@SuppressWarnings("unused") public class User extends BaseModel {
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

  public User(Entity entity) {
    super(entity);
    if (entity.contains(DATASTORE_FIRST_NAME)) {
      firstName = entity.getString(DATASTORE_FIRST_NAME);
    }
    if (entity.contains(DATASTORE_LAST_NAME)) {
      lastName = entity.getString(DATASTORE_LAST_NAME);
    }
    if (entity.contains(DATASTORE_DEVICE_ID)) {
      deviceId = entity.getString(DATASTORE_DEVICE_ID);
    }
    if (entity.contains(DATASTORE_JOINED)) {
      joined = entity.getTimestamp(DATASTORE_JOINED);
    }

  }

  @VisibleForTesting public User(@Nullable String deviceId,
      @Nullable String firstName, @Nullable String lastName, Timestamp joined) {
    this.deviceId = deviceId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.joined = joined;
  }

  @Override public FullEntity.Builder<IncompleteKey> toEntityBuilder(KeyFactory keyFactory) {
    FullEntity.Builder<IncompleteKey> builder = super.toEntityBuilder(keyFactory);
    if (deviceId != null) {
      builder.set(DATASTORE_DEVICE_ID, deviceId);
    }
    if (firstName != null) {
      builder.set(DATASTORE_FIRST_NAME, firstName);
    }
    if (lastName != null) {
      builder.set(DATASTORE_LAST_NAME, lastName);
    }
    builder.set(DATASTORE_JOINED, joined != null ? joined : Timestamp.now());
    return builder;
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

  /**
   * Prepares this instance for enrichment, so that private data is not exposed.
   */
  public void deletePrivateData() {
    deviceId = null;
  }
}
