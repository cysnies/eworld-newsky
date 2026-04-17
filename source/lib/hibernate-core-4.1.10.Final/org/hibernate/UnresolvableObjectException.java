package org.hibernate;

import java.io.Serializable;
import org.hibernate.pretty.MessageHelper;

public class UnresolvableObjectException extends HibernateException {
   private final Serializable identifier;
   private final String entityName;

   public UnresolvableObjectException(Serializable identifier, String clazz) {
      this("No row with the given identifier exists", identifier, clazz);
   }

   UnresolvableObjectException(String message, Serializable identifier, String clazz) {
      super(message);
      this.identifier = identifier;
      this.entityName = clazz;
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

   public static void throwIfNull(Object o, Serializable id, String clazz) throws UnresolvableObjectException {
      if (o == null) {
         throw new UnresolvableObjectException(id, clazz);
      }
   }
}
