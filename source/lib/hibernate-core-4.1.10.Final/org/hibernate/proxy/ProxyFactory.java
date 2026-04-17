package org.hibernate.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.CompositeType;

public interface ProxyFactory {
   void postInstantiate(String var1, Class var2, Set var3, Method var4, Method var5, CompositeType var6) throws HibernateException;

   HibernateProxy getProxy(Serializable var1, SessionImplementor var2) throws HibernateException;
}
