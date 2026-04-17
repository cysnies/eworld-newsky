package org.hibernate.persister.collection;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cache.spi.entry.CacheEntryStructure;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;

public interface CollectionPersister {
   void initialize(Serializable var1, SessionImplementor var2) throws HibernateException;

   boolean hasCache();

   CollectionRegionAccessStrategy getCacheAccessStrategy();

   CacheEntryStructure getCacheEntryStructure();

   CollectionType getCollectionType();

   Type getKeyType();

   Type getIndexType();

   Type getElementType();

   Class getElementClass();

   Object readKey(ResultSet var1, String[] var2, SessionImplementor var3) throws HibernateException, SQLException;

   Object readElement(ResultSet var1, Object var2, String[] var3, SessionImplementor var4) throws HibernateException, SQLException;

   Object readIndex(ResultSet var1, String[] var2, SessionImplementor var3) throws HibernateException, SQLException;

   Object readIdentifier(ResultSet var1, String var2, SessionImplementor var3) throws HibernateException, SQLException;

   boolean isPrimitiveArray();

   boolean isArray();

   boolean isOneToMany();

   boolean isManyToMany();

   String getManyToManyFilterFragment(String var1, Map var2);

   boolean hasIndex();

   boolean isLazy();

   boolean isInverse();

   void remove(Serializable var1, SessionImplementor var2) throws HibernateException;

   void recreate(PersistentCollection var1, Serializable var2, SessionImplementor var3) throws HibernateException;

   void deleteRows(PersistentCollection var1, Serializable var2, SessionImplementor var3) throws HibernateException;

   void updateRows(PersistentCollection var1, Serializable var2, SessionImplementor var3) throws HibernateException;

   void insertRows(PersistentCollection var1, Serializable var2, SessionImplementor var3) throws HibernateException;

   String getRole();

   EntityPersister getOwnerEntityPersister();

   IdentifierGenerator getIdentifierGenerator();

   Type getIdentifierType();

   boolean hasOrphanDelete();

   boolean hasOrdering();

   boolean hasManyToManyOrdering();

   Serializable[] getCollectionSpaces();

   CollectionMetadata getCollectionMetadata();

   boolean isCascadeDeleteEnabled();

   boolean isVersioned();

   boolean isMutable();

   String getNodeName();

   String getElementNodeName();

   String getIndexNodeName();

   void postInstantiate() throws MappingException;

   SessionFactoryImplementor getFactory();

   boolean isAffectedByEnabledFilters(SessionImplementor var1);

   String[] getKeyColumnAliases(String var1);

   String[] getIndexColumnAliases(String var1);

   String[] getElementColumnAliases(String var1);

   String getIdentifierColumnAlias(String var1);

   boolean isExtraLazy();

   int getSize(Serializable var1, SessionImplementor var2);

   boolean indexExists(Serializable var1, Object var2, SessionImplementor var3);

   boolean elementExists(Serializable var1, Object var2, SessionImplementor var3);

   Object getElementByIndex(Serializable var1, Object var2, SessionImplementor var3, Object var4);

   int getBatchSize();
}
