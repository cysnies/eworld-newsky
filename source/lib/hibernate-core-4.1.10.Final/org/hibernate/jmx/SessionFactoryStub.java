package org.hibernate.jmx;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import org.hibernate.AssertionFailure;
import org.hibernate.Cache;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.StatelessSessionBuilder;
import org.hibernate.TypeHelper;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.UUIDGenerator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.SessionFactoryRegistry;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.service.jndi.internal.JndiServiceImpl;
import org.hibernate.stat.Statistics;
import org.jboss.logging.Logger;

/** @deprecated */
@Deprecated
public class SessionFactoryStub implements SessionFactory {
   private static final IdentifierGenerator UUID_GENERATOR = UUIDGenerator.buildSessionFactoryUniqueIdentifierGenerator();
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SessionFactoryStub.class.getName());
   private transient SessionFactory impl;
   private transient HibernateService service;
   private String uuid;
   private String name;

   SessionFactoryStub(HibernateService service) {
      this.service = service;
      this.name = service.getJndiName();

      try {
         this.uuid = (String)UUID_GENERATOR.generate((SessionImplementor)null, (Object)null);
      } catch (Exception var3) {
         throw new AssertionFailure("Could not generate UUID");
      }

      SessionFactoryRegistry.INSTANCE.addSessionFactory(this.uuid, this.name, ConfigurationHelper.getBoolean("hibernate.session_factory_name_is_jndi", service.getProperties(), true), this, new JndiServiceImpl(service.getProperties()));
   }

   public SessionFactory.SessionFactoryOptions getSessionFactoryOptions() {
      return this.impl.getSessionFactoryOptions();
   }

   public SessionBuilder withOptions() {
      return this.getImpl().withOptions();
   }

   public Session openSession() throws HibernateException {
      return this.getImpl().openSession();
   }

   public Session getCurrentSession() {
      return this.getImpl().getCurrentSession();
   }

   private synchronized SessionFactory getImpl() {
      if (this.impl == null) {
         this.impl = this.service.buildSessionFactory();
      }

      return this.impl;
   }

   private Object readResolve() throws ObjectStreamException {
      Object result = SessionFactoryRegistry.INSTANCE.getSessionFactory(this.uuid);
      if (result == null) {
         result = SessionFactoryRegistry.INSTANCE.getNamedSessionFactory(this.name);
         if (result == null) {
            throw new InvalidObjectException("Could not find a SessionFactory [uuid=" + this.uuid + ",name=" + this.name + "]");
         }

         LOG.debug("Resolved stub SessionFactory by name");
      } else {
         LOG.debug("Resolved stub SessionFactory by UUID");
      }

      return result;
   }

   public Reference getReference() throws NamingException {
      return new Reference(SessionFactoryStub.class.getName(), new StringRefAddr("uuid", this.uuid), SessionFactoryRegistry.ObjectFactoryImpl.class.getName(), (String)null);
   }

   public ClassMetadata getClassMetadata(Class persistentClass) throws HibernateException {
      return this.getImpl().getClassMetadata(persistentClass);
   }

   public ClassMetadata getClassMetadata(String entityName) throws HibernateException {
      return this.getImpl().getClassMetadata(entityName);
   }

   public CollectionMetadata getCollectionMetadata(String roleName) throws HibernateException {
      return this.getImpl().getCollectionMetadata(roleName);
   }

   public Map getAllClassMetadata() throws HibernateException {
      return this.getImpl().getAllClassMetadata();
   }

   public Map getAllCollectionMetadata() throws HibernateException {
      return this.getImpl().getAllCollectionMetadata();
   }

   public void close() throws HibernateException {
   }

   public boolean isClosed() {
      return false;
   }

   public Cache getCache() {
      return this.getImpl().getCache();
   }

   public void evict(Class persistentClass, Serializable id) throws HibernateException {
      this.getImpl().evict(persistentClass, id);
   }

   public void evict(Class persistentClass) throws HibernateException {
      this.getImpl().evict(persistentClass);
   }

   public void evictEntity(String entityName, Serializable id) throws HibernateException {
      this.getImpl().evictEntity(entityName, id);
   }

   public void evictEntity(String entityName) throws HibernateException {
      this.getImpl().evictEntity(entityName);
   }

   public void evictCollection(String roleName, Serializable id) throws HibernateException {
      this.getImpl().evictCollection(roleName, id);
   }

   public void evictCollection(String roleName) throws HibernateException {
      this.getImpl().evictCollection(roleName);
   }

   public void evictQueries() throws HibernateException {
      this.getImpl().evictQueries();
   }

   public void evictQueries(String cacheRegion) throws HibernateException {
      this.getImpl().evictQueries(cacheRegion);
   }

   public Statistics getStatistics() {
      return this.getImpl().getStatistics();
   }

   public StatelessSessionBuilder withStatelessOptions() {
      return this.getImpl().withStatelessOptions();
   }

   public StatelessSession openStatelessSession() {
      return this.getImpl().openStatelessSession();
   }

   public StatelessSession openStatelessSession(Connection conn) {
      return this.getImpl().openStatelessSession(conn);
   }

   public Set getDefinedFilterNames() {
      return this.getImpl().getDefinedFilterNames();
   }

   public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
      return this.getImpl().getFilterDefinition(filterName);
   }

   public boolean containsFetchProfileDefinition(String name) {
      return this.getImpl().containsFetchProfileDefinition(name);
   }

   public TypeHelper getTypeHelper() {
      return this.getImpl().getTypeHelper();
   }
}
