package org.hibernate;

import java.io.Serializable;

public interface IdentifierLoadAccess {
   IdentifierLoadAccess with(LockOptions var1);

   Object getReference(Serializable var1);

   Object load(Serializable var1);
}
