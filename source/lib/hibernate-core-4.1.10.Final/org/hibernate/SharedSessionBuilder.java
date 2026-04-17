package org.hibernate;

import java.sql.Connection;

public interface SharedSessionBuilder extends SessionBuilder {
   SharedSessionBuilder interceptor();

   SharedSessionBuilder connection();

   SharedSessionBuilder connectionReleaseMode();

   SharedSessionBuilder autoJoinTransactions();

   /** @deprecated */
   @Deprecated
   SharedSessionBuilder autoClose();

   SharedSessionBuilder flushBeforeCompletion();

   SharedSessionBuilder transactionContext();

   SharedSessionBuilder interceptor(Interceptor var1);

   SharedSessionBuilder noInterceptor();

   SharedSessionBuilder connection(Connection var1);

   SharedSessionBuilder connectionReleaseMode(ConnectionReleaseMode var1);

   SharedSessionBuilder autoJoinTransactions(boolean var1);

   SharedSessionBuilder autoClose(boolean var1);

   SharedSessionBuilder flushBeforeCompletion(boolean var1);
}
