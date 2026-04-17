package org.hibernate.type;

import java.lang.reflect.Method;
import org.hibernate.EntityMode;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.SessionImplementor;

public interface CompositeType extends Type {
   Type[] getSubtypes();

   String[] getPropertyNames();

   boolean[] getPropertyNullability();

   Object[] getPropertyValues(Object var1, SessionImplementor var2) throws HibernateException;

   Object[] getPropertyValues(Object var1, EntityMode var2) throws HibernateException;

   Object getPropertyValue(Object var1, int var2, SessionImplementor var3) throws HibernateException;

   void setPropertyValues(Object var1, Object[] var2, EntityMode var3) throws HibernateException;

   CascadeStyle getCascadeStyle(int var1);

   FetchMode getFetchMode(int var1);

   boolean isMethodOf(Method var1);

   boolean isEmbedded();
}
