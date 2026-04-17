package org.hibernate.loader.collection;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

public interface CollectionInitializer {
   void initialize(Serializable var1, SessionImplementor var2) throws HibernateException;
}
