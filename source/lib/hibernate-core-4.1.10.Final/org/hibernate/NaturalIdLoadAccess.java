package org.hibernate;

public interface NaturalIdLoadAccess {
   NaturalIdLoadAccess with(LockOptions var1);

   NaturalIdLoadAccess using(String var1, Object var2);

   NaturalIdLoadAccess setSynchronizationEnabled(boolean var1);

   Object getReference();

   Object load();
}
