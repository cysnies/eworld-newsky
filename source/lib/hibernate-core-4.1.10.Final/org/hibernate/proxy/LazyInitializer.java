package org.hibernate.proxy;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

public interface LazyInitializer {
   void initialize() throws HibernateException;

   Serializable getIdentifier();

   void setIdentifier(Serializable var1);

   String getEntityName();

   Class getPersistentClass();

   boolean isUninitialized();

   Object getImplementation();

   Object getImplementation(SessionImplementor var1) throws HibernateException;

   void setImplementation(Object var1);

   boolean isReadOnlySettingAvailable();

   boolean isReadOnly();

   void setReadOnly(boolean var1);

   SessionImplementor getSession();

   void setSession(SessionImplementor var1) throws HibernateException;

   void unsetSession();

   void setUnwrap(boolean var1);

   boolean isUnwrap();
}
