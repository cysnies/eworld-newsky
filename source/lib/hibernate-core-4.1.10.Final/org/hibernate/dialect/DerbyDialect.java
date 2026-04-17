package org.hibernate.dialect;

import java.lang.reflect.Method;
import org.hibernate.MappingException;
import org.hibernate.dialect.function.AnsiTrimFunction;
import org.hibernate.dialect.function.DerbyConcatFunction;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.sql.CaseFragment;
import org.hibernate.sql.DerbyCaseFragment;
import org.jboss.logging.Logger;

/** @deprecated */
@Deprecated
public class DerbyDialect extends DB2Dialect {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DerbyDialect.class.getName());
   private int driverVersionMajor;
   private int driverVersionMinor;

   public DerbyDialect() {
      if (this.getClass() == DerbyDialect.class) {
         LOG.deprecatedDerbyDialect();
      }

      this.registerFunction("concat", new DerbyConcatFunction());
      this.registerFunction("trim", new AnsiTrimFunction());
      this.registerColumnType(2004, "blob");
      this.determineDriverVersion();
      if (this.driverVersionMajor > 10 || this.driverVersionMajor == 10 && this.driverVersionMinor >= 7) {
         this.registerColumnType(16, "boolean");
      }

   }

   private void determineDriverVersion() {
      try {
         Class sysinfoClass = ReflectHelper.classForName("org.apache.derby.tools.sysinfo", this.getClass());
         Method majorVersionGetter = sysinfoClass.getMethod("getMajorVersion", ReflectHelper.NO_PARAM_SIGNATURE);
         Method minorVersionGetter = sysinfoClass.getMethod("getMinorVersion", ReflectHelper.NO_PARAM_SIGNATURE);
         this.driverVersionMajor = (Integer)majorVersionGetter.invoke((Object)null, ReflectHelper.NO_PARAMS);
         this.driverVersionMinor = (Integer)minorVersionGetter.invoke((Object)null, ReflectHelper.NO_PARAMS);
      } catch (Exception e) {
         LOG.unableToLoadDerbyDriver(e.getMessage());
         this.driverVersionMajor = -1;
         this.driverVersionMinor = -1;
      }

   }

   private boolean isTenPointFiveReleaseOrNewer() {
      return this.driverVersionMajor > 10 || this.driverVersionMajor == 10 && this.driverVersionMinor >= 5;
   }

   public String getCrossJoinSeparator() {
      return ", ";
   }

   public CaseFragment createCaseFragment() {
      return new DerbyCaseFragment();
   }

   public boolean dropConstraints() {
      return true;
   }

   public boolean supportsSequences() {
      return this.driverVersionMajor > 10 || this.driverVersionMajor == 10 && this.driverVersionMinor >= 6;
   }

   public String getSequenceNextValString(String sequenceName) {
      if (this.supportsSequences()) {
         return "values next value for " + sequenceName;
      } else {
         throw new MappingException("Derby does not support sequence prior to release 10.6.1.0");
      }
   }

   public boolean supportsLimit() {
      return this.isTenPointFiveReleaseOrNewer();
   }

   public boolean supportsCommentOn() {
      return false;
   }

   public boolean supportsLimitOffset() {
      return this.isTenPointFiveReleaseOrNewer();
   }

   public String getForUpdateString() {
      return " for update with rs";
   }

   public String getWriteLockString(int timeout) {
      return " for update with rs";
   }

   public String getReadLockString(int timeout) {
      return " for read only with rs";
   }

   public String getLimitString(String query, int offset, int limit) {
      StringBuilder sb = new StringBuilder(query.length() + 50);
      String normalizedSelect = query.toLowerCase().trim();
      int forUpdateIndex = normalizedSelect.lastIndexOf("for update");
      if (this.hasForUpdateClause(forUpdateIndex)) {
         sb.append(query.substring(0, forUpdateIndex - 1));
      } else if (this.hasWithClause(normalizedSelect)) {
         sb.append(query.substring(0, this.getWithIndex(query) - 1));
      } else {
         sb.append(query);
      }

      if (offset == 0) {
         sb.append(" fetch first ");
      } else {
         sb.append(" offset ").append(offset).append(" rows fetch next ");
      }

      sb.append(limit).append(" rows only");
      if (this.hasForUpdateClause(forUpdateIndex)) {
         sb.append(' ');
         sb.append(query.substring(forUpdateIndex));
      } else if (this.hasWithClause(normalizedSelect)) {
         sb.append(' ').append(query.substring(this.getWithIndex(query)));
      }

      return sb.toString();
   }

   public boolean supportsVariableLimit() {
      return false;
   }

   private boolean hasForUpdateClause(int forUpdateIndex) {
      return forUpdateIndex >= 0;
   }

   private boolean hasWithClause(String normalizedSelect) {
      return normalizedSelect.startsWith("with ", normalizedSelect.length() - 7);
   }

   private int getWithIndex(String querySelect) {
      int i = querySelect.lastIndexOf("with ");
      if (i < 0) {
         i = querySelect.lastIndexOf("WITH ");
      }

      return i;
   }

   public String getQuerySequencesString() {
      return null;
   }

   public boolean supportsLobValueChangePropogation() {
      return false;
   }

   public boolean supportsUnboundedLobLocatorMaterialization() {
      return false;
   }
}
