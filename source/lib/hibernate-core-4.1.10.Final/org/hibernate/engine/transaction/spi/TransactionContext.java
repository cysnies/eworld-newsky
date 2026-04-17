package org.hibernate.engine.transaction.spi;

import java.io.Serializable;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.engine.jdbc.spi.JdbcConnectionAccess;

public interface TransactionContext extends Serializable {
   TransactionEnvironment getTransactionEnvironment();

   ConnectionReleaseMode getConnectionReleaseMode();

   boolean shouldAutoJoinTransaction();

   boolean isAutoCloseSessionEnabled();

   boolean isClosed();

   boolean isFlushModeNever();

   boolean isFlushBeforeCompletionEnabled();

   void managedFlush();

   boolean shouldAutoClose();

   void managedClose();

   void afterTransactionBegin(TransactionImplementor var1);

   void beforeTransactionCompletion(TransactionImplementor var1);

   void afterTransactionCompletion(TransactionImplementor var1, boolean var2);

   String onPrepareStatement(String var1);

   JdbcConnectionAccess getJdbcConnectionAccess();
}
