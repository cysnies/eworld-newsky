package org.hibernate.engine.internal;

import java.io.Serializable;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.TransientObjectException;
import org.hibernate.bytecode.instrumentation.spi.LazyPropertyInitializer;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

public final class ForeignKeys {
   private ForeignKeys() {
   }

   public static boolean isNotTransient(String entityName, Object entity, Boolean assumed, SessionImplementor session) throws HibernateException {
      if (entity instanceof HibernateProxy) {
         return true;
      } else if (session.getPersistenceContext().isEntryFor(entity)) {
         return true;
      } else {
         return !isTransient(entityName, entity, assumed, session);
      }
   }

   public static boolean isTransient(String entityName, Object entity, Boolean assumed, SessionImplementor session) throws HibernateException {
      if (entity == LazyPropertyInitializer.UNFETCHED_PROPERTY) {
         return false;
      } else {
         Boolean isUnsaved = session.getInterceptor().isTransient(entity);
         if (isUnsaved != null) {
            return isUnsaved;
         } else {
            EntityPersister persister = session.getEntityPersister(entityName, entity);
            isUnsaved = persister.isTransient(entity, session);
            if (isUnsaved != null) {
               return isUnsaved;
            } else if (assumed != null) {
               return assumed;
            } else {
               Object[] snapshot = session.getPersistenceContext().getDatabaseSnapshot(persister.getIdentifier(entity, session), persister);
               return snapshot == null;
            }
         }
      }
   }

   public static Serializable getEntityIdentifierIfNotUnsaved(String entityName, Object object, SessionImplementor session) throws HibernateException {
      if (object == null) {
         return null;
      } else {
         Serializable id = session.getContextEntityIdentifier(object);
         if (id == null) {
            if (isTransient(entityName, object, Boolean.FALSE, session)) {
               throw new TransientObjectException("object references an unsaved transient instance - save the transient instance before flushing: " + (entityName == null ? session.guessEntityName(object) : entityName));
            }

            id = session.getEntityPersister(entityName, object).getIdentifier(object, session);
         }

         return id;
      }
   }

   public static NonNullableTransientDependencies findNonNullableTransientEntities(String entityName, Object entity, Object[] values, boolean isEarlyInsert, SessionImplementor session) {
      Nullifier nullifier = new Nullifier(entity, false, isEarlyInsert, session);
      EntityPersister persister = session.getEntityPersister(entityName, entity);
      String[] propertyNames = persister.getPropertyNames();
      Type[] types = persister.getPropertyTypes();
      boolean[] nullability = persister.getPropertyNullability();
      NonNullableTransientDependencies nonNullableTransientEntities = new NonNullableTransientDependencies();

      for(int i = 0; i < types.length; ++i) {
         collectNonNullableTransientEntities(nullifier, i, values[i], propertyNames[i], types[i], nullability[i], session, nonNullableTransientEntities);
      }

      return nonNullableTransientEntities.isEmpty() ? null : nonNullableTransientEntities;
   }

   private static void collectNonNullableTransientEntities(Nullifier nullifier, int i, Object value, String propertyName, Type type, boolean isNullable, SessionImplementor session, NonNullableTransientDependencies nonNullableTransientEntities) {
      if (value != null) {
         if (type.isEntityType()) {
            EntityType entityType = (EntityType)type;
            if (!isNullable && !entityType.isOneToOne() && nullifier.isNullifiable(entityType.getAssociatedEntityName(), value)) {
               nonNullableTransientEntities.add(propertyName, value);
            }
         } else if (type.isAnyType()) {
            if (!isNullable && nullifier.isNullifiable((String)null, value)) {
               nonNullableTransientEntities.add(propertyName, value);
            }
         } else if (type.isComponentType()) {
            CompositeType actype = (CompositeType)type;
            boolean[] subValueNullability = actype.getPropertyNullability();
            if (subValueNullability != null) {
               String[] subPropertyNames = actype.getPropertyNames();
               Object[] subvalues = actype.getPropertyValues(value, session);
               Type[] subtypes = actype.getSubtypes();

               for(int j = 0; j < subvalues.length; ++j) {
                  collectNonNullableTransientEntities(nullifier, j, subvalues[j], subPropertyNames[j], subtypes[j], subValueNullability[j], session, nonNullableTransientEntities);
               }
            }
         }

      }
   }

   public static class Nullifier {
      private final boolean isDelete;
      private final boolean isEarlyInsert;
      private final SessionImplementor session;
      private final Object self;

      public Nullifier(Object self, boolean isDelete, boolean isEarlyInsert, SessionImplementor session) {
         this.isDelete = isDelete;
         this.isEarlyInsert = isEarlyInsert;
         this.session = session;
         this.self = self;
      }

      public void nullifyTransientReferences(Object[] values, Type[] types) throws HibernateException {
         for(int i = 0; i < types.length; ++i) {
            values[i] = this.nullifyTransientReferences(values[i], types[i]);
         }

      }

      private Object nullifyTransientReferences(Object value, Type type) throws HibernateException {
         if (value == null) {
            return null;
         } else if (type.isEntityType()) {
            EntityType entityType = (EntityType)type;
            if (entityType.isOneToOne()) {
               return value;
            } else {
               String entityName = entityType.getAssociatedEntityName();
               return this.isNullifiable(entityName, value) ? null : value;
            }
         } else if (type.isAnyType()) {
            return this.isNullifiable((String)null, value) ? null : value;
         } else if (type.isComponentType()) {
            CompositeType actype = (CompositeType)type;
            Object[] subvalues = actype.getPropertyValues(value, this.session);
            Type[] subtypes = actype.getSubtypes();
            boolean substitute = false;

            for(int i = 0; i < subvalues.length; ++i) {
               Object replacement = this.nullifyTransientReferences(subvalues[i], subtypes[i]);
               if (replacement != subvalues[i]) {
                  substitute = true;
                  subvalues[i] = replacement;
               }
            }

            if (substitute) {
               actype.setPropertyValues(value, subvalues, EntityMode.POJO);
            }

            return value;
         } else {
            return value;
         }
      }

      private boolean isNullifiable(String entityName, Object object) throws HibernateException {
         if (object == LazyPropertyInitializer.UNFETCHED_PROPERTY) {
            return false;
         } else {
            if (object instanceof HibernateProxy) {
               LazyInitializer li = ((HibernateProxy)object).getHibernateLazyInitializer();
               if (li.getImplementation(this.session) == null) {
                  return false;
               }

               object = li.getImplementation();
            }

            if (object != this.self) {
               EntityEntry entityEntry = this.session.getPersistenceContext().getEntry(object);
               return entityEntry == null ? ForeignKeys.isTransient(entityName, object, (Boolean)null, this.session) : entityEntry.isNullifiable(this.isEarlyInsert, this.session);
            } else {
               return this.isEarlyInsert || this.isDelete && this.session.getFactory().getDialect().hasSelfReferentialForeignKeyBug();
            }
         }
      }
   }
}
