package com.mysql.jdbc.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Log4JLogger implements Log {
   private Logger logger;

   public Log4JLogger(String instanceName) {
      this.logger = Logger.getLogger(instanceName);
   }

   public boolean isDebugEnabled() {
      return this.logger.isDebugEnabled();
   }

   public boolean isErrorEnabled() {
      return this.logger.isEnabledFor(Level.ERROR);
   }

   public boolean isFatalEnabled() {
      return this.logger.isEnabledFor(Level.FATAL);
   }

   public boolean isInfoEnabled() {
      return this.logger.isInfoEnabled();
   }

   public boolean isTraceEnabled() {
      return this.logger.isDebugEnabled();
   }

   public boolean isWarnEnabled() {
      return this.logger.isEnabledFor(Level.WARN);
   }

   public void logDebug(Object msg) {
      this.logger.debug(LogUtils.expandProfilerEventIfNecessary(LogUtils.expandProfilerEventIfNecessary(msg)));
   }

   public void logDebug(Object msg, Throwable thrown) {
      this.logger.debug(LogUtils.expandProfilerEventIfNecessary(msg), thrown);
   }

   public void logError(Object msg) {
      this.logger.error(LogUtils.expandProfilerEventIfNecessary(msg));
   }

   public void logError(Object msg, Throwable thrown) {
      this.logger.error(LogUtils.expandProfilerEventIfNecessary(msg), thrown);
   }

   public void logFatal(Object msg) {
      this.logger.fatal(LogUtils.expandProfilerEventIfNecessary(msg));
   }

   public void logFatal(Object msg, Throwable thrown) {
      this.logger.fatal(LogUtils.expandProfilerEventIfNecessary(msg), thrown);
   }

   public void logInfo(Object msg) {
      this.logger.info(LogUtils.expandProfilerEventIfNecessary(msg));
   }

   public void logInfo(Object msg, Throwable thrown) {
      this.logger.info(LogUtils.expandProfilerEventIfNecessary(msg), thrown);
   }

   public void logTrace(Object msg) {
      this.logger.debug(LogUtils.expandProfilerEventIfNecessary(msg));
   }

   public void logTrace(Object msg, Throwable thrown) {
      this.logger.debug(LogUtils.expandProfilerEventIfNecessary(msg), thrown);
   }

   public void logWarn(Object msg) {
      this.logger.warn(LogUtils.expandProfilerEventIfNecessary(msg));
   }

   public void logWarn(Object msg, Throwable thrown) {
      this.logger.warn(LogUtils.expandProfilerEventIfNecessary(msg), thrown);
   }
}
