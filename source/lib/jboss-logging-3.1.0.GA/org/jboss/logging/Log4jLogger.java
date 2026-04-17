package org.jboss.logging;

import java.text.MessageFormat;

final class Log4jLogger extends Logger {
   private static final long serialVersionUID = -5446154366955151335L;
   private final org.apache.log4j.Logger logger;

   Log4jLogger(String name) {
      super(name);
      this.logger = org.apache.log4j.Logger.getLogger(name);
   }

   public boolean isEnabled(Logger.Level level) {
      org.apache.log4j.Level l = translate(level);
      return this.logger.isEnabledFor(l) && l.isGreaterOrEqual(this.logger.getEffectiveLevel());
   }

   protected void doLog(Logger.Level level, String loggerClassName, Object message, Object[] parameters, Throwable thrown) {
      org.apache.log4j.Level translatedLevel = translate(level);
      if (this.logger.isEnabledFor(translatedLevel)) {
         try {
            this.logger.log(loggerClassName, translatedLevel, parameters != null && parameters.length != 0 ? MessageFormat.format(String.valueOf(message), parameters) : message, thrown);
         } catch (Throwable var8) {
         }
      }

   }

   protected void doLogf(Logger.Level level, String loggerClassName, String format, Object[] parameters, Throwable thrown) {
      org.apache.log4j.Level translatedLevel = translate(level);
      if (this.logger.isEnabledFor(translatedLevel)) {
         try {
            this.logger.log(loggerClassName, translatedLevel, parameters == null ? String.format(format) : String.format(format, parameters), thrown);
         } catch (Throwable var8) {
         }
      }

   }

   private static org.apache.log4j.Level translate(Logger.Level level) {
      if (level != null) {
         switch (level) {
            case FATAL:
               return org.apache.log4j.Level.FATAL;
            case ERROR:
               return org.apache.log4j.Level.ERROR;
            case WARN:
               return org.apache.log4j.Level.WARN;
            case INFO:
               return org.apache.log4j.Level.INFO;
            case DEBUG:
               return org.apache.log4j.Level.DEBUG;
            case TRACE:
               return org.apache.log4j.Level.TRACE;
         }
      }

      return org.apache.log4j.Level.ALL;
   }
}
