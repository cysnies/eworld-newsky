package org.hibernate.type;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;

/** @deprecated */
@Deprecated
public interface XmlRepresentableType {
   String toXMLString(Object var1, SessionFactoryImplementor var2) throws HibernateException;

   Object fromXMLString(String var1, Mapping var2) throws HibernateException;
}
