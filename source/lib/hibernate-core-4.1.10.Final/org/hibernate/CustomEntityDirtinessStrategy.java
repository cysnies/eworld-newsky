package org.hibernate;

import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;

public interface CustomEntityDirtinessStrategy {
   boolean canDirtyCheck(Object var1, EntityPersister var2, Session var3);

   boolean isDirty(Object var1, EntityPersister var2, Session var3);

   void resetDirty(Object var1, EntityPersister var2, Session var3);

   void findDirty(Object var1, EntityPersister var2, Session var3, DirtyCheckContext var4);

   public interface AttributeChecker {
      boolean isDirty(AttributeInformation var1);
   }

   public interface AttributeInformation {
      EntityPersister getContainingPersister();

      int getAttributeIndex();

      String getName();

      Type getType();

      Object getCurrentValue();

      Object getLoadedValue();
   }

   public interface DirtyCheckContext {
      void doDirtyChecking(AttributeChecker var1);
   }
}
