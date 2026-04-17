package org.hibernate.engine.loading.internal;

import java.io.Serializable;
import java.sql.ResultSet;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.pretty.MessageHelper;

public class LoadingCollectionEntry {
   private final ResultSet resultSet;
   private final CollectionPersister persister;
   private final Serializable key;
   private final PersistentCollection collection;

   public LoadingCollectionEntry(ResultSet resultSet, CollectionPersister persister, Serializable key, PersistentCollection collection) {
      this.resultSet = resultSet;
      this.persister = persister;
      this.key = key;
      this.collection = collection;
   }

   public ResultSet getResultSet() {
      return this.resultSet;
   }

   public CollectionPersister getPersister() {
      return this.persister;
   }

   public Serializable getKey() {
      return this.key;
   }

   public PersistentCollection getCollection() {
      return this.collection;
   }

   public String toString() {
      return this.getClass().getName() + "<rs=" + this.resultSet + ", coll=" + MessageHelper.collectionInfoString(this.persister.getRole(), this.key) + ">@" + Integer.toHexString(this.hashCode());
   }
}
