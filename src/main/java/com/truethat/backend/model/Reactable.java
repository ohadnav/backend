package com.truethat.backend.model;

import com.google.appengine.api.datastore.Entity;
import com.truethat.backend.common.Util;
import com.truethat.backend.servlet.StudioServlet;
import java.util.Date;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

/**
 * Proudly created by ohad on 27/06/2017.
 * <p>
 * A media item that the user can be reacted to, such as a {@link Scene}.
 * <p>
 * Each implementation should register at {@link Util#GSON}.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/model/Reactable.java</a>
 */
public abstract class Reactable {
  /**
   * Multipart HTTP request part names, as used by backend endpoints such as {@link
   * com.truethat.backend.servlet.StudioServlet}.
   *
   * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/StudioAPI.java</a>
   */
  public static final String REACTABLE_PART = "reactable";
  /**
   * Datastore kind.
   */
  public static final String DATASTORE_KIND = "Reactable";
  /**
   * Datastore column names.
   */
  public static final String DATASTORE_CREATED = "created";
  public static final String DATASTORE_DIRECTOR_ID = "directorId";
  public static final String DATASTORE_TYPE = "type";
  /**
   * Scene ID, as defined by its datastore key.
   */
  private long id;
  /**
   * ID of the Scene director (i.e. its creator).
   */
  private long directorId;

  /**
   * Client created UTC timestamp
   */
  private Date created;

  public Reactable(Entity entity) {
    id = entity.getKey().getId();
    directorId = (Long) entity.getProperty(DATASTORE_DIRECTOR_ID);
    created = (Date) entity.getProperty(DATASTORE_CREATED);
  }

  public Reactable(Long directorId, Date created) {
    this.directorId = directorId;
    this.created = created;
  }

  // A default constructor is provided for serialization and de-serialization.
  @SuppressWarnings("unused") Reactable() {
  }

  /**
   * @param entity to create a {@link Reactable} from.
   * @return a {@link Reactable} based on {@code entity}. The correct constructor is called based on {@code entity.getProperty(DATASTORE_TYPE}.
   */
  public static Reactable fromEntity(Entity entity) {
    if (entity.getProperty(DATASTORE_TYPE) == null) {
      throw new IllegalArgumentException("Entity is missing a type property");
    }
    String type = (String)entity.getProperty(DATASTORE_TYPE);
    Reactable reactable = null;
    if (Objects.equals(type, Scene.class.getSimpleName())) {
      reactable = new Scene(entity);
    }
    return reactable;
  }

  public long getId() {
    return id;
  }

  public long getDirectorId() {
    return directorId;
  }

  public Date getCreated() {
    return created;
  }

  public Entity toEntity() {
    Entity entity = new Entity(DATASTORE_KIND);
    entity.setProperty(DATASTORE_CREATED, created);
    entity.setProperty(DATASTORE_DIRECTOR_ID, directorId);
    entity.setProperty(DATASTORE_TYPE, this.getClass().getSimpleName());
    return entity;
  }

  public abstract void prepareSave(HttpServletRequest req,
      StudioServlet servlet) throws Exception;

  public void save(HttpServletRequest req, StudioServlet servlet) throws Exception {
    prepareSave(req, servlet);
    Entity entity = toEntity();
    servlet.getDatastoreService().put(entity);
    id = entity.getKey().getId();
  }

  @Override public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + (int) (directorId ^ (directorId >>> 32));
    result = 31 * result + (created != null ? created.hashCode() : 0);
    return result;
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Reactable)) return false;

    Reactable reactable = (Reactable) o;

    if (id != reactable.id) return false;
    if (directorId != reactable.directorId) return false;
    return created != null ? created.equals(reactable.created) : reactable.created == null;
  }

  @Override
  public String toString() {
    return Util.GSON.toJson(this);
  }
}
