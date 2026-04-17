package org.hibernate.annotations;

public enum CascadeType {
   ALL,
   PERSIST,
   MERGE,
   REMOVE,
   REFRESH,
   DELETE,
   SAVE_UPDATE,
   REPLICATE,
   /** @deprecated */
   @Deprecated
   DELETE_ORPHAN,
   LOCK,
   /** @deprecated */
   @Deprecated
   EVICT,
   DETACH;
}
