package org.hibernate;

import java.io.Serializable;

public class WrongClassException extends HibernateException {
   private final Serializable identifier;
   private final String entityName;

   public WrongClassException(String msg, Serializable identifier, String clazz) {
      super(msg);
      this.identifier = identifier;
      this.entityName = clazz;
   }

   public Serializable getIdentifier() {
      return this.identifier;
   }

   public String getMessage() {
      return "Object with id: " + this.identifier + " was not of the specified subclass: " + this.entityName + " (" + super.getMessage() + ")";
   }

   public String getEntityName() {
      return this.entityName;
   }
}
