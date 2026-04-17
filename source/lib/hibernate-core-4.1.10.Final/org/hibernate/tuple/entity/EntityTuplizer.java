package org.hibernate.tuple.entity;

import java.io.Serializable;
import java.util.Map;
import org.hibernate.EntityMode;
import org.hibernate.EntityNameResolver;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.property.Getter;
import org.hibernate.tuple.Tuplizer;

public interface EntityTuplizer extends Tuplizer {
   EntityMode getEntityMode();

   /** @deprecated */
   Object instantiate(Serializable var1) throws HibernateException;

   Object instantiate(Serializable var1, SessionImplementor var2);

   /** @deprecated */
   Serializable getIdentifier(Object var1) throws HibernateException;

   Serializable getIdentifier(Object var1, SessionImplementor var2);

   /** @deprecated */
   void setIdentifier(Object var1, Serializable var2) throws HibernateException;

   void setIdentifier(Object var1, Serializable var2, SessionImplementor var3);

   /** @deprecated */
   void resetIdentifier(Object var1, Serializable var2, Object var3);

   void resetIdentifier(Object var1, Serializable var2, Object var3, SessionImplementor var4);

   Object getVersion(Object var1) throws HibernateException;

   void setPropertyValue(Object var1, int var2, Object var3) throws HibernateException;

   void setPropertyValue(Object var1, String var2, Object var3) throws HibernateException;

   Object[] getPropertyValuesToInsert(Object var1, Map var2, SessionImplementor var3) throws HibernateException;

   Object getPropertyValue(Object var1, String var2) throws HibernateException;

   void afterInitialize(Object var1, boolean var2, SessionImplementor var3);

   boolean hasProxy();

   Object createProxy(Serializable var1, SessionImplementor var2) throws HibernateException;

   boolean isLifecycleImplementor();

   Class getConcreteProxyClass();

   boolean hasUninitializedLazyProperties(Object var1);

   boolean isInstrumented();

   EntityNameResolver[] getEntityNameResolvers();

   String determineConcreteSubclassEntityName(Object var1, SessionFactoryImplementor var2);

   Getter getIdentifierGetter();

   Getter getVersionGetter();
}
