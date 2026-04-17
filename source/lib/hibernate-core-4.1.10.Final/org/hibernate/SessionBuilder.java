package org.hibernate;

import java.sql.Connection;

public interface SessionBuilder {
   Session openSession();

   SessionBuilder interceptor(Interceptor var1);

   SessionBuilder noInterceptor();

   SessionBuilder connection(Connection var1);

   SessionBuilder connectionReleaseMode(ConnectionReleaseMode var1);

   SessionBuilder autoJoinTransactions(boolean var1);

   /** @deprecated */
   @Deprecated
   SessionBuilder autoClose(boolean var1);

   SessionBuilder flushBeforeCompletion(boolean var1);

   SessionBuilder tenantIdentifier(String var1);
}
