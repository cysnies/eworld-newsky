package org.hibernate.event.internal;

import org.hibernate.HibernateException;
import org.hibernate.bytecode.instrumentation.spi.LazyPropertyInitializer;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

public abstract class AbstractVisitor {
   private final EventSource session;

   AbstractVisitor(EventSource session) {
      this.session = session;
   }

   void processValues(Object[] values, Type[] types) throws HibernateException {
      for(int i = 0; i < types.length; ++i) {
         if (this.includeProperty(values, i)) {
            this.processValue(i, values, types);
         }
      }

   }

   public void processEntityPropertyValues(Object[] values, Type[] types) throws HibernateException {
      for(int i = 0; i < types.length; ++i) {
         if (this.includeEntityProperty(values, i)) {
            this.processValue(i, values, types);
         }
      }

   }

   void processValue(int i, Object[] values, Type[] types) {
      this.processValue(values[i], types[i]);
   }

   boolean includeEntityProperty(Object[] values, int i) {
      return this.includeProperty(values, i);
   }

   boolean includeProperty(Object[] values, int i) {
      return values[i] != LazyPropertyInitializer.UNFETCHED_PROPERTY;
   }

   Object processComponent(Object component, CompositeType componentType) throws HibernateException {
      if (component != null) {
         this.processValues(componentType.getPropertyValues(component, (SessionImplementor)this.session), componentType.getSubtypes());
      }

      return null;
   }

   final Object processValue(Object value, Type type) throws HibernateException {
      if (type.isCollectionType()) {
         return this.processCollection(value, (CollectionType)type);
      } else if (type.isEntityType()) {
         return this.processEntity(value, (EntityType)type);
      } else {
         return type.isComponentType() ? this.processComponent(value, (CompositeType)type) : null;
      }
   }

   void process(Object object, EntityPersister persister) throws HibernateException {
      this.processEntityPropertyValues(persister.getPropertyValues(object), persister.getPropertyTypes());
   }

   Object processCollection(Object collection, CollectionType type) throws HibernateException {
      return null;
   }

   Object processEntity(Object value, EntityType entityType) throws HibernateException {
      return null;
   }

   final EventSource getSession() {
      return this.session;
   }
}
