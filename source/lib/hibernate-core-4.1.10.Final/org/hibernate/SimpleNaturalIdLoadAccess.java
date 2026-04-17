package org.hibernate;

public interface SimpleNaturalIdLoadAccess {
   SimpleNaturalIdLoadAccess with(LockOptions var1);

   SimpleNaturalIdLoadAccess setSynchronizationEnabled(boolean var1);

   Object getReference(Object var1);

   Object load(Object var1);
}
