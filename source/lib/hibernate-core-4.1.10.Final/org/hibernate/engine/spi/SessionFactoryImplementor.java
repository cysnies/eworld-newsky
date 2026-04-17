package org.hibernate.engine.spi;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.hibernate.CustomEntityDirtinessStrategy;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.cache.spi.QueryCache;
import org.hibernate.cache.spi.Region;
import org.hibernate.cache.spi.UpdateTimestampsCache;
import org.hibernate.cfg.Settings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionRegistry;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.profile.FetchProfile;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.exception.spi.SQLExceptionConverter;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.stat.spi.StatisticsImplementor;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;

public interface SessionFactoryImplementor extends Mapping, SessionFactory {
   SessionBuilderImplementor withOptions();

   TypeResolver getTypeResolver();

   Properties getProperties();

   EntityPersister getEntityPersister(String var1) throws MappingException;

   Map getEntityPersisters();

   CollectionPersister getCollectionPersister(String var1) throws MappingException;

   Map getCollectionPersisters();

   JdbcServices getJdbcServices();

   Dialect getDialect();

   Interceptor getInterceptor();

   QueryPlanCache getQueryPlanCache();

   Type[] getReturnTypes(String var1) throws HibernateException;

   String[] getReturnAliases(String var1) throws HibernateException;

   /** @deprecated */
   @Deprecated
   ConnectionProvider getConnectionProvider();

   String[] getImplementors(String var1) throws MappingException;

   String getImportedClassName(String var1);

   QueryCache getQueryCache();

   QueryCache getQueryCache(String var1) throws HibernateException;

   UpdateTimestampsCache getUpdateTimestampsCache();

   StatisticsImplementor getStatisticsImplementor();

   NamedQueryDefinition getNamedQuery(String var1);

   NamedSQLQueryDefinition getNamedSQLQuery(String var1);

   ResultSetMappingDefinition getResultSetMapping(String var1);

   IdentifierGenerator getIdentifierGenerator(String var1);

   Region getSecondLevelCacheRegion(String var1);

   Region getNaturalIdCacheRegion(String var1);

   Map getAllSecondLevelCacheRegions();

   SQLExceptionConverter getSQLExceptionConverter();

   SqlExceptionHelper getSQLExceptionHelper();

   Settings getSettings();

   Session openTemporarySession() throws HibernateException;

   Set getCollectionRolesByEntityParticipant(String var1);

   EntityNotFoundDelegate getEntityNotFoundDelegate();

   SQLFunctionRegistry getSqlFunctionRegistry();

   FetchProfile getFetchProfile(String var1);

   ServiceRegistryImplementor getServiceRegistry();

   void addObserver(SessionFactoryObserver var1);

   CustomEntityDirtinessStrategy getCustomEntityDirtinessStrategy();

   CurrentTenantIdentifierResolver getCurrentTenantIdentifierResolver();
}
