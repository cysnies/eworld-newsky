package org.hibernate.proxy.pojo.javassist;

import java.io.Serializable;
import java.lang.reflect.Method;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.AbstractSerializableProxy;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.CompositeType;

public final class SerializableProxy extends AbstractSerializableProxy {
   private Class persistentClass;
   private Class[] interfaces;
   private Class getIdentifierMethodClass;
   private Class setIdentifierMethodClass;
   private String getIdentifierMethodName;
   private String setIdentifierMethodName;
   private Class[] setIdentifierMethodParams;
   private CompositeType componentIdType;

   public SerializableProxy() {
   }

   public SerializableProxy(String entityName, Class persistentClass, Class[] interfaces, Serializable id, Boolean readOnly, Method getIdentifierMethod, Method setIdentifierMethod, CompositeType componentIdType) {
      super(entityName, id, readOnly);
      this.persistentClass = persistentClass;
      this.interfaces = interfaces;
      if (getIdentifierMethod != null) {
         this.getIdentifierMethodClass = getIdentifierMethod.getDeclaringClass();
         this.getIdentifierMethodName = getIdentifierMethod.getName();
      }

      if (setIdentifierMethod != null) {
         this.setIdentifierMethodClass = setIdentifierMethod.getDeclaringClass();
         this.setIdentifierMethodName = setIdentifierMethod.getName();
         this.setIdentifierMethodParams = setIdentifierMethod.getParameterTypes();
      }

      this.componentIdType = componentIdType;
   }

   private Object readResolve() {
      try {
         HibernateProxy proxy = JavassistLazyInitializer.getProxy(this.getEntityName(), this.persistentClass, this.interfaces, this.getIdentifierMethodName == null ? null : this.getIdentifierMethodClass.getDeclaredMethod(this.getIdentifierMethodName, (Class[])null), this.setIdentifierMethodName == null ? null : this.setIdentifierMethodClass.getDeclaredMethod(this.setIdentifierMethodName, this.setIdentifierMethodParams), this.componentIdType, this.getId(), (SessionImplementor)null);
         this.setReadOnlyBeforeAttachedToSession((JavassistLazyInitializer)proxy.getHibernateLazyInitializer());
         return proxy;
      } catch (NoSuchMethodException nsme) {
         throw new HibernateException("could not create proxy for entity: " + this.getEntityName(), nsme);
      }
   }
}
