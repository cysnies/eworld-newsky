package org.hibernate.usertype;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.Type;

public interface CompositeUserType {
   String[] getPropertyNames();

   Type[] getPropertyTypes();

   Object getPropertyValue(Object var1, int var2) throws HibernateException;

   void setPropertyValue(Object var1, int var2, Object var3) throws HibernateException;

   Class returnedClass();

   boolean equals(Object var1, Object var2) throws HibernateException;

   int hashCode(Object var1) throws HibernateException;

   Object nullSafeGet(ResultSet var1, String[] var2, SessionImplementor var3, Object var4) throws HibernateException, SQLException;

   void nullSafeSet(PreparedStatement var1, Object var2, int var3, SessionImplementor var4) throws HibernateException, SQLException;

   Object deepCopy(Object var1) throws HibernateException;

   boolean isMutable();

   Serializable disassemble(Object var1, SessionImplementor var2) throws HibernateException;

   Object assemble(Serializable var1, SessionImplementor var2, Object var3) throws HibernateException;

   Object replace(Object var1, Object var2, SessionImplementor var3, Object var4) throws HibernateException;
}
