package org.hibernate.loader.custom;

import org.hibernate.LockMode;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.loader.EntityAliases;

public class CollectionReturn extends NonScalarReturn {
   private final String ownerEntityName;
   private final String ownerProperty;
   private final CollectionAliases collectionAliases;
   private final EntityAliases elementEntityAliases;

   public CollectionReturn(String alias, String ownerEntityName, String ownerProperty, CollectionAliases collectionAliases, EntityAliases elementEntityAliases, LockMode lockMode) {
      super(alias, lockMode);
      this.ownerEntityName = ownerEntityName;
      this.ownerProperty = ownerProperty;
      this.collectionAliases = collectionAliases;
      this.elementEntityAliases = elementEntityAliases;
   }

   public String getOwnerEntityName() {
      return this.ownerEntityName;
   }

   public String getOwnerProperty() {
      return this.ownerProperty;
   }

   public CollectionAliases getCollectionAliases() {
      return this.collectionAliases;
   }

   public EntityAliases getElementEntityAliases() {
      return this.elementEntityAliases;
   }
}
