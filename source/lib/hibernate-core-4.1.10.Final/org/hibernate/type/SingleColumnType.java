package org.hibernate.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

public interface SingleColumnType extends Type {
   int sqlType();

   String toString(Object var1) throws HibernateException;

   Object fromStringValue(String var1) throws HibernateException;

   Object nullSafeGet(ResultSet var1, String var2, SessionImplementor var3) throws HibernateException, SQLException;

   Object get(ResultSet var1, String var2, SessionImplementor var3) throws HibernateException, SQLException;

   void set(PreparedStatement var1, Object var2, int var3, SessionImplementor var4) throws HibernateException, SQLException;
}
