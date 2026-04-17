package org.hibernate.dialect;

import java.sql.SQLException;
import java.util.Map;
import org.hibernate.JDBCException;
import org.hibernate.LockOptions;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.exception.LockTimeoutException;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.internal.util.JdbcExceptionHelper;
import org.hibernate.sql.ForUpdateFragment;
import org.hibernate.type.StandardBasicTypes;

public class SybaseASE157Dialect extends SybaseASE15Dialect {
   public SybaseASE157Dialect() {
      this.registerFunction("create_locator", new SQLFunctionTemplate(StandardBasicTypes.BINARY, "create_locator(?1, ?2)"));
      this.registerFunction("locator_literal", new SQLFunctionTemplate(StandardBasicTypes.BINARY, "locator_literal(?1, ?2)"));
      this.registerFunction("locator_valid", new SQLFunctionTemplate(StandardBasicTypes.BOOLEAN, "locator_valid(?1)"));
      this.registerFunction("return_lob", new SQLFunctionTemplate(StandardBasicTypes.BINARY, "return_lob(?1, ?2)"));
      this.registerFunction("setdata", new SQLFunctionTemplate(StandardBasicTypes.BOOLEAN, "setdata(?1, ?2, ?3)"));
      this.registerFunction("charindex", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "charindex(?1, ?2, ?3)"));
   }

   public String getTableTypeString() {
      return " lock datarows";
   }

   public boolean supportsExpectedLobUsagePattern() {
      return true;
   }

   public boolean supportsLobValueChangePropogation() {
      return false;
   }

   public boolean forUpdateOfColumns() {
      return true;
   }

   public String getForUpdateString() {
      return " for update";
   }

   public String getForUpdateString(String aliases) {
      return this.getForUpdateString() + " of " + aliases;
   }

   public String appendLockHint(LockOptions mode, String tableName) {
      return tableName;
   }

   public String applyLocksToSql(String sql, LockOptions aliasedLockOptions, Map keyColumnNames) {
      return sql + (new ForUpdateFragment(this, aliasedLockOptions, keyColumnNames)).toFragmentString();
   }

   public SQLExceptionConversionDelegate buildSQLExceptionConversionDelegate() {
      return new SQLExceptionConversionDelegate() {
         public JDBCException convert(SQLException sqlException, String message, String sql) {
            String sqlState = JdbcExceptionHelper.extractSqlState(sqlException);
            if (!"JZ0TO".equals(sqlState) && !"JZ006".equals(sqlState)) {
               return null;
            } else {
               throw new LockTimeoutException(message, sqlException, sql);
            }
         }
      };
   }
}
