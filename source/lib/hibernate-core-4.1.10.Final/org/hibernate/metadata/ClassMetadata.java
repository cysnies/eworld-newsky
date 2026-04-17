package org.hibernate.metadata;

import java.io.Serializable;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.Type;

public interface ClassMetadata {
   String getEntityName();

   String getIdentifierPropertyName();

   String[] getPropertyNames();

   Type getIdentifierType();

   Type[] getPropertyTypes();

   Type getPropertyType(String var1) throws HibernateException;

   boolean hasProxy();

   boolean isMutable();

   boolean isVersioned();

   int getVersionProperty();

   boolean[] getPropertyNullability();

   boolean[] getPropertyLaziness();

   boolean hasIdentifierProperty();

   boolean hasNaturalIdentifier();

   int[] getNaturalIdentifierProperties();

   boolean hasSubclasses();

   boolean isInherited();

   Object[] getPropertyValuesToInsert(Object var1, Map var2, SessionImplementor var3) throws HibernateException;

   Class getMappedClass();

   Object instantiate(Serializable var1, SessionImplementor var2);

   Object getPropertyValue(Object var1, String var2) throws HibernateException;

   Object[] getPropertyValues(Object var1) throws HibernateException;

   void setPropertyValue(Object var1, String var2, Object var3) throws HibernateException;

   void setPropertyValues(Object var1, Object[] var2) throws HibernateException;

   /** @deprecated */
   Serializable getIdentifier(Object var1) throws HibernateException;

   Serializable getIdentifier(Object var1, SessionImplementor var2);

   void setIdentifier(Object var1, Serializable var2, SessionImplementor var3);

   boolean implementsLifecycle();

   Object getVersion(Object var1) throws HibernateException;
}
