package org.hibernate.hql.internal.ast.exec;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;

public interface StatementExecutor {
   String[] getSqlStatements();

   int execute(QueryParameters var1, SessionImplementor var2) throws HibernateException;
}
