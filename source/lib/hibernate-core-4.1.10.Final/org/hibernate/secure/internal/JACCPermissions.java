package org.hibernate.secure.internal;

import java.lang.reflect.UndeclaredThrowableException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

public class JACCPermissions {
   public static void checkPermission(Class clazz, String contextID, EJBMethodPermission methodPerm) throws SecurityException {
      CodeSource ejbCS = clazz.getProtectionDomain().getCodeSource();

      try {
         setContextID(contextID);
         Policy policy = Policy.getPolicy();
         Subject caller = getContextSubject();
         Principal[] principals = null;
         if (caller != null) {
            Set principalsSet = caller.getPrincipals();
            principals = new Principal[principalsSet.size()];
            principalsSet.toArray(principals);
         }

         ProtectionDomain pd = new ProtectionDomain(ejbCS, (PermissionCollection)null, (ClassLoader)null, principals);
         if (!policy.implies(pd, methodPerm)) {
            String msg = "Denied: " + methodPerm + ", caller=" + caller;
            SecurityException e = new SecurityException(msg);
            throw e;
         }
      } catch (PolicyContextException e) {
         throw new RuntimeException(e);
      }
   }

   static Subject getContextSubject() throws PolicyContextException {
      return System.getSecurityManager() == null ? JACCPermissions.PolicyContextActions.NON_PRIVILEGED.getContextSubject() : JACCPermissions.PolicyContextActions.PRIVILEGED.getContextSubject();
   }

   static String setContextID(String contextID) {
      PrivilegedAction action = new SetContextID(contextID);
      String previousID = (String)AccessController.doPrivileged(action);
      return previousID;
   }

   interface PolicyContextActions {
      String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container";
      PolicyContextActions PRIVILEGED = new PolicyContextActions() {
         private final PrivilegedExceptionAction exAction = new PrivilegedExceptionAction() {
            public Object run() throws Exception {
               return (Subject)PolicyContext.getContext("javax.security.auth.Subject.container");
            }
         };

         public Subject getContextSubject() throws PolicyContextException {
            try {
               return (Subject)AccessController.doPrivileged(this.exAction);
            } catch (PrivilegedActionException e) {
               Exception ex = e.getException();
               if (ex instanceof PolicyContextException) {
                  throw (PolicyContextException)ex;
               } else {
                  throw new UndeclaredThrowableException(ex);
               }
            }
         }
      };
      PolicyContextActions NON_PRIVILEGED = new PolicyContextActions() {
         public Subject getContextSubject() throws PolicyContextException {
            return (Subject)PolicyContext.getContext("javax.security.auth.Subject.container");
         }
      };

      Subject getContextSubject() throws PolicyContextException;
   }

   private static class SetContextID implements PrivilegedAction {
      String contextID;

      SetContextID(String contextID) {
         this.contextID = contextID;
      }

      public Object run() {
         String previousID = PolicyContext.getContextID();
         PolicyContext.setContextID(this.contextID);
         return previousID;
      }
   }
}
