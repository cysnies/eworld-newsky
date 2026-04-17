package org.hibernate.service.jndi.internal;

import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.event.EventContext;
import javax.naming.event.NamespaceChangeListener;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.jndi.JndiHelper;
import org.hibernate.service.jndi.JndiException;
import org.hibernate.service.jndi.JndiNameException;
import org.hibernate.service.jndi.spi.JndiService;
import org.jboss.logging.Logger;

public class JndiServiceImpl implements JndiService {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JndiServiceImpl.class.getName());
   private final Hashtable initialContextSettings;

   public JndiServiceImpl(Map configurationValues) {
      this.initialContextSettings = JndiHelper.extractJndiProperties(configurationValues);
   }

   public Object locate(String jndiName) {
      InitialContext initialContext = this.buildInitialContext();
      Name name = this.parseName(jndiName, initialContext);

      Object var4;
      try {
         var4 = initialContext.lookup(name);
      } catch (NamingException e) {
         throw new JndiException("Unable to lookup JNDI name [" + jndiName + "]", e);
      } finally {
         this.cleanUp(initialContext);
      }

      return var4;
   }

   private InitialContext buildInitialContext() {
      try {
         return this.initialContextSettings.size() == 0 ? new InitialContext() : new InitialContext(this.initialContextSettings);
      } catch (NamingException e) {
         throw new JndiException("Unable to open InitialContext", e);
      }
   }

   private Name parseName(String jndiName, Context context) {
      try {
         return context.getNameParser("").parse(jndiName);
      } catch (InvalidNameException e) {
         throw new JndiNameException("JNDI name [" + jndiName + "] was not valid", e);
      } catch (NamingException e) {
         throw new JndiException("Error parsing JNDI name [" + jndiName + "]", e);
      }
   }

   private void cleanUp(InitialContext initialContext) {
      try {
         initialContext.close();
      } catch (NamingException e) {
         LOG.unableToCloseInitialContext(e.toString());
      }

   }

   public void bind(String jndiName, Object value) {
      InitialContext initialContext = this.buildInitialContext();
      Name name = this.parseName(jndiName, initialContext);

      try {
         this.bind(name, value, initialContext);
      } finally {
         this.cleanUp(initialContext);
      }

   }

   private void bind(Name name, Object value, Context context) {
      try {
         LOG.tracef("Binding : %s", name);
         context.rebind(name, value);
      } catch (Exception initialException) {
         if (name.size() == 1) {
            throw new JndiException("Error performing bind [" + name + "]", initialException);
         }

         Context intermediateContextBase;
         for(intermediateContextBase = context; name.size() > 1; name = name.getSuffix(1)) {
            String intermediateContextName = name.get(0);
            Context intermediateContext = null;

            try {
               LOG.tracev("Intermediate lookup: {0}", intermediateContextName);
               intermediateContext = (Context)intermediateContextBase.lookup(intermediateContextName);
            } catch (NameNotFoundException var11) {
            } catch (NamingException e) {
               throw new JndiException("Unanticipated error doing intermediate lookup", e);
            }

            if (intermediateContext != null) {
               LOG.tracev("Found intermediate context: {0}", intermediateContextName);
            } else {
               LOG.tracev("Creating sub-context: {0}", intermediateContextName);

               try {
                  intermediateContext = intermediateContextBase.createSubcontext(intermediateContextName);
               } catch (NamingException e) {
                  throw new JndiException("Error creating intermediate context [" + intermediateContextName + "]", e);
               }
            }

            intermediateContextBase = intermediateContext;
         }

         LOG.tracev("Binding : {0}", name);

         try {
            intermediateContextBase.rebind(name, value);
         } catch (NamingException e) {
            throw new JndiException("Error performing intermediate bind [" + name + "]", e);
         }
      }

      LOG.debugf("Bound name: %s", name);
   }

   public void unbind(String jndiName) {
      InitialContext initialContext = this.buildInitialContext();
      Name name = this.parseName(jndiName, initialContext);

      try {
         initialContext.unbind(name);
      } catch (Exception e) {
         throw new JndiException("Error performing unbind [" + name + "]", e);
      } finally {
         this.cleanUp(initialContext);
      }

   }

   public void addListener(String jndiName, NamespaceChangeListener listener) {
      InitialContext initialContext = this.buildInitialContext();
      Name name = this.parseName(jndiName, initialContext);

      try {
         ((EventContext)initialContext).addNamingListener(name, 0, listener);
      } catch (Exception e) {
         throw new JndiException("Unable to bind listener to namespace [" + name + "]", e);
      } finally {
         this.cleanUp(initialContext);
      }

   }
}
