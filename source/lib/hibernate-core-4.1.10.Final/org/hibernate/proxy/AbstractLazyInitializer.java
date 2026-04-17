package org.hibernate.proxy;

import java.io.Serializable;
import javax.naming.NamingException;
import org.hibernate.HibernateException;
import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.hibernate.SessionException;
import org.hibernate.TransientObjectException;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.SessionFactoryRegistry;
import org.hibernate.persister.entity.EntityPersister;
import org.jboss.logging.Logger;

public abstract class AbstractLazyInitializer implements LazyInitializer {
   private static final Logger log = Logger.getLogger(AbstractLazyInitializer.class);
   private String entityName;
   private Serializable id;
   private Object target;
   private boolean initialized;
   private boolean readOnly;
   private boolean unwrap;
   private transient SessionImplementor session;
   private Boolean readOnlyBeforeAttachedToSession;
   private String sessionFactoryUuid;
   private boolean specjLazyLoad = false;

   protected AbstractLazyInitializer() {
   }

   protected AbstractLazyInitializer(String entityName, Serializable id, SessionImplementor session) {
      this.entityName = entityName;
      this.id = id;
      if (session == null) {
         this.unsetSession();
      } else {
         this.setSession(session);
      }

   }

   public final String getEntityName() {
      return this.entityName;
   }

   public final Serializable getIdentifier() {
      return this.id;
   }

   public final void setIdentifier(Serializable id) {
      this.id = id;
   }

   public final boolean isUninitialized() {
      return !this.initialized;
   }

   public final SessionImplementor getSession() {
      return this.session;
   }

   public final void setSession(SessionImplementor s) throws HibernateException {
      if (s != this.session) {
         if (s == null) {
            this.unsetSession();
         } else {
            if (this.isConnectedToSession()) {
               throw new HibernateException("illegally attempted to associate a proxy with two open Sessions");
            }

            this.session = s;
            if (this.readOnlyBeforeAttachedToSession == null) {
               EntityPersister persister = s.getFactory().getEntityPersister(this.entityName);
               this.setReadOnly(s.getPersistenceContext().isDefaultReadOnly() || !persister.isMutable());
            } else {
               this.setReadOnly(this.readOnlyBeforeAttachedToSession);
               this.readOnlyBeforeAttachedToSession = null;
            }
         }
      }

   }

   private static EntityKey generateEntityKeyOrNull(Serializable id, SessionImplementor s, String entityName) {
      return id != null && s != null && entityName != null ? s.generateEntityKey(id, s.getFactory().getEntityPersister(entityName)) : null;
   }

   public final void unsetSession() {
      this.prepareForPossibleSpecialSpecjInitialization();
      this.session = null;
      this.readOnly = false;
      this.readOnlyBeforeAttachedToSession = null;
   }

   public final void initialize() throws HibernateException {
      if (!this.initialized) {
         if (this.specjLazyLoad) {
            this.specialSpecjInitialization();
         } else {
            if (this.session == null) {
               throw new LazyInitializationException("could not initialize proxy - no Session");
            }

            if (!this.session.isOpen()) {
               throw new LazyInitializationException("could not initialize proxy - the owning Session was closed");
            }

            if (!this.session.isConnected()) {
               throw new LazyInitializationException("could not initialize proxy - the owning Session is disconnected");
            }

            this.target = this.session.immediateLoad(this.entityName, this.id);
            this.initialized = true;
            this.checkTargetState();
         }
      } else {
         this.checkTargetState();
      }

   }

   protected void specialSpecjInitialization() {
      if (this.session == null) {
         if (this.sessionFactoryUuid == null) {
            throw new LazyInitializationException("could not initialize proxy - no Session");
         }

         try {
            SessionFactoryImplementor sf = (SessionFactoryImplementor)SessionFactoryRegistry.INSTANCE.getSessionFactory(this.sessionFactoryUuid);
            SessionImplementor session = (SessionImplementor)sf.openSession();
            boolean isJTA = session.getTransactionCoordinator().getTransactionContext().getTransactionEnvironment().getTransactionFactory().compatibleWithJtaSynchronization();
            if (!isJTA) {
               ((Session)session).beginTransaction();
            }

            try {
               this.target = session.immediateLoad(this.entityName, this.id);
            } finally {
               try {
                  if (!isJTA) {
                     ((Session)session).getTransaction().commit();
                  }

                  ((Session)session).close();
               } catch (Exception var11) {
                  log.warn("Unable to close temporary session used to load lazy proxy associated to no session");
               }

            }

            this.initialized = true;
            this.checkTargetState();
         } catch (Exception e) {
            e.printStackTrace();
            throw new LazyInitializationException(e.getMessage());
         }
      } else {
         if (!this.session.isOpen() || !this.session.isConnected()) {
            throw new LazyInitializationException("could not initialize proxy - Session was closed or disced");
         }

         this.target = this.session.immediateLoad(this.entityName, this.id);
         this.initialized = true;
         this.checkTargetState();
      }

   }

