package org.hibernate.loader.entity;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.engine.spi.SessionImplementor;

public interface UniqueEntityLoader {
   /** @deprecated */
   Object load(Serializable var1, Object var2, SessionImplementor var3) throws HibernateException;

   Object load(Serializable var1, Object var2, SessionImplementor var3, LockOptions var4);
}
