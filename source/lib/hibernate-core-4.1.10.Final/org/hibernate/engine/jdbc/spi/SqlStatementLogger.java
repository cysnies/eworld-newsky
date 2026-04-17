package org.hibernate.engine.jdbc.spi;

import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class SqlStatementLogger {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, "org.hibernate.SQL");
   private boolean logToStdout;
   private boolean format;

   public SqlStatementLogger() {
      this(false, false);
   }

   public SqlStatementLogger(boolean logToStdout, boolean format) {
      this.logToStdout = logToStdout;
      this.format = format;
   }

   public boolean isLogToStdout() {
      return this.logToStdout;
   }

   public void setLogToStdout(boolean logToStdout) {
      this.logToStdout = logToStdout;
   }

   public boolean isFormat() {
      return this.format;
   }

   public void setFormat(boolean format) {
      this.format = format;
   }

   public void logStatement(String statement) {
      this.logStatement(statement, FormatStyle.BASIC.getFormatter());
   }

   public void logStatement(String statement, Formatter formatter) {
      if (this.format && (this.logToStdout || LOG.isDebugEnabled())) {
         statement = formatter.format(statement);
      }

      LOG.debug(statement);
      if (this.logToStdout) {
         System.out.println("Hibernate: " + statement);
      }

   }
}
