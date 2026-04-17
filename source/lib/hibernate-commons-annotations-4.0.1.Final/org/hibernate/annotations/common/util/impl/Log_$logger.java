package org.hibernate.annotations.common.util.impl;

import java.io.Serializable;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

public class Log_$logger implements Serializable, Log, BasicLogger {
   private static final long serialVersionUID = 1L;
   private static final String projectCode = "HCANN";
   private static final String FQCN = Log_$logger.class.getName();
   protected final Logger log;
   private static final String assertionFailure = "An assertion failure occurred (this may indicate a bug in Hibernate)";
   private static final String version = "Hibernate Commons Annotations {%1$s}";

   public Log_$logger(Logger log) {
      this.log = log;
   }

   public final boolean isTraceEnabled() {
      return this.log.isTraceEnabled();
   }

   public final void trace(Object message) {
      this.log.trace(FQCN, message, (Throwable)null);
   }

   public final void trace(Object message, Throwable t) {
      this.log.trace(FQCN, message, t);
   }

   public final void trace(String loggerFqcn, Object message, Throwable t) {
      this.log.trace(loggerFqcn, message, t);
   }

   public final void trace(String loggerFqcn, Object message, Object[] params, Throwable t) {
      this.log.trace(loggerFqcn, message, params, t);
   }

   public final void tracev(String format, Object... params) {
      this.log.logv(FQCN, Level.TRACE, (Throwable)null, format, params);
   }

   public final void tracev(String format, Object param1) {
      this.log.logv(FQCN, Level.TRACE, (Throwable)null, format, param1);
   }

