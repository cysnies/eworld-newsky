package org.hibernate.mapping;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;

public interface RelationalModel {
   String sqlCreateString(Dialect var1, Mapping var2, String var3, String var4) throws HibernateException;

   String sqlDropString(Dialect var1, String var2, String var3);
}
