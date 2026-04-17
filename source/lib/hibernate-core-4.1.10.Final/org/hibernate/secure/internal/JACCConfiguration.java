package org.hibernate.secure.internal;

import java.util.StringTokenizer;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;
import org.hibernate.HibernateException;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class JACCConfiguration {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JACCConfiguration.class.getName());
   private final PolicyConfiguration policyConfiguration;

   public JACCConfiguration(String contextId) throws HibernateException {
      try {
         this.policyConfiguration = PolicyConfigurationFactory.getPolicyConfigurationFactory().getPolicyConfiguration(contextId, false);
      } catch (ClassNotFoundException cnfe) {
         throw new HibernateException("JACC provider class not found", cnfe);
      } catch (PolicyContextException pce) {
         throw new HibernateException("policy context exception occurred", pce);
      }
   }

   public void addPermission(String role, String entityName, String action) {
      if (action.equals("*")) {
         action = "insert,read,update,delete";
      }

      StringTokenizer tok = new StringTokenizer(action, ",");

      while(tok.hasMoreTokens()) {
         String methodName = tok.nextToken().trim();
         EJBMethodPermission permission = new EJBMethodPermission(entityName, methodName, (String)null, (String[])null);
         LOG.debugf("Adding permission to role %s: %s", role, permission);

         try {
            this.policyConfiguration.addToRole(role, permission);
         } catch (PolicyContextException pce) {
            throw new HibernateException("policy context exception occurred", pce);
         }
      }

   }
}
