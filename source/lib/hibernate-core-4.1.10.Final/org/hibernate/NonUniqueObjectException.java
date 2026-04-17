package org.hibernate;

import java.io.Serializable;
import org.hibernate.pretty.MessageHelper;

public class NonUniqueObjectException extends HibernateException {
   private final Serializable identifier;
   private final String entityName;

   public NonUniqueObjectException(String message, Serializable id, String clazz) {
      super(message);
      this.entityName = clazz;
      this.identifier = id;
   }

   public NonUniqueObjectException(Serializable id, String clazz) {
      this("a different object with the same identifier value was already associated with the session", id, clazz);
   }

   public Serializable getIdentifier() {
      return this.identifier;
   }

   public String getMessage() {
      return super.getMessage() + ": " + MessageHelper.infoString(this.entityName, this.identifier);
   }

   public String getEntityName() {
      return this.entityName;
   }
}
