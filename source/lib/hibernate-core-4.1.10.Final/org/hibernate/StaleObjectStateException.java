package org.hibernate;

import java.io.Serializable;
import org.hibernate.pretty.MessageHelper;

public class StaleObjectStateException extends StaleStateException {
   private final String entityName;
   private final Serializable identifier;

   public StaleObjectStateException(String persistentClass, Serializable identifier) {
      super("Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect)");
      this.entityName = persistentClass;
      this.identifier = identifier;
   }

   public String getEntityName() {
      return this.entityName;
   }

   public Serializable getIdentifier() {
      return this.identifier;
   }

   public String getMessage() {
      return super.getMessage() + ": " + MessageHelper.infoString(this.entityName, this.identifier);
   }
}
