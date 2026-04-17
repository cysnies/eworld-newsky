package org.hibernate.context.internal;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.spi.AbstractCurrentSessionContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;

public class ManagedSessionContext extends AbstractCurrentSessionContext {
   private static final ThreadLocal context = new ThreadLocal();

   public ManagedSessionContext(SessionFactoryImplementor factory) {
      super(factory);
   }

   public Session currentSession() {
      Session current = existingSession(this.factory());
      if (current == null) {
         throw new HibernateException("No session currently bound to execution context");
      } else {
         this.validateExistingSession(current);
         return current;
      }
   }

   public static boolean hasBind(SessionFactory factory) {
      return existingSession(factory) != null;
   }

   public static Session bind(Session session) {
      return (Session)sessionMap(true).put(session.getSessionFactory(), session);
   }

   public static Session unbind(SessionFactory factory) {
      Session existing = null;
      Map<SessionFactory, Session> sessionMap = sessionMap();
      if (sessionMap != null) {
         existing = (Session)sessionMap.remove(factory);
         doCleanup();
      }

      return existing;
   }

   private static Session existingSession(SessionFactory factory) {
      Map sessionMap = sessionMap();
      return sessionMap == null ? null : (Session)sessionMap.get(factory);
   }

   protected static Map sessionMap() {
      return sessionMap(false);
   }

   private static synchronized Map sessionMap(boolean createMap) {
      Map<SessionFactory, Session> sessionMap = (Map)context.get();
      if (sessionMap == null && createMap) {
         sessionMap = new HashMap();
         context.set(sessionMap);
      }

      return sessionMap;
   }

   private static synchronized void doCleanup() {
      Map<SessionFactory, Session> sessionMap = sessionMap(false);
      if (sessionMap != null && sessionMap.isEmpty()) {
         context.set((Object)null);
      }

   }
}
