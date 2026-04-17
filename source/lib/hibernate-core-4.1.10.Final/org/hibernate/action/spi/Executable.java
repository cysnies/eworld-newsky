package org.hibernate.action.spi;

import java.io.Serializable;
import org.hibernate.HibernateException;

public interface Executable {
   Serializable[] getPropertySpaces();

   void beforeExecutions() throws HibernateException;

   void execute() throws HibernateException;

   AfterTransactionCompletionProcess getAfterTransactionCompletionProcess();

   BeforeTransactionCompletionProcess getBeforeTransactionCompletionProcess();
}
