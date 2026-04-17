package org.hibernate.loader.custom;

import org.hibernate.LockMode;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.loader.EntityAliases;

public class CollectionFetchReturn extends FetchReturn {
   private final CollectionAliases collectionAliases;
   private final EntityAliases elementEntityAliases;

   public CollectionFetchReturn(String alias, NonScalarReturn owner, String ownerProperty, CollectionAliases collectionAliases, EntityAliases elementEntityAliases, LockMode lockMode) {
      super(owner, ownerProperty, alias, lockMode);
      this.collectionAliases = collectionAliases;
      this.elementEntityAliases = elementEntityAliases;
   }

   public CollectionAliases getCollectionAliases() {
      return this.collectionAliases;
   }

   public EntityAliases getElementEntityAliases() {
      return this.elementEntityAliases;
   }
}
