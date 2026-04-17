package org.hibernate.engine.transaction.spi;

public enum JoinStatus {
   NOT_JOINED,
   MARKED_FOR_JOINED,
   JOINED;
}
