package org.hibernate.annotations;

public enum FlushModeType {
   ALWAYS,
   AUTO,
   COMMIT,
   /** @deprecated */
   NEVER,
   MANUAL,
   PERSISTENCE_CONTEXT;
}
