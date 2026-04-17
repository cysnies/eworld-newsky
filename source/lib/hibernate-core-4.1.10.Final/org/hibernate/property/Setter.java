package org.hibernate.property;

import java.io.Serializable;
import java.lang.reflect.Method;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;

public interface Setter extends Serializable {
   void set(Object var1, Object var2, SessionFactoryImplementor var3) throws HibernateException;

   String getMethodName();

   Method getMethod();
}
