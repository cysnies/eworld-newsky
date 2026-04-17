package org.hibernate.collection.spi;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.Type;

public interface PersistentCollection {
   Object getOwner();

   void setOwner(Object var1);

   boolean empty();

   void setSnapshot(Serializable var1, String var2, Serializable var3);

   void postAction();

   Object getValue();

   void beginRead();

   boolean endRead();

   boolean afterInitialize();

   boolean isDirectlyAccessible();

   boolean unsetSession(SessionImplementor var1);

   boolean setCurrentSession(SessionImplementor var1) throws HibernateException;

   void initializeFromCache(CollectionPersister var1, Serializable var2, Object var3) throws HibernateException;

   Iterator entries(CollectionPersister var1);

   Object readFrom(ResultSet var1, CollectionPersister var2, CollectionAliases var3, Object var4) throws HibernateException, SQLException;

   Object getIdentifier(Object var1, int var2);

   Object getIndex(Object var1, int var2, CollectionPersister var3);

   Object getElement(Object var1);

   Object getSnapshotElement(Object var1, int var2);

   void beforeInitialize(CollectionPersister var1, int var2);

   boolean equalsSnapshot(CollectionPersister var1) throws HibernateException;

   boolean isSnapshotEmpty(Serializable var1);

   Serializable disassemble(CollectionPersister var1) throws HibernateException;

   boolean needsRecreate(CollectionPersister var1);

   Serializable getSnapshot(CollectionPersister var1) throws HibernateException;

   void forceInitialization() throws HibernateException;

   boolean entryExists(Object var1, int var2);

   boolean needsInserting(Object var1, int var2, Type var3) throws HibernateException;

   boolean needsUpdating(Object var1, int var2, Type var3) throws HibernateException;

   boolean isRowUpdatePossible();

   Iterator getDeletes(CollectionPersister var1, boolean var2) throws HibernateException;

   boolean isWrapper(Object var1);

   boolean wasInitialized();

   boolean hasQueuedOperations();

   Iterator queuedAdditionIterator();

   Collection getQueuedOrphans(String var1);

   Serializable getKey();

   String getRole();

   boolean isUnreferenced();

   boolean isDirty();

   void clearDirty();

   Serializable getStoredSnapshot();

   void dirty();

   void preInsert(CollectionPersister var1) throws HibernateException;

   void afterRowInsert(CollectionPersister var1, Object var2, int var3) throws HibernateException;

   Collection getOrphans(Serializable var1, String var2) throws HibernateException;
}
