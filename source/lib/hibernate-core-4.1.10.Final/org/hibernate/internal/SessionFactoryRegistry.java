package org.hibernate.internal;

import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.spi.ObjectFactory;
import org.hibernate.SessionFactory;
import org.hibernate.service.jndi.JndiException;
import org.hibernate.service.jndi.JndiNameException;
import org.hibernate.service.jndi.spi.JndiService;
import org.jboss.logging.Logger;

public class SessionFactoryRegistry {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SessionFactoryRegistry.class.getName());
   public static final SessionFactoryRegistry INSTANCE = new SessionFactoryRegistry();
   private final ConcurrentHashMap sessionFactoryMap = new ConcurrentHashMap();
   private final ConcurrentHashMap nameUuidXref = new ConcurrentHashMap();
   private final NamespaceChangeListener LISTENER = new NamespaceChangeListener() {
      public void objectAdded(NamingEvent evt) {
         SessionFactoryRegistry.LOG.debugf("A factory was successfully bound to name: %s", evt.getNewBinding().getName());
      }

      public void objectRemoved(NamingEvent evt) {
         String jndiName = evt.getOldBinding().getName();
         SessionFactoryRegistry.LOG.factoryUnboundFromName(jndiName);
         String uuid = (String)SessionFactoryRegistry.this.nameUuidXref.remove(jndiName);
         if (uuid == null) {
         }

         SessionFactoryRegistry.this.sessionFactoryMap.remove(uuid);
      }

      public void objectRenamed(NamingEvent evt) {
         String oldJndiName = evt.getOldBinding().getName();
         String newJndiName = evt.getNewBinding().getName();
         SessionFactoryRegistry.LOG.factoryJndiRename(oldJndiName, newJndiName);
         String uuid = (String)SessionFactoryRegistry.this.nameUuidXref.remove(oldJndiName);
         SessionFactoryRegistry.this.nameUuidXref.put(newJndiName, uuid);
      }

      public void namingExceptionThrown(NamingExceptionEvent evt) {
         SessionFactoryRegistry.LOG.namingExceptionAccessingFactory(evt.getException());
      }
   };

   public SessionFactoryRegistry() {
      LOG.debugf("Initializing SessionFactoryRegistry : %s", this);
   }

   public void addSessionFactory(String uuid, String name, boolean isNameAlsoJndiName, SessionFactory instance, JndiService jndiService) {
      if (uuid == null) {
         throw new IllegalArgumentException("SessionFactory UUID cannot be null");
      } else {
         LOG.debugf("Registering SessionFactory: %s (%s)", uuid, name == null ? "<unnamed>" : name);
         this.sessionFactoryMap.put(uuid, instance);
         if (name != null) {
            this.nameUuidXref.put(name, uuid);
         }

         if (name != null && isNameAlsoJndiName) {
            LOG.debugf("Attempting to bind SessionFactory [%s] to JNDI", name);

            try {
               jndiService.bind(name, instance);
               LOG.factoryBoundToJndiName(name);

               try {
                  jndiService.addListener(name, this.LISTENER);
               } catch (Exception var7) {
                  LOG.couldNotBindJndiListener();
               }
            } catch (JndiNameException e) {
               LOG.invalidJndiName(name, e);
            } catch (JndiException e) {
               LOG.unableToBindFactoryToJndi(e);
            }

         } else {
            LOG.debug("Not binding SessionFactory to JNDI, no JNDI name configured");
         }
      }
   }

   public void removeSessionFactory(String uuid, String name, boolean isNameAlsoJndiName, JndiService jndiService) {
      if (name != null) {
         this.nameUuidXref.remove(name);
         if (isNameAlsoJndiName) {
            try {
               LOG.tracef("Unbinding SessionFactory from JNDI : %s", name);
               jndiService.unbind(name);
               LOG.factoryUnboundFromJndiName(name);
            } catch (JndiNameException e) {
               LOG.invalidJndiName(name, e);
            } catch (JndiException e) {
               LOG.unableToUnbindFactoryFromJndi(e);
            }
         }
      }

      this.sessionFactoryMap.remove(uuid);
   }

   public SessionFactory getNamedSessionFactory(String name) {
      LOG.debugf("Lookup: name=%s", name);
      String uuid = (String)this.nameUuidXref.get(name);
      return this.getSessionFactory(uuid);
   }

   public SessionFactory getSessionFactory(String uuid) {
      LOG.debugf("Lookup: uid=%s", uuid);
      SessionFactory sessionFactory = (SessionFactory)this.sessionFactoryMap.get(uuid);
      if (sessionFactory == null && LOG.isDebugEnabled()) {
         LOG.debugf("Not found: %s", uuid);
         LOG.debugf(this.sessionFactoryMap.toString(), new Object[0]);
      }

      return sessionFactory;
   }

   public static class ObjectFactoryImpl implements ObjectFactory {
      public Object getObjectInstance(Object reference, Name name, Context nameCtx, Hashtable environment) throws Exception {
         SessionFactoryRegistry.LOG.debugf("JNDI lookup: %s", name);
         String uuid = (String)((Reference)reference).get(0).getContent();
         SessionFactoryRegistry.LOG.tracef("Resolved to UUID = %s", uuid);
         return SessionFactoryRegistry.INSTANCE.getSessionFactory(uuid);
      }
   }
}