   protected void prepareForPossibleSpecialSpecjInitialization() {
      if (this.session != null) {
         this.specjLazyLoad = this.session.getFactory().getSettings().isInitializeLazyStateOutsideTransactionsEnabled();
         if (this.specjLazyLoad && this.sessionFactoryUuid == null) {
            try {
               this.sessionFactoryUuid = (String)this.session.getFactory().getReference().get("uuid").getContent();
            } catch (NamingException var2) {
            }
         }
      }

   }

   private void checkTargetState() {
      if (!this.unwrap && this.target == null) {
         this.getSession().getFactory().getEntityNotFoundDelegate().handleEntityNotFound(this.entityName, this.id);
      }

   }

   protected final boolean isConnectedToSession() {
      return this.getProxyOrNull() != null;
   }

   private Object getProxyOrNull() {
      EntityKey entityKey = generateEntityKeyOrNull(this.getIdentifier(), this.session, this.getEntityName());
      return entityKey != null && this.session != null && this.session.isOpen() ? this.session.getPersistenceContext().getProxy(entityKey) : null;
   }

   public final Object getImplementation() {
      this.initialize();
      return this.target;
   }

   public final void setImplementation(Object target) {
      this.target = target;
      this.initialized = true;
   }

   public final Object getImplementation(SessionImplementor s) throws HibernateException {
      EntityKey entityKey = generateEntityKeyOrNull(this.getIdentifier(), s, this.getEntityName());
      return entityKey == null ? null : s.getPersistenceContext().getEntity(entityKey);
   }

   protected final Object getTarget() {
      return this.target;
   }

   public final boolean isReadOnlySettingAvailable() {
      return this.session != null && !this.session.isClosed();
   }

   private void errorIfReadOnlySettingNotAvailable() {
      if (this.session == null) {
         throw new TransientObjectException("Proxy is detached (i.e, session is null). The read-only/modifiable setting is only accessible when the proxy is associated with an open session.");
      } else if (this.session.isClosed()) {
         throw new SessionException("Session is closed. The read-only/modifiable setting is only accessible when the proxy is associated with an open session.");
      }
   }

   public final boolean isReadOnly() {
      this.errorIfReadOnlySettingNotAvailable();
      return this.readOnly;
   }

   public final void setReadOnly(boolean readOnly) {
      this.errorIfReadOnlySettingNotAvailable();
      if (this.readOnly != readOnly) {
         EntityPersister persister = this.session.getFactory().getEntityPersister(this.entityName);
         if (!persister.isMutable() && !readOnly) {
            throw new IllegalStateException("cannot make proxies for immutable entities modifiable");
         }

         this.readOnly = readOnly;
         if (this.initialized) {
            EntityKey key = generateEntityKeyOrNull(this.getIdentifier(), this.session, this.getEntityName());
            if (key != null && this.session.getPersistenceContext().containsEntity(key)) {
               this.session.getPersistenceContext().setReadOnly(this.target, readOnly);
            }
         }
      }

   }

   protected final Boolean isReadOnlyBeforeAttachedToSession() {
      if (this.isReadOnlySettingAvailable()) {
         throw new IllegalStateException("Cannot call isReadOnlyBeforeAttachedToSession when isReadOnlySettingAvailable == true");
      } else {
         return this.readOnlyBeforeAttachedToSession;
      }
   }

   final void setReadOnlyBeforeAttachedToSession(Boolean readOnlyBeforeAttachedToSession) {
      if (this.isReadOnlySettingAvailable()) {
         throw new IllegalStateException("Cannot call setReadOnlyBeforeAttachedToSession when isReadOnlySettingAvailable == true");
      } else {
         this.readOnlyBeforeAttachedToSession = readOnlyBeforeAttachedToSession;
      }
   }

   public boolean isUnwrap() {
      return this.unwrap;
   }

   public void setUnwrap(boolean unwrap) {
      this.unwrap = unwrap;
   }
}
