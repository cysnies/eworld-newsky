package org.hibernate.annotations;

public enum OptimisticLockType {
   NONE,
   VERSION,
   DIRTY,
   ALL;
}
