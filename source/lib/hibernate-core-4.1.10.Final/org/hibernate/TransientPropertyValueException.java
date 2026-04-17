package org.hibernate;

import org.hibernate.internal.util.StringHelper;

public class TransientPropertyValueException extends TransientObjectException {
   private final String transientEntityName;
   private final String propertyOwnerEntityName;
   private final String propertyName;

   public TransientPropertyValueException(String message, String transientEntityName, String propertyOwnerEntityName, String propertyName) {
      super(message);
      this.transientEntityName = transientEntityName;
      this.propertyOwnerEntityName = propertyOwnerEntityName;
      this.propertyName = propertyName;
   }

   public String getTransientEntityName() {
      return this.transientEntityName;
   }

   public String getPropertyOwnerEntityName() {
      return this.propertyOwnerEntityName;
   }

   public String getPropertyName() {
      return this.propertyName;
   }

   public String getMessage() {
      return super.getMessage() + ": " + StringHelper.qualify(this.propertyOwnerEntityName, this.propertyName) + " -> " + this.transientEntityName;
   }
}
