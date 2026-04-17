package org.hibernate.engine.transaction.spi;

public enum LocalStatus {
   NOT_ACTIVE,
   ACTIVE,
   COMMITTED,
   ROLLED_BACK,
   FAILED_COMMIT;
}
