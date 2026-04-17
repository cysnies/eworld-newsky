package org.hibernate.engine.profile;

import org.hibernate.persister.entity.EntityPersister;

public class Association {
   private final EntityPersister owner;
   private final String associationPath;
   private final String role;

   public Association(EntityPersister owner, String associationPath) {
      this.owner = owner;
      this.associationPath = associationPath;
      this.role = owner.getEntityName() + '.' + associationPath;
   }

   public EntityPersister getOwner() {
      return this.owner;
   }

   public String getAssociationPath() {
      return this.associationPath;
   }

   public String getRole() {
      return this.role;
   }
}
