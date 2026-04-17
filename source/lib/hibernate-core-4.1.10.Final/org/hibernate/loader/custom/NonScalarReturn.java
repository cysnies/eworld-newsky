package org.hibernate.loader.custom;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;

public abstract class NonScalarReturn implements Return {
   private final String alias;
   private final LockMode lockMode;

   public NonScalarReturn(String alias, LockMode lockMode) {
      this.alias = alias;
      if (alias == null) {
         throw new HibernateException("alias must be specified");
      } else {
         this.lockMode = lockMode;
      }
   }

   public String getAlias() {
      return this.alias;
   }

   public LockMode getLockMode() {
      return this.lockMode;
   }
}
