package org.hibernate.internal.util;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.bytecode.instrumentation.spi.LazyPropertyInitializer;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public final class EntityPrinter {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, EntityPrinter.class.getName());
   private SessionFactoryImplementor factory;

   public String toString(String entityName, Object entity) throws HibernateException {
      EntityPersister entityPersister = this.factory.getEntityPersister(entityName);
      if (entityPersister == null) {
         return entity.getClass().getName();
      } else {
         Map<String, String> result = new HashMap();
         if (entityPersister.hasIdentifierProperty()) {
            result.put(entityPersister.getIdentifierPropertyName(), entityPersister.getIdentifierType().toLoggableString(entityPersister.getIdentifier(entity), this.factory));
         }

         Type[] types = entityPersister.getPropertyTypes();
         String[] names = entityPersister.getPropertyNames();
         Object[] values = entityPersister.getPropertyValues(entity);

         for(int i = 0; i < types.length; ++i) {
            if (!names[i].startsWith("_")) {
               String strValue = values[i] == LazyPropertyInitializer.UNFETCHED_PROPERTY ? values[i].toString() : types[i].toLoggableString(values[i], this.factory);
               result.put(names[i], strValue);
            }
         }

         return entityName + result.toString();
      }
   }

   public String toString(Type[] types, Object[] values) throws HibernateException {
      StringBuilder buffer = new StringBuilder();

      for(int i = 0; i < types.length; ++i) {
         if (types[i] != null) {
            buffer.append(types[i].toLoggableString(values[i], this.factory)).append(", ");
         }
      }

      return buffer.toString();
   }

   public String toString(Map namedTypedValues) throws HibernateException {
      Map<String, String> result = new HashMap();

      for(Map.Entry entry : namedTypedValues.entrySet()) {
         result.put(entry.getKey(), ((TypedValue)entry.getValue()).getType().toLoggableString(((TypedValue)entry.getValue()).getValue(), this.factory));
      }

      return result.toString();
   }

   public void toString(Iterable entitiesByEntityKey) throws HibernateException {
      if (LOG.isDebugEnabled() && entitiesByEntityKey.iterator().hasNext()) {
         LOG.debug("Listing entities:");
         int i = 0;

         for(Map.Entry entityKeyAndEntity : entitiesByEntityKey) {
            if (i++ > 20) {
               LOG.debug("More......");
               break;
            }

            LOG.debug(this.toString(((EntityKey)entityKeyAndEntity.getKey()).getEntityName(), entityKeyAndEntity.getValue()));
         }

      }
   }

   public EntityPrinter(SessionFactoryImplementor factory) {
      this.factory = factory;
   }
}
