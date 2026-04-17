package org.hibernate.event.spi;

import org.hibernate.engine.spi.EntityEntry;

public class FlushEntityEvent extends AbstractEvent {
   private Object entity;
   private Object[] propertyValues;
   private Object[] databaseSnapshot;
   private int[] dirtyProperties;
   private boolean hasDirtyCollection;
   private boolean dirtyCheckPossible;
   private boolean dirtyCheckHandledByInterceptor;
   private EntityEntry entityEntry;

   public FlushEntityEvent(EventSource source, Object entity, EntityEntry entry) {
      super(source);
      this.entity = entity;
      this.entityEntry = entry;
   }

   public EntityEntry getEntityEntry() {
      return this.entityEntry;
   }

   public Object[] getDatabaseSnapshot() {
      return this.databaseSnapshot;
   }

   public void setDatabaseSnapshot(Object[] databaseSnapshot) {
      this.databaseSnapshot = databaseSnapshot;
   }

   public boolean hasDatabaseSnapshot() {
      return this.databaseSnapshot != null;
   }

   public boolean isDirtyCheckHandledByInterceptor() {
      return this.dirtyCheckHandledByInterceptor;
   }

   public void setDirtyCheckHandledByInterceptor(boolean dirtyCheckHandledByInterceptor) {
      this.dirtyCheckHandledByInterceptor = dirtyCheckHandledByInterceptor;
   }

   public boolean isDirtyCheckPossible() {
      return this.dirtyCheckPossible;
   }

   public void setDirtyCheckPossible(boolean dirtyCheckPossible) {
      this.dirtyCheckPossible = dirtyCheckPossible;
   }

   public int[] getDirtyProperties() {
      return this.dirtyProperties;
   }

   public void setDirtyProperties(int[] dirtyProperties) {
      this.dirtyProperties = dirtyProperties;
   }

   public boolean hasDirtyCollection() {
      return this.hasDirtyCollection;
   }

   public void setHasDirtyCollection(boolean hasDirtyCollection) {
      this.hasDirtyCollection = hasDirtyCollection;
   }

   public Object[] getPropertyValues() {
      return this.propertyValues;
   }

   public void setPropertyValues(Object[] propertyValues) {
      this.propertyValues = propertyValues;
   }

   public Object getEntity() {
      return this.entity;
   }
}
