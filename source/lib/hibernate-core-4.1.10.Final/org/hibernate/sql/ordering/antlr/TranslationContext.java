package org.hibernate.sql.ordering.antlr;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionRegistry;
import org.hibernate.engine.spi.SessionFactoryImplementor;

public interface TranslationContext {
   SessionFactoryImplementor getSessionFactory();

   Dialect getDialect();

   SQLFunctionRegistry getSqlFunctionRegistry();

   ColumnMapper getColumnMapper();
}
