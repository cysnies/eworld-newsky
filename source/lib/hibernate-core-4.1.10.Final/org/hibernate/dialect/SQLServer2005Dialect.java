package org.hibernate.dialect;

import java.sql.SQLException;
import org.hibernate.JDBCException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.QueryTimeoutException;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.SQLServer2005LimitHandler;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.exception.LockTimeoutException;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.internal.util.JdbcExceptionHelper;
import org.hibernate.type.StandardBasicTypes;

public class SQLServer2005Dialect extends SQLServerDialect {
   private static final int MAX_LENGTH = 8000;

   public SQLServer2005Dialect() {
      this.registerColumnType(2004, "varbinary(MAX)");
      this.registerColumnType(-3, "varbinary(MAX)");
      this.registerColumnType(-3, 8000L, "varbinary($l)");
      this.registerColumnType(-4, "varbinary(MAX)");
      this.registerColumnType(2005, "varchar(MAX)");
      this.registerColumnType(-1, "varchar(MAX)");
      this.registerColumnType(12, "varchar(MAX)");
      this.registerColumnType(12, 8000L, "varchar($l)");
      this.registerColumnType(-5, "bigint");
      this.registerColumnType(-7, "bit");
      this.registerFunction("row_number", new NoArgSQLFunction("row_number", StandardBasicTypes.INTEGER, true));
   }

   public LimitHandler buildLimitHandler(String sql, RowSelection selection) {
      return new SQLServer2005LimitHandler(sql, selection);
   }

   public String appendLockHint(LockOptions lockOptions, String tableName) {
      if (lockOptions.getLockMode() == LockMode.UPGRADE_NOWAIT) {
         return tableName + " with (updlock, rowlock, nowait)";
      } else {
         LockMode mode = lockOptions.getLockMode();
         boolean isNoWait = lockOptions.getTimeOut() == 0;
         String noWaitStr = isNoWait ? ", nowait" : "";
         switch (mode) {
            case UPGRADE_NOWAIT:
               return tableName + " with (updlock, rowlock, nowait)";
            case UPGRADE:
            case PESSIMISTIC_WRITE:
            case WRITE:
               return tableName + " with (updlock, rowlock" + noWaitStr + " )";
            case PESSIMISTIC_READ:
               return tableName + " with (holdlock, rowlock" + noWaitStr + " )";
            default:
               return tableName;
         }
      }
   }

   public SQLExceptionConversionDelegate buildSQLExceptionConversionDelegate() {
      return new SQLExceptionConversionDelegate() {
         public JDBCException convert(SQLException sqlException, String message, String sql) {
            String sqlState = JdbcExceptionHelper.extractSqlState(sqlException);
            int errorCode = JdbcExceptionHelper.extractErrorCode(sqlException);
            if ("HY008".equals(sqlState)) {
               throw new QueryTimeoutException(message, sqlException, sql);
            } else if (1222 == errorCode) {
               throw new LockTimeoutException(message, sqlException, sql);
            } else {
               return null;
            }
         }
      };
   }
}
