package org.hibernate.property;

import java.io.Serializable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

public interface Getter extends Serializable {
   Object get(Object var1) throws HibernateException;

   Object getForInsert(Object var1, Map var2, SessionImplementor var3) throws HibernateException;

   Member getMember();

   Class getReturnType();

   String getMethodName();

   Method getMethod();
}
