package org.hibernate.usertype;

import java.util.Iterator;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

public interface UserCollectionType {
   PersistentCollection instantiate(SessionImplementor var1, CollectionPersister var2) throws HibernateException;

   PersistentCollection wrap(SessionImplementor var1, Object var2);

   Iterator getElementsIterator(Object var1);

   boolean contains(Object var1, Object var2);

   Object indexOf(Object var1, Object var2);

   Object replaceElements(Object var1, Object var2, CollectionPersister var3, Object var4, Map var5, SessionImplementor var6) throws HibernateException;

   Object instantiate(int var1);
}
