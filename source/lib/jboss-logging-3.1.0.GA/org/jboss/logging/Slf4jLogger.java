package org.jboss.logging;

import java.text.MessageFormat;

final class Slf4jLogger extends Logger {
   private static final long serialVersionUID = 8685757928087758380L;
   private final org.slf4j.Logger logger;

   Slf4jLogger(String name, org.slf4j.Logger logger) {
      super(name);
      this.logger = logger;
   }

   public boolean isEnabled(Logger.Level level) {
      if (level != null) {
         switch (level) {
            case FATAL:
               return this.logger.isErrorEnabled();
            case ERROR:
               return this.logger.isErrorEnabled();
            case WARN:
               return this.logger.isWarnEnabled();
            case INFO:
               return this.logger.isInfoEnabled();
            case DEBUG:
               return this.logger.isDebugEnabled();
            case TRACE:
               return this.logger.isTraceEnabled();
         }
      }

      return true;
   }

   protected void doLog(Logger.Level level, String loggerClassName, Object message, Object[] parameters, Throwable thrown) {
      if (this.isEnabled(level)) {
         try {
            String text = parameters != null && parameters.length != 0 ? MessageFormat.format(String.valueOf(message), parameters) : String.valueOf(message);
            switch (level) {
               case FATAL:
               case ERROR:
                  this.logger.error(text, thrown);
                  return;
               case WARN:
                  this.logger.warn(text, thrown);
                  return;
               case INFO:
                  this.logger.info(text, thrown);
                  return;
               case DEBUG:
                  this.logger.debug(text, thrown);
                  return;
               case TRACE:
                  this.logger.trace(text, thrown);
                  return;
            }
         } catch (Throwable var7) {
         }
      }

   }

   protected void doLogf(Logger.Level level, String loggerClassName, String format, Object[] parameters, Throwable thrown) {
      if (this.isEnabled(level)) {
         try {
            String text = parameters == null ? String.format(format) : String.format(format, parameters);
            switch (level) {
               case FATAL:
               case ERROR:
                  this.logger.error(text, thrown);
                  return;
               case WARN:
                  this.logger.warn(text, thrown);
                  return;
               case INFO:
                  this.logger.info(text, thrown);
                  return;
               case DEBUG:
                  this.logger.debug(text, thrown);
                  return;
               case TRACE:
                  this.logger.trace(text, thrown);
                  return;
            }
         } catch (Throwable var7) {
         }
      }

   }
}
