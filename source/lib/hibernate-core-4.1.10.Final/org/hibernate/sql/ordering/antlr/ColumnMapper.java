package org.hibernate.sql.ordering.antlr;

import org.hibernate.HibernateException;

public interface ColumnMapper {
   SqlValueReference[] map(String var1) throws HibernateException;
}
