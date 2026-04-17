package org.hibernate.engine.transaction.spi;

import org.hibernate.HibernateException;
import org.hibernate.jdbc.WorkExecutorVisitable;

public interface IsolationDelegate {
   Object delegateWork(WorkExecutorVisitable var1, boolean var2) throws HibernateException;
}
