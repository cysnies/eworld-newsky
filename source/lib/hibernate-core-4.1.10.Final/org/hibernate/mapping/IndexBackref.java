package org.hibernate.mapping;

import org.hibernate.property.IndexPropertyAccessor;
import org.hibernate.property.PropertyAccessor;

public class IndexBackref extends Property {
   private String collectionRole;
   private String entityName;

   public boolean isBackRef() {
      return true;
   }

   public boolean isSynthetic() {
      return true;
   }

   public String getCollectionRole() {
      return this.collectionRole;
   }

   public void setCollectionRole(String collectionRole) {
      this.collectionRole = collectionRole;
   }

   public boolean isBasicPropertyAccessor() {
      return false;
   }

   public PropertyAccessor getPropertyAccessor(Class clazz) {
      return new IndexPropertyAccessor(this.collectionRole, this.entityName);
   }

   public String getEntityName() {
      return this.entityName;
   }

   public void setEntityName(String entityName) {
      this.entityName = entityName;
   }
}
