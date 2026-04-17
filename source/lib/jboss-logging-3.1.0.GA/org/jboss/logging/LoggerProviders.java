package org.jboss.logging;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.LogManager;

final class LoggerProviders {
   static final String LOGGING_PROVIDER_KEY = "org.jboss.logging.provider";
   static final LoggerProvider PROVIDER = find();

   private static LoggerProvider find() {
      LoggerProvider result = findProvider();
      result.getLogger("org.jboss.logging").debugf("Logging Provider: %s", (Object)result.getClass().getName());
      return result;
   }

   private static LoggerProvider findProvider() {
      ClassLoader cl = LoggerProviders.class.getClassLoader();

      try {
         String loggerProvider = (String)AccessController.doPrivileged(new PrivilegedAction() {
            public String run() {
               return System.getProperty("org.jboss.logging.provider");
            }
         });
         if (loggerProvider != null) {
            if ("jboss".equalsIgnoreCase(loggerProvider)) {
               return tryJBossLogManager(cl);
            }

            if ("jdk".equalsIgnoreCase(loggerProvider)) {
               return tryJDK();
            }

            if ("log4j".equalsIgnoreCase(loggerProvider)) {
               return tryLog4j(cl);
            }

            if ("slf4j".equalsIgnoreCase(loggerProvider)) {
               return trySlf4j();
            }
         }
      } catch (Throwable var5) {
      }

      try {
         return tryJBossLogManager(cl);
      } catch (Throwable var4) {
         try {
            return tryLog4j(cl);
         } catch (Throwable var3) {
            try {
               Class.forName("ch.qos.logback.classic.Logger", false, cl);
               return trySlf4j();
            } catch (Throwable var2) {
               return tryJDK();
            }
         }
      }
   }

   private static JDKLoggerProvider tryJDK() {
      return new JDKLoggerProvider();
   }

   private static LoggerProvider trySlf4j() {
      return new Slf4jLoggerProvider();
   }

   private static LoggerProvider tryLog4j(ClassLoader cl) throws ClassNotFoundException {
      Class.forName("org.apache.log4j.LogManager", true, cl);
      Class.forName("org.apache.log4j.Hierarchy", true, cl);
      return new Log4jLoggerProvider();
   }

   private static LoggerProvider tryJBossLogManager(ClassLoader cl) throws ClassNotFoundException {
      Class<? extends LogManager> logManagerClass = LogManager.getLogManager().getClass();
      if (logManagerClass == Class.forName("org.jboss.logmanager.LogManager", false, cl) && Class.forName("org.jboss.logmanager.Logger$AttachmentKey", true, cl).getClassLoader() == logManagerClass.getClassLoader()) {
         return new JBossLogManagerProvider();
      } else {
         throw new IllegalStateException();
      }
   }

   private LoggerProviders() {
   }
}
