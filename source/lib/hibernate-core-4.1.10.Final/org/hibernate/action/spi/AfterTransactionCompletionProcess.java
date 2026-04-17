package org.hibernate.action.spi;

import org.hibernate.engine.spi.SessionImplementor;

public interface AfterTransactionCompletionProcess {
   void doAfterTransactionCompletion(boolean var1, SessionImplementor var2);
}
