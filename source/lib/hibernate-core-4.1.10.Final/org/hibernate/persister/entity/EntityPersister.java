package org.hibernate.persister.entity;

import java.io.Serializable;
import java.util.Map;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.bytecode.spi.EntityInstrumentationMetadata;
import org.hibernate.cache.spi.OptimisticCacheSource;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cache.spi.entry.CacheEntryStructure;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.ValueInclusion;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.internal.FilterAliasGenerator;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.type.Type;
import org.hibernate.type.VersionType;

public interface EntityPersister extends OptimisticCacheSource {
   String ENTITY_ID = "id";

   void postInstantiate() throws MappingException;

   SessionFactoryImplementor getFactory();

   String getRootEntityName();

   String getEntityName();

   EntityMetamodel getEntityMetamodel();

   boolean isSubclassEntityName(String var1);

   Serializable[] getPropertySpaces();

   Serializable[] getQuerySpaces();

   boolean hasProxy();

   boolean hasCollections();

   boolean hasMutableProperties();

   boolean hasSubselectLoadableCollections();

   boolean hasCascades();

   boolean isMutable();

   boolean isInherited();

   boolean isIdentifierAssignedByInsert();

   Type getPropertyType(String var1) throws MappingException;

   int[] findDirty(Object[] var1, Object[] var2, Object var3, SessionImplementor var4);

   int[] findModified(Object[] var1, Object[] var2, Object var3, SessionImplementor var4);

   boolean hasIdentifierProperty();

   boolean canExtractIdOutOfEntity();

   boolean isVersioned();

   VersionType getVersionType();

   int getVersionProperty();

   boolean hasNaturalIdentifier();

   int[] getNaturalIdentifierProperties();

   Object[] getNaturalIdentifierSnapshot(Serializable var1, SessionImplementor var2);

   IdentifierGenerator getIdentifierGenerator();

   boolean hasLazyProperties();

   Serializable loadEntityIdByNaturalId(Object[] var1, LockOptions var2, SessionImplementor var3);

   Object load(Serializable var1, Object var2, LockMode var3, SessionImplementor var4) throws HibernateException;

   Object load(Serializable var1, Object var2, LockOptions var3, SessionImplementor var4) throws HibernateException;

   void lock(Serializable var1, Object var2, Object var3, LockMode var4, SessionImplementor var5) throws HibernateException;

   void lock(Serializable var1, Object var2, Object var3, LockOptions var4, SessionImplementor var5) throws HibernateException;

   void insert(Serializable var1, Object[] var2, Object var3, SessionImplementor var4) throws HibernateException;

   Serializable insert(Object[] var1, Object var2, SessionImplementor var3) throws HibernateException;

   void delete(Serializable var1, Object var2, Object var3, SessionImplementor var4) throws HibernateException;

   void update(Serializable var1, Object[] var2, int[] var3, boolean var4, Object[] var5, Object var6, Object var7, Object var8, SessionImplementor var9) throws HibernateException;

   Type[] getPropertyTypes();

   String[] getPropertyNames();

   boolean[] getPropertyInsertability();

   ValueInclusion[] getPropertyInsertGenerationInclusions();

   ValueInclusion[] getPropertyUpdateGenerationInclusions();

   boolean[] getPropertyUpdateability();

   boolean[] getPropertyCheckability();

   boolean[] getPropertyNullability();

   boolean[] getPropertyVersionability();

   boolean[] getPropertyLaziness();

   CascadeStyle[] getPropertyCascadeStyles();

   Type getIdentifierType();

   String getIdentifierPropertyName();

   boolean isCacheInvalidationRequired();

   boolean isLazyPropertiesCacheable();

   boolean hasCache();

   EntityRegionAccessStrategy getCacheAccessStrategy();

   CacheEntryStructure getCacheEntryStructure();

   boolean hasNaturalIdCache();

   NaturalIdRegionAccessStrategy getNaturalIdCacheAccessStrategy();

   ClassMetadata getClassMetadata();

   boolean isBatchLoadable();

   boolean isSelectBeforeUpdateRequired();

   Object[] getDatabaseSnapshot(Serializable var1, SessionImplementor var2) throws HibernateException;

   Serializable getIdByUniqueKey(Serializable var1, String var2, SessionImplementor var3);

   Object getCurrentVersion(Serializable var1, SessionImplementor var2) throws HibernateException;

   Object forceVersionIncrement(Serializable var1, Object var2, SessionImplementor var3) throws HibernateException;

   boolean isInstrumented();

   boolean hasInsertGeneratedProperties();

   boolean hasUpdateGeneratedProperties();

   boolean isVersionPropertyGenerated();

   void afterInitialize(Object var1, boolean var2, SessionImplementor var3);

   void afterReassociate(Object var1, SessionImplementor var2);

   Object createProxy(Serializable var1, SessionImplementor var2) throws HibernateException;

   Boolean isTransient(Object var1, SessionImplementor var2) throws HibernateException;

   Object[] getPropertyValuesToInsert(Object var1, Map var2, SessionImplementor var3) throws HibernateException;

   void processInsertGeneratedProperties(Serializable var1, Object var2, Object[] var3, SessionImplementor var4);

   void processUpdateGeneratedProperties(Serializable var1, Object var2, Object[] var3, SessionImplementor var4);

   Class getMappedClass();

   boolean implementsLifecycle();

   Class getConcreteProxyClass();

   void setPropertyValues(Object var1, Object[] var2);

   void setPropertyValue(Object var1, int var2, Object var3);

   Object[] getPropertyValues(Object var1);

   Object getPropertyValue(Object var1, int var2) throws HibernateException;

   Object getPropertyValue(Object var1, String var2);

   /** @deprecated */
   Serializable getIdentifier(Object var1) throws HibernateException;

   Serializable getIdentifier(Object var1, SessionImplementor var2);

   void setIdentifier(Object var1, Serializable var2, SessionImplementor var3);

   Object getVersion(Object var1) throws HibernateException;

   Object instantiate(Serializable var1, SessionImplementor var2);

   boolean isInstance(Object var1);

   boolean hasUninitializedLazyProperties(Object var1);

   void resetIdentifier(Object var1, Serializable var2, Object var3, SessionImplementor var4);

   EntityPersister getSubclassEntityPersister(Object var1, SessionFactoryImplementor var2);

   EntityMode getEntityMode();

   EntityTuplizer getEntityTuplizer();

   EntityInstrumentationMetadata getInstrumentationMetadata();

   FilterAliasGenerator getFilterAliasGenerator(String var1);
}
