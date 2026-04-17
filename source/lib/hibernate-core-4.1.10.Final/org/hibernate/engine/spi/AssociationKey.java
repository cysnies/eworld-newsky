package org.hibernate.engine.spi;

import java.io.Serializable;

public final class AssociationKey implements Serializable {
   private EntityKey ownerKey;
   private String propertyName;

   public AssociationKey(EntityKey ownerKey, String propertyName) {
      this.ownerKey = ownerKey;
      this.propertyName = propertyName;
   }

   public boolean equals(Object that) {
      AssociationKey key = (AssociationKey)that;
      return key.propertyName.equals(this.propertyName) && key.ownerKey.equals(this.ownerKey);
   }

   public int hashCode() {
      return this.ownerKey.hashCode() + this.propertyName.hashCode();
   }
}
