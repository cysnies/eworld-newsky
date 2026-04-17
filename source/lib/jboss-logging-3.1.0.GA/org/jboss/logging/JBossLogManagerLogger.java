package org.jboss.logging;

import org.jboss.logmanager.ExtLogRecord.FormatStyle;

final class JBossLogManagerLogger extends Logger {
   private static final long serialVersionUID = 7429618317727584742L;
   private final org.jboss.logmanager.Logger logger;

   JBossLogManagerLogger(String name, org.jboss.logmanager.Logger logger) {
      super(name);
      this.logger = logger;
   }

   public boolean isEnabled(Logger.Level level) {
      return this.logger.isLoggable(translate(level));
   }

   protected void doLog(Logger.Level level, String loggerClassName, Object message, Object[] parameters, Throwable thrown) {
      if (parameters == null) {
         this.logger.log(loggerClassName, translate(level), String.valueOf(message), thrown);
      } else {
         this.logger.log(loggerClassName, translate(level), String.valueOf(message), FormatStyle.MESSAGE_FORMAT, parameters, thrown);
      }

   }

   protected void doLogf(Logger.Level level, String loggerClassName, String format, Object[] parameters, Throwable thrown) {
      if (parameters == null) {
         this.logger.log(loggerClassName, translate(level), format, thrown);
      } else {
         this.logger.log(loggerClassName, translate(level), format, FormatStyle.PRINTF, parameters, thrown);
      }

   }

   private static java.util.logging.Level translate(Logger.Level level) {
      if (level != null) {
         switch (level) {
            case FATAL:
               return org.jboss.logmanager.Level.FATAL;
            case ERROR:
               return org.jboss.logmanager.Level.ERROR;
            case WARN:
               return org.jboss.logmanager.Level.WARN;
            case INFO:
               return org.jboss.logmanager.Level.INFO;
            case DEBUG:
               return org.jboss.logmanager.Level.DEBUG;
            case TRACE:
               return org.jboss.logmanager.Level.TRACE;
         }
      }

      return org.jboss.logmanager.Level.ALL;
   }
}
