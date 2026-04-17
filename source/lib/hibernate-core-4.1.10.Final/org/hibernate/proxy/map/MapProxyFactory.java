package org.hibernate.proxy.map;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.type.CompositeType;

public class MapProxyFactory implements ProxyFactory {
   private String entityName;

   public void postInstantiate(String entityName, Class persistentClass, Set interfaces, Method getIdentifierMethod, Method setIdentifierMethod, CompositeType componentIdType) throws HibernateException {
      this.entityName = entityName;
   }

   public HibernateProxy getProxy(Serializable id, SessionImplementor session) throws HibernateException {
      return new MapProxy(new MapLazyInitializer(this.entityName, id, session));
   }
}
