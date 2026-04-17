package org.hibernate.context.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import javax.transaction.Synchronization;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.spi.AbstractCurrentSessionContext;
import org.hibernate.engine.jdbc.LobCreationContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.transaction.spi.TransactionContext;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class ThreadLocalSessionContext extends AbstractCurrentSessionContext {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ThreadLocalSessionContext.class.getName());
   private static final Class[] SESSION_PROXY_INTERFACES = new Class[]{Session.class, SessionImplementor.class, EventSource.class, TransactionContext.class, LobCreationContext.class};
   private static final ThreadLocal context = new ThreadLocal();

   public ThreadLocalSessionContext(SessionFactoryImplementor factory) {
      super(factory);
   }

   public final Session currentSession() throws HibernateException {
      Session current = existingSession(this.factory());
      if (current == null) {
         current = this.buildOrObtainSession();
         current.getTransaction().registerSynchronization(this.buildCleanupSynch());
         if (this.needsWrapping(current)) {
            current = this.wrap(current);
         }

         doBind(current, this.factory());
      } else {
         this.validateExistingSession(current);
      }

      return current;
   }

   private boolean needsWrapping(Session session) {
      return session != null && !Proxy.isProxyClass(session.getClass()) || Proxy.getInvocationHandler(session) != null && !(Proxy.getInvocationHandler(session) instanceof TransactionProtectionWrapper);
   }

   protected SessionFactoryImplementor getFactory() {
      return this.factory();
   }

   protected Session buildOrObtainSession() {
      return this.baseSessionBuilder().autoClose(this.isAutoCloseEnabled()).connectionReleaseMode(this.getConnectionReleaseMode()).flushBeforeCompletion(this.isAutoFlushEnabled()).openSession();
   }

   protected CleanupSynch buildCleanupSynch() {
      return new CleanupSynch(this.factory());
   }

   protected boolean isAutoCloseEnabled() {
      return true;
   }

   protected boolean isAutoFlushEnabled() {
      return true;
   }

   protected ConnectionReleaseMode getConnectionReleaseMode() {
      return this.factory().getSettings().getConnectionReleaseMode();
   }

   protected Session wrap(Session session) {
      TransactionProtectionWrapper wrapper = new TransactionProtectionWrapper(session);
      Session wrapped = (Session)Proxy.newProxyInstance(Session.class.getClassLoader(), SESSION_PROXY_INTERFACES, wrapper);
      wrapper.setWrapped(wrapped);
      return wrapped;
   }

   public static void bind(Session session) {
      SessionFactory factory = session.getSessionFactory();
      cleanupAnyOrphanedSession(factory);
      doBind(session, factory);
   }

   private static void cleanupAnyOrphanedSession(SessionFactory factory) {
      Session orphan = doUnbind(factory, false);
      if (orphan != null) {
         LOG.alreadySessionBound();

         try {
            if (orphan.getTransaction() != null && orphan.getTransaction().isActive()) {
               try {
                  orphan.getTransaction().rollback();
               } catch (Throwable t) {
                  LOG.debug("Unable to rollback transaction for orphaned session", t);
               }
            }

            orphan.close();
         } catch (Throwable t) {
            LOG.debug("Unable to close orphaned session", t);
         }
      }

   }

   public static Session unbind(SessionFactory factory) {
      return doUnbind(factory, true);
   }

   private static Session existingSession(SessionFactory factory) {
      Map sessionMap = sessionMap();
      return sessionMap == null ? null : (Session)sessionMap.get(factory);
   }

   protected static Map sessionMap() {
      return (Map)context.get();
   }

   private static void doBind(Session session, SessionFactory factory) {
      Map sessionMap = sessionMap();
      if (sessionMap == null) {
         sessionMap = new HashMap();
         context.set(sessionMap);
      }

      sessionMap.put(factory, session);
   }

   private static Session doUnbind(SessionFactory factory, boolean releaseMapIfEmpty) {
      Map sessionMap = sessionMap();
      Session session = null;
      if (sessionMap != null) {
         session = (Session)sessionMap.remove(factory);
         if (releaseMapIfEmpty && sessionMap.isEmpty()) {
            context.set((Object)null);
         }
      }

      return session;
   }

   protected static class CleanupSynch implements Synchronization, Serializable {
      protected final SessionFactory factory;

      public CleanupSynch(SessionFactory factory) {
         this.factory = factory;
      }

      public void beforeCompletion() {
      }

      public void afterCompletion(int i) {
         ThreadLocalSessionContext.unbind(this.factory);
      }
   }

   private class TransactionProtectionWrapper implements InvocationHandler, Serializable {
      private final Session realSession;
      private Session wrappedSession;

      public TransactionProtectionWrapper(Session realSession) {
         this.realSession = realSession;
      }

      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         String methodName = method.getName();

         try {
            if ("close".equals(methodName)) {
               ThreadLocalSessionContext.unbind(this.realSession.getSessionFactory());
            } else if (!"toString".equals(methodName) && !"equals".equals(methodName) && !"hashCode".equals(methodName) && !"getStatistics".equals(methodName) && !"isOpen".equals(methodName) && !"getListeners".equals(methodName) && this.realSession.isOpen() && !this.realSession.getTransaction().isActive()) {
               if (!"beginTransaction".equals(methodName) && !"getTransaction".equals(methodName) && !"isTransactionInProgress".equals(methodName) && !"setFlushMode".equals(methodName) && !"getFactory".equals(methodName) && !"getSessionFactory".equals(methodName)) {
                  if (!"reconnect".equals(methodName) && !"disconnect".equals(methodName)) {
                     throw new HibernateException(methodName + " is not valid without active transaction");
                  }
               } else {
                  ThreadLocalSessionContext.LOG.tracev("Allowing method [{0}] in non-transacted context", methodName);
               }
            }

            ThreadLocalSessionContext.LOG.tracev("Allowing proxied method [{0}] to proceed to real session", methodName);
            return method.invoke(this.realSession, args);
         } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
               throw (RuntimeException)e.getTargetException();
            } else {
               throw e;
            }
         }
      }

      public void setWrapped(Session wrapped) {
         this.wrappedSession = wrapped;
      }

      private void writeObject(ObjectOutputStream oos) throws IOException {
         oos.defaultWriteObject();
         if (ThreadLocalSessionContext.existingSession(ThreadLocalSessionContext.this.factory()) == this.wrappedSession) {
            ThreadLocalSessionContext.unbind(ThreadLocalSessionContext.this.factory());
         }

      }

      private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
         ois.defaultReadObject();
         this.realSession.getTransaction().registerSynchronization(ThreadLocalSessionContext.this.buildCleanupSynch());
         ThreadLocalSessionContext.doBind(this.wrappedSession, ThreadLocalSessionContext.this.factory());
      }
   }
}
