package org.hibernate.loader.custom;

import org.hibernate.LockMode;
import org.hibernate.loader.EntityAliases;

public class RootReturn extends NonScalarReturn {
   private final String entityName;
   private final EntityAliases entityAliases;

   public RootReturn(String alias, String entityName, EntityAliases entityAliases, LockMode lockMode) {
      super(alias, lockMode);
      this.entityName = entityName;
      this.entityAliases = entityAliases;
   }

   public String getEntityName() {
      return this.entityName;
   }

   public EntityAliases getEntityAliases() {
      return this.entityAliases;
   }
}
