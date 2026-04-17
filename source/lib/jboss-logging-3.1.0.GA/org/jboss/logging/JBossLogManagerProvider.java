package org.jboss.logging;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import org.jboss.logmanager.LogContext;

final class JBossLogManagerProvider implements LoggerProvider {
   private static final org.jboss.logmanager.Logger.AttachmentKey KEY = new org.jboss.logmanager.Logger.AttachmentKey();

   public Logger getLogger(final String name) {
      SecurityManager sm = System.getSecurityManager();
      return sm != null ? (Logger)AccessController.doPrivileged(new PrivilegedAction() {
         public Logger run() {
            return JBossLogManagerProvider.doGetLogger(name);
         }
      }) : doGetLogger(name);
   }

   private static Logger doGetLogger(String name) {
      Logger l = (Logger)LogContext.getLogContext().getAttachment(name, KEY);
      if (l != null) {
         return l;
      } else {
         org.jboss.logmanager.Logger logger = org.jboss.logmanager.Logger.getLogger(name);
         l = new JBossLogManagerLogger(name, logger);
         Logger a = (Logger)logger.attachIfAbsent(KEY, l);
         return a == null ? l : a;
      }
   }

   public Object putMdc(String key, Object value) {
      return org.jboss.logmanager.MDC.put(key, String.valueOf(value));
   }

   public Object getMdc(String key) {
      return org.jboss.logmanager.MDC.get(key);
   }

   public void removeMdc(String key) {
      org.jboss.logmanager.MDC.remove(key);
   }

   public Map getMdcMap() {
      return org.jboss.logmanager.MDC.copy();
   }

   public void clearNdc() {
      org.jboss.logmanager.NDC.clear();
   }

   public String getNdc() {
      return org.jboss.logmanager.NDC.get();
   }

   public int getNdcDepth() {
      return org.jboss.logmanager.NDC.getDepth();
   }

   public String popNdc() {
      return org.jboss.logmanager.NDC.pop();
   }

   public String peekNdc() {
      return org.jboss.logmanager.NDC.get();
   }

   public void pushNdc(String message) {
      org.jboss.logmanager.NDC.push(message);
   }

   public void setNdcMaxDepth(int maxDepth) {
      org.jboss.logmanager.NDC.trimTo(maxDepth);
   }
}
