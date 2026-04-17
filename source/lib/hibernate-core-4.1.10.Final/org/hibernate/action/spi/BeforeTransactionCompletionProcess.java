package org.hibernate.action.spi;

import org.hibernate.engine.spi.SessionImplementor;

public interface BeforeTransactionCompletionProcess {
   void doBeforeTransactionCompletion(SessionImplementor var1);
}
