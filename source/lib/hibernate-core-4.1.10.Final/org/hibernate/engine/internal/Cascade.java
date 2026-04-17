package org.hibernate.engine.internal;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public final class Cascade {
   public static final int AFTER_INSERT_BEFORE_DELETE = 1;
   public static final int BEFORE_INSERT_AFTER_DELETE = 2;
   public static final int AFTER_INSERT_BEFORE_DELETE_VIA_COLLECTION = 3;
   public static final int AFTER_UPDATE = 0;
   public static final int BEFORE_FLUSH = 0;
   public static final int AFTER_EVICT = 0;
   public static final int BEFORE_REFRESH = 0;
   public static final int AFTER_LOCK = 0;
   public static final int BEFORE_MERGE = 0;
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, Cascade.class.getName());
   private int cascadeTo;
   private EventSource eventSource;
   private CascadingAction action;
   private Stack componentPathStack = new Stack();

   public Cascade(CascadingAction action, int cascadeTo, EventSource eventSource) {
      this.cascadeTo = cascadeTo;
      this.eventSource = eventSource;
      this.action = action;
   }

   private SessionFactoryImplementor getFactory() {
      return this.eventSource.getFactory();
   }

   public void cascade(EntityPersister persister, Object parent) throws HibernateException {
      this.cascade(persister, parent, (Object)null);
   }

   public void cascade(EntityPersister persister, Object parent, Object anything) throws HibernateException {
      if (persister.hasCascades() || this.action.requiresNoCascadeChecking()) {
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Processing cascade {0} for: {1}", this.action, persister.getEntityName());
         }

         Type[] types = persister.getPropertyTypes();
         CascadeStyle[] cascadeStyles = persister.getPropertyCascadeStyles();
         boolean hasUninitializedLazyProperties = persister.hasUninitializedLazyProperties(parent);

         for(int i = 0; i < types.length; ++i) {
            CascadeStyle style = cascadeStyles[i];
            String propertyName = persister.getPropertyNames()[i];
            if (!hasUninitializedLazyProperties || !persister.getPropertyLaziness()[i] || this.action.performOnLazyProperty()) {
               if (style.doCascade(this.action)) {
                  this.cascadeProperty(parent, persister.getPropertyValue(parent, i), types[i], style, propertyName, anything, false);
               } else if (this.action.requiresNoCascadeChecking()) {
                  this.action.noCascade(this.eventSource, persister.getPropertyValue(parent, i), parent, persister, i);
               }
            }
         }

         if (LOG.isTraceEnabled()) {
            LOG.tracev("Done processing cascade {0} for: {1}", this.action, persister.getEntityName());
         }
      }

   }

   private void cascadeProperty(Object parent, Object child, Type type, CascadeStyle style, String propertyName, Object anything, boolean isCascadeDeleteEnabled) throws HibernateException {
      if (child != null) {
         if (type.isAssociationType()) {
            AssociationType associationType = (AssociationType)type;
            if (this.cascadeAssociationNow(associationType)) {
               this.cascadeAssociation(parent, child, type, style, anything, isCascadeDeleteEnabled);
            }
         } else if (type.isComponentType()) {
            this.cascadeComponent(parent, child, (CompositeType)type, propertyName, anything);
         }
      } else if (this.isLogicalOneToOne(type) && style.hasOrphanDelete() && this.action.deleteOrphans()) {
         EntityEntry entry = this.eventSource.getPersistenceContext().getEntry(parent);
         if (entry != null && entry.getStatus() != Status.SAVING) {
            Object loadedValue;
            if (this.componentPathStack.isEmpty()) {
               loadedValue = entry.getLoadedValue(propertyName);
            } else {
               loadedValue = null;
            }

            if (loadedValue != null) {
               EntityEntry valueEntry = this.eventSource.getPersistenceContext().getEntry(loadedValue);
               if (valueEntry != null) {
                  String entityName = valueEntry.getPersister().getEntityName();
                  if (LOG.isTraceEnabled()) {
                     Serializable id = valueEntry.getPersister().getIdentifier(loadedValue, this.eventSource);
                     String description = MessageHelper.infoString(entityName, id);
                     LOG.tracev("Deleting orphaned entity instance: {0}", description);
                  }

                  this.eventSource.delete(entityName, loadedValue, false, new HashSet());
               }
            }
         }
      }

   }

   private boolean isLogicalOneToOne(Type type) {
      return type.isEntityType() && ((EntityType)type).isLogicalOneToOne();
   }

   private boolean cascadeAssociationNow(AssociationType associationType) {
      return associationType.getForeignKeyDirection().cascadeNow(this.cascadeTo);
   }

   private void cascadeComponent(Object parent, Object child, CompositeType componentType, String componentPropertyName, Object anything) {
      this.componentPathStack.push(componentPropertyName);
      Object[] children = componentType.getPropertyValues(child, (SessionImplementor)this.eventSource);
      Type[] types = componentType.getSubtypes();

      for(int i = 0; i < types.length; ++i) {
         CascadeStyle componentPropertyStyle = componentType.getCascadeStyle(i);
         String subPropertyName = componentType.getPropertyNames()[i];
         if (componentPropertyStyle.doCascade(this.action)) {
            this.cascadeProperty(parent, children[i], types[i], componentPropertyStyle, subPropertyName, anything, false);
         }
      }

      this.componentPathStack.pop();
   }

   private void cascadeAssociation(Object parent, Object child, Type type, CascadeStyle style, Object anything, boolean isCascadeDeleteEnabled) {
      if (!type.isEntityType() && !type.isAnyType()) {
         if (type.isCollectionType()) {
            this.cascadeCollection(parent, child, style, anything, (CollectionType)type);
         }
      } else {
         this.cascadeToOne(parent, child, type, style, anything, isCascadeDeleteEnabled);
      }

   }

   private void cascadeCollection(Object parent, Object child, CascadeStyle style, Object anything, CollectionType type) {
      CollectionPersister persister = this.eventSource.getFactory().getCollectionPersister(type.getRole());
      Type elemType = persister.getElementType();
      int oldCascadeTo = this.cascadeTo;
      if (this.cascadeTo == 1) {
         this.cascadeTo = 3;
      }

      if (elemType.isEntityType() || elemType.isAnyType() || elemType.isComponentType()) {
         this.cascadeCollectionElements(parent, child, type, style, elemType, anything, persister.isCascadeDeleteEnabled());
      }

      this.cascadeTo = oldCascadeTo;
   }

   private void cascadeToOne(Object parent, Object child, Type type, CascadeStyle style, Object anything, boolean isCascadeDeleteEnabled) {
      String entityName = type.isEntityType() ? ((EntityType)type).getAssociatedEntityName() : null;
      if (style.reallyDoCascade(this.action)) {
         this.eventSource.getPersistenceContext().addChildParent(child, parent);

         try {
            this.action.cascade(this.eventSource, child, entityName, anything, isCascadeDeleteEnabled);
         } finally {
            this.eventSource.getPersistenceContext().removeChildParent(child);
         }
      }

   }

   private void cascadeCollectionElements(Object parent, Object child, CollectionType collectionType, CascadeStyle style, Type elemType, Object anything, boolean isCascadeDeleteEnabled) throws HibernateException {
      boolean reallyDoCascade = style.reallyDoCascade(this.action) && child != CollectionType.UNFETCHED_COLLECTION;
      if (reallyDoCascade) {
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Cascade {0} for collection: {1}", this.action, collectionType.getRole());
         }

         Iterator iter = this.action.getCascadableChildrenIterator(this.eventSource, collectionType, child);

         while(iter.hasNext()) {
            this.cascadeProperty(parent, iter.next(), elemType, style, (String)null, anything, isCascadeDeleteEnabled);
         }

         if (LOG.isTraceEnabled()) {
            LOG.tracev("Done cascade {0} for collection: {1}", this.action, collectionType.getRole());
         }
      }

      boolean deleteOrphans = style.hasOrphanDelete() && this.action.deleteOrphans() && elemType.isEntityType() && child instanceof PersistentCollection;
      if (deleteOrphans) {
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Deleting orphans for collection: {0}", collectionType.getRole());
         }

         String entityName = collectionType.getAssociatedEntityName(this.eventSource.getFactory());
         this.deleteOrphans(entityName, (PersistentCollection)child);
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Done deleting orphans for collection: {0}", collectionType.getRole());
         }
      }

   }

   private void deleteOrphans(String entityName, PersistentCollection pc) throws HibernateException {
      Collection orphans;
      if (pc.wasInitialized()) {
         CollectionEntry ce = this.eventSource.getPersistenceContext().getCollectionEntry(pc);
         orphans = (Collection)(ce == null ? java.util.Collections.EMPTY_LIST : ce.getOrphans(entityName, pc));
      } else {
         orphans = pc.getQueuedOrphans(entityName);
      }

      for(Object orphan : orphans) {
         if (orphan != null) {
            LOG.tracev("Deleting orphaned entity instance: {0}", entityName);
            this.eventSource.delete(entityName, orphan, false, new HashSet());
         }
      }

   }
}
