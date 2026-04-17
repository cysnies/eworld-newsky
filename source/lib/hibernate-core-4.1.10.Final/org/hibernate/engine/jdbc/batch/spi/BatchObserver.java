package org.hibernate.engine.jdbc.batch.spi;

public interface BatchObserver {
   void batchExplicitlyExecuted();

   void batchImplicitlyExecuted();
}
