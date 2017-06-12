package com.truethat.backend.storage;

import com.google.api.services.storage.Storage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 08/05/2017.
 */
public class StorageFactoryTest {
  @Test
  public void getService() throws Exception {
    Storage client = StorageFactory.getService();
    assertTrue(Storage.class.isInstance(client));
    assertEquals(StorageFactory.getService(), client);
    assertEquals(System.getenv("APPLICATION_NAME"), client.getApplicationName());
  }
}