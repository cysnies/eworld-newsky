package org.hibernate.event.spi;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.engine.spi.ActionQueue;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;

public interface EventSource extends SessionImplementor, Session {
   ActionQueue getActionQueue();

   Object instantiate(EntityPersister var1, Serializable var2) throws HibernateException;

   void forceFlush(EntityEntry var1) throws HibernateException;

   void merge(String var1, Object var2, Map var3) throws HibernateException;

   void persist(String var1, Object var2, Map var3) throws HibernateException;

   void persistOnFlush(String var1, Object var2, Map var3);

   void refresh(Object var1, Map var2) throws HibernateException;

   void delete(String var1, Object var2, boolean var3, Set var4);
}