   public final void tracev(String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.TRACE, (Throwable)null, format, param1, param2);
   }

   public final void tracev(String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.TRACE, (Throwable)null, format, param1, param2, param3);
   }

   public final void tracev(Throwable t, String format, Object... params) {
      this.log.logv(FQCN, Level.TRACE, t, format, params);
   }

   public final void tracev(Throwable t, String format, Object param1) {
      this.log.logv(FQCN, Level.TRACE, t, format, param1);
   }

   public final void tracev(Throwable t, String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.TRACE, t, format, param1, param2);
   }

   public final void tracev(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.TRACE, t, format, param1, param2, param3);
   }

   public final void tracef(String format, Object... params) {
      this.log.logf(FQCN, Level.TRACE, (Throwable)null, format, params);
   }

   public final void tracef(String format, Object param1) {
      this.log.logf(FQCN, Level.TRACE, (Throwable)null, format, param1);
   }

   public final void tracef(String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.TRACE, (Throwable)null, format, param1, param2);
   }

   public final void tracef(String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.TRACE, (Throwable)null, format, param1, param2, param3);
   }

   public final void tracef(Throwable t, String format, Object... params) {
      this.log.logf(FQCN, Level.TRACE, t, format, params);
   }

   public final void tracef(Throwable t, String format, Object param1) {
      this.log.logf(FQCN, Level.TRACE, t, format, param1);
   }

   public final void tracef(Throwable t, String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.TRACE, t, format, param1, param2);
   }

   public final void tracef(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.TRACE, t, format, param1, param2, param3);
   }

   public final boolean isDebugEnabled() {
      return this.log.isDebugEnabled();
   }

   public final void debug(Object message) {
      this.log.debug(FQCN, message, (Throwable)null);
   }

   public final void debug(Object message, Throwable t) {
      this.log.debug(FQCN, message, t);
   }

   public final void debug(String loggerFqcn, Object message, Throwable t) {
      this.log.debug(loggerFqcn, message, t);
   }

   public final void debug(String loggerFqcn, Object message, Object[] params, Throwable t) {
      this.log.debug(loggerFqcn, message, params, t);
   }

   public final void debugv(String format, Object... params) {
      this.log.logv(FQCN, Level.DEBUG, (Throwable)null, format, params);
   }

   public final void debugv(String format, Object param1) {
      this.log.logv(FQCN, Level.DEBUG, (Throwable)null, format, param1);
   }

   public final void debugv(String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.DEBUG, (Throwable)null, format, param1, param2);
   }

   public final void debugv(String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.DEBUG, (Throwable)null, format, param1, param2, param3);
   }

   public final void debugv(Throwable t, String format, Object... params) {
      this.log.logv(FQCN, Level.DEBUG, t, format, params);
   }

   public final void debugv(Throwable t, String format, Object param1) {
      this.log.logv(FQCN, Level.DEBUG, t, format, param1);
   }

   public final void debugv(Throwable t, String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.DEBUG, t, format, param1, param2);
   }

   public final void debugv(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.DEBUG, t, format, param1, param2, param3);
   }

   public final void debugf(String format, Object... params) {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, format, params);
   }

   public final void debugf(String format, Object param1) {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, format, param1);
   }

   public final void debugf(String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, format, param1, param2);
   }

   public final void debugf(String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.DEBUG, (Throwable)null, format, param1, param2, param3);
   }

   public final void debugf(Throwable t, String format, Object... params) {
      this.log.logf(FQCN, Level.DEBUG, t, format, params);
   }

   public final void debugf(Throwable t, String format, Object param1) {
      this.log.logf(FQCN, Level.DEBUG, t, format, param1);
   }

   public final void debugf(Throwable t, String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.DEBUG, t, format, param1, param2);
   }

   public final void debugf(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.DEBUG, t, format, param1, param2, param3);
   }

   public final boolean isInfoEnabled() {
      return this.log.isInfoEnabled();
   }

   public final void info(Object message) {
      this.log.info(FQCN, message, (Throwable)null);
   }

   public final void info(Object message, Throwable t) {
      this.log.info(FQCN, message, t);
   }

   public final void info(String loggerFqcn, Object message, Throwable t) {
      this.log.info(loggerFqcn, message, t);
   }

   public final void info(String loggerFqcn, Object message, Object[] params, Throwable t) {
      this.log.info(loggerFqcn, message, params, t);
   }

   public final void infov(String format, Object... params) {
      this.log.logv(FQCN, Level.INFO, (Throwable)null, format, params);
   }

   public final void infov(String format, Object param1) {
      this.log.logv(FQCN, Level.INFO, (Throwable)null, format, param1);
   }

   public final void infov(String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.INFO, (Throwable)null, format, param1, param2);
   }

   public final void infov(String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.INFO, (Throwable)null, format, param1, param2, param3);
   }

   public final void infov(Throwable t, String format, Object... params) {
      this.log.logv(FQCN, Level.INFO, t, format, params);
   }

   public final void infov(Throwable t, String format, Object param1) {
      this.log.logv(FQCN, Level.INFO, t, format, param1);
   }

   public final void infov(Throwable t, String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.INFO, t, format, param1, param2);
   }

   public final void infov(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.INFO, t, format, param1, param2, param3);
   }

   public final void infof(String format, Object... params) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, format, params);
   }

   public final void infof(String format, Object param1) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, format, param1);
   }

   public final void infof(String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, format, param1, param2);
   }

   public final void infof(String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, format, param1, param2, param3);
   }

   public final void infof(Throwable t, String format, Object... params) {
      this.log.logf(FQCN, Level.INFO, t, format, params);
   }

   public final void infof(Throwable t, String format, Object param1) {
      this.log.logf(FQCN, Level.INFO, t, format, param1);
   }

   public final void infof(Throwable t, String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.INFO, t, format, param1, param2);
   }

   public final void infof(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.INFO, t, format, param1, param2, param3);
   }

   public final void warn(Object message) {
      this.log.warn(FQCN, message, (Throwable)null);
   }

   public final void warn(Object message, Throwable t) {
      this.log.warn(FQCN, message, t);
   }

   public final void warn(String loggerFqcn, Object message, Throwable t) {
      this.log.warn(loggerFqcn, message, t);
   }

   public final void warn(String loggerFqcn, Object message, Object[] params, Throwable t) {
      this.log.warn(loggerFqcn, message, params, t);
   }

   public final void warnv(String format, Object... params) {
      this.log.logv(FQCN, Level.WARN, (Throwable)null, format, params);
   }

   public final void warnv(String format, Object param1) {
      this.log.logv(FQCN, Level.WARN, (Throwable)null, format, param1);
   }

   public final void warnv(String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.WARN, (Throwable)null, format, param1, param2);
   }

   public final void warnv(String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.WARN, (Throwable)null, format, param1, param2, param3);
   }

   public final void warnv(Throwable t, String format, Object... params) {
      this.log.logv(FQCN, Level.WARN, t, format, params);
   }

   public final void warnv(Throwable t, String format, Object param1) {
      this.log.logv(FQCN, Level.WARN, t, format, param1);
   }

   public final void warnv(Throwable t, String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.WARN, t, format, param1, param2);
   }

   public final void warnv(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.WARN, t, format, param1, param2, param3);
   }

   public final void warnf(String format, Object... params) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, format, params);
   }

   public final void warnf(String format, Object param1) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, format, param1);
   }

   public final void warnf(String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, format, param1, param2);
   }

   public final void warnf(String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.WARN, (Throwable)null, format, param1, param2, param3);
   }

   public final void warnf(Throwable t, String format, Object... params) {
      this.log.logf(FQCN, Level.WARN, t, format, params);
   }

   public final void warnf(Throwable t, String format, Object param1) {
      this.log.logf(FQCN, Level.WARN, t, format, param1);
   }

   public final void warnf(Throwable t, String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.WARN, t, format, param1, param2);
   }

   public final void warnf(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.WARN, t, format, param1, param2, param3);
   }

   public final void error(Object message) {
      this.log.error(FQCN, message, (Throwable)null);
   }

   public final void error(Object message, Throwable t) {
      this.log.error(FQCN, message, t);
   }

   public final void error(String loggerFqcn, Object message, Throwable t) {
      this.log.error(loggerFqcn, message, t);
   }

   public final void error(String loggerFqcn, Object message, Object[] params, Throwable t) {
      this.log.error(loggerFqcn, message, params, t);
   }

   public final void errorv(String format, Object... params) {
      this.log.logv(FQCN, Level.ERROR, (Throwable)null, format, params);
   }

   public final void errorv(String format, Object param1) {
      this.log.logv(FQCN, Level.ERROR, (Throwable)null, format, param1);
   }

   public final void errorv(String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.ERROR, (Throwable)null, format, param1, param2);
   }

   public final void errorv(String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.ERROR, (Throwable)null, format, param1, param2, param3);
   }

   public final void errorv(Throwable t, String format, Object... params) {
      this.log.logv(FQCN, Level.ERROR, t, format, params);
   }

   public final void errorv(Throwable t, String format, Object param1) {
      this.log.logv(FQCN, Level.ERROR, t, format, param1);
   }

   public final void errorv(Throwable t, String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.ERROR, t, format, param1, param2);
   }

   public final void errorv(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.ERROR, t, format, param1, param2, param3);
   }

   public final void errorf(String format, Object... params) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, format, params);
   }

   public final void errorf(String format, Object param1) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, format, param1);
   }

   public final void errorf(String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, format, param1, param2);
   }

   public final void errorf(String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.ERROR, (Throwable)null, format, param1, param2, param3);
   }

   public final void errorf(Throwable t, String format, Object... params) {
      this.log.logf(FQCN, Level.ERROR, t, format, params);
   }

   public final void errorf(Throwable t, String format, Object param1) {
      this.log.logf(FQCN, Level.ERROR, t, format, param1);
   }

   public final void errorf(Throwable t, String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.ERROR, t, format, param1, param2);
   }

   public final void errorf(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.ERROR, t, format, param1, param2, param3);
   }

   public final void fatal(Object message) {
      this.log.fatal(FQCN, message, (Throwable)null);
   }

   public final void fatal(Object message, Throwable t) {
      this.log.fatal(FQCN, message, t);
   }

   public final void fatal(String loggerFqcn, Object message, Throwable t) {
      this.log.fatal(loggerFqcn, message, t);
   }

   public final void fatal(String loggerFqcn, Object message, Object[] params, Throwable t) {
      this.log.fatal(loggerFqcn, message, params, t);
   }

   public final void fatalv(String format, Object... params) {
      this.log.logv(FQCN, Level.FATAL, (Throwable)null, format, params);
   }

   public final void fatalv(String format, Object param1) {
      this.log.logv(FQCN, Level.FATAL, (Throwable)null, format, param1);
   }

   public final void fatalv(String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.FATAL, (Throwable)null, format, param1, param2);
   }

   public final void fatalv(String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.FATAL, (Throwable)null, format, param1, param2, param3);
   }

   public final void fatalv(Throwable t, String format, Object... params) {
      this.log.logv(FQCN, Level.FATAL, t, format, params);
   }

   public final void fatalv(Throwable t, String format, Object param1) {
      this.log.logv(FQCN, Level.FATAL, t, format, param1);
   }

   public final void fatalv(Throwable t, String format, Object param1, Object param2) {
      this.log.logv(FQCN, Level.FATAL, t, format, param1, param2);
   }

   public final void fatalv(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, Level.FATAL, t, format, param1, param2, param3);
   }

   public final void fatalf(String format, Object... params) {
      this.log.logf(FQCN, Level.FATAL, (Throwable)null, format, params);
   }

   public final void fatalf(String format, Object param1) {
      this.log.logf(FQCN, Level.FATAL, (Throwable)null, format, param1);
   }

   public final void fatalf(String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.FATAL, (Throwable)null, format, param1, param2);
   }

   public final void fatalf(String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.FATAL, (Throwable)null, format, param1, param2, param3);
   }

   public final void fatalf(Throwable t, String format, Object... params) {
      this.log.logf(FQCN, Level.FATAL, t, format, params);
   }

   public final void fatalf(Throwable t, String format, Object param1) {
      this.log.logf(FQCN, Level.FATAL, t, format, param1);
   }

   public final void fatalf(Throwable t, String format, Object param1, Object param2) {
      this.log.logf(FQCN, Level.FATAL, t, format, param1, param2);
   }

   public final void fatalf(Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, Level.FATAL, t, format, param1, param2, param3);
   }

   public final boolean isEnabled(Logger.Level level) {
      return this.log.isEnabled(level);
   }

   public final void log(Logger.Level level, Object message) {
      this.log.log(FQCN, level, message, (Object[])null, (Throwable)null);
   }

   public final void log(Logger.Level level, Object message, Throwable t) {
      this.log.log(FQCN, level, message, (Object[])null, t);
   }

   public final void log(Logger.Level level, String loggerFqcn, Object message, Throwable t) {
      this.log.log(level, loggerFqcn, message, t);
   }

   public final void log(String loggerFqcn, Logger.Level level, Object message, Object[] params, Throwable t) {
      this.log.log(loggerFqcn, level, message, params, t);
   }

   public final void logv(Logger.Level level, String format, Object... params) {
      this.log.logv(FQCN, level, (Throwable)null, format, params);
   }

   public final void logv(Logger.Level level, String format, Object param1) {
      this.log.logv(FQCN, level, (Throwable)null, format, param1);
   }

   public final void logv(Logger.Level level, String format, Object param1, Object param2) {
      this.log.logv(FQCN, level, (Throwable)null, format, param1, param2);
   }

   public final void logv(Logger.Level level, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, level, (Throwable)null, format, param1, param2, param3);
   }

   public final void logv(Logger.Level level, Throwable t, String format, Object... params) {
      this.log.logv(FQCN, level, t, format, params);
   }

   public final void logv(Logger.Level level, Throwable t, String format, Object param1) {
      this.log.logv(FQCN, level, t, format, param1);
   }

   public final void logv(Logger.Level level, Throwable t, String format, Object param1, Object param2) {
      this.log.logv(FQCN, level, t, format, param1, param2);
   }

   public final void logv(Logger.Level level, Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(FQCN, level, t, format, param1, param2, param3);
   }

   public final void logv(String loggerFqcn, Logger.Level level, Throwable t, String format, Object... params) {
      this.log.logv(loggerFqcn, level, t, format, params);
   }

   public final void logv(String loggerFqcn, Logger.Level level, Throwable t, String format, Object param1) {
      this.log.logv(loggerFqcn, level, t, format, param1);
   }

   public final void logv(String loggerFqcn, Logger.Level level, Throwable t, String format, Object param1, Object param2) {
      this.log.logv(loggerFqcn, level, t, format, param1, param2);
   }

   public final void logv(String loggerFqcn, Logger.Level level, Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logv(loggerFqcn, level, t, format, param1, param2, param3);
   }

   public final void logf(Logger.Level level, String format, Object... params) {
      this.log.logf(FQCN, level, (Throwable)null, format, params);
   }

   public final void logf(Logger.Level level, String format, Object param1) {
      this.log.logf(FQCN, level, (Throwable)null, format, param1);
   }

   public final void logf(Logger.Level level, String format, Object param1, Object param2) {
      this.log.logf(FQCN, level, (Throwable)null, format, param1, param2);
   }

   public final void logf(Logger.Level level, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, level, (Throwable)null, format, param1, param2, param3);
   }

   public final void logf(Logger.Level level, Throwable t, String format, Object... params) {
      this.log.logf(FQCN, level, t, format, params);
   }

   public final void logf(Logger.Level level, Throwable t, String format, Object param1) {
      this.log.logf(FQCN, level, t, format, param1);
   }

   public final void logf(Logger.Level level, Throwable t, String format, Object param1, Object param2) {
      this.log.logf(FQCN, level, t, format, param1, param2);
   }

   public final void logf(Logger.Level level, Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(FQCN, level, t, format, param1, param2, param3);
   }

   public final void logf(String loggerFqcn, Logger.Level level, Throwable t, String format, Object... params) {
      this.log.logf(loggerFqcn, level, t, format, params);
   }

   public final void logf(String loggerFqcn, Logger.Level level, Throwable t, String format, Object param1) {
      this.log.logf(loggerFqcn, level, t, format, param1);
   }

   public final void logf(String loggerFqcn, Logger.Level level, Throwable t, String format, Object param1, Object param2) {
      this.log.logf(loggerFqcn, level, t, format, param1, param2);
   }

   public final void logf(String loggerFqcn, Logger.Level level, Throwable t, String format, Object param1, Object param2, Object param3) {
      this.log.logf(loggerFqcn, level, t, format, param1, param2, param3);
   }

   public final void assertionFailure(Throwable t) {
      this.log.logf(FQCN, Level.ERROR, t, "HCANN000002: " + this.assertionFailure$str(), new Object[0]);
   }

   protected String assertionFailure$str() {
      return "An assertion failure occurred (this may indicate a bug in Hibernate)";
   }

   public final void version(String version) {
      this.log.logf(FQCN, Level.INFO, (Throwable)null, "HCANN000001: " + this.version$str(), version);
   }

   protected String version$str() {
      return "Hibernate Commons Annotations {%1$s}";
   }
}
