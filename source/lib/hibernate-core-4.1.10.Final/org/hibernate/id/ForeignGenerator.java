package org.hibernate.id;

import java.io.Serializable;
import java.util.Properties;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.TransientObjectException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

public class ForeignGenerator implements IdentifierGenerator, Configurable {
   private String entityName;
   private String propertyName;

   public String getEntityName() {
      return this.entityName;
   }

   public String getPropertyName() {
      return this.propertyName;
   }

   public String getRole() {
      return this.getEntityName() + '.' + this.getPropertyName();
   }

   public void configure(Type type, Properties params, Dialect d) {
      this.propertyName = params.getProperty("property");
      this.entityName = params.getProperty("entity_name");
      if (this.propertyName == null) {
         throw new MappingException("param named \"property\" is required for foreign id generation strategy");
      }
   }

   public Serializable generate(SessionImplementor sessionImplementor, Object object) {
      Session session = (Session)sessionImplementor;
      EntityPersister persister = sessionImplementor.getFactory().getEntityPersister(this.entityName);
      Object associatedObject = persister.getPropertyValue(object, this.propertyName);
      if (associatedObject == null) {
         throw new IdentifierGenerationException("attempted to assign id from null one-to-one property [" + this.getRole() + "]");
      } else {
         Type propertyType = persister.getPropertyType(this.propertyName);
         EntityType foreignValueSourceType;
         if (propertyType.isEntityType()) {
            foreignValueSourceType = (EntityType)propertyType;
         } else {
            foreignValueSourceType = (EntityType)persister.getPropertyType("_identifierMapper." + this.propertyName);
         }

         Serializable id;
         try {
            id = ForeignKeys.getEntityIdentifierIfNotUnsaved(foreignValueSourceType.getAssociatedEntityName(), associatedObject, sessionImplementor);
         } catch (TransientObjectException var10) {
            id = session.save(foreignValueSourceType.getAssociatedEntityName(), associatedObject);
         }

         return session.contains(object) ? IdentifierGeneratorHelper.SHORT_CIRCUIT_INDICATOR : id;
      }
   }
}
