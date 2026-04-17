package org.hibernate.persister.entity;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

public interface UniqueKeyLoadable extends Loadable {
   Object loadByUniqueKey(String var1, Object var2, SessionImplementor var3) throws HibernateException;

   int getPropertyIndex(String var1);
}
