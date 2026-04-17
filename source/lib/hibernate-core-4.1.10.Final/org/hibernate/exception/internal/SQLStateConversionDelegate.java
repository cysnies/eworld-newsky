package org.hibernate.exception.internal;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.JDBCException;
import org.hibernate.PessimisticLockException;
import org.hibernate.QueryTimeoutException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.exception.spi.AbstractSQLExceptionConversionDelegate;
import org.hibernate.exception.spi.ConversionContext;
import org.hibernate.internal.util.JdbcExceptionHelper;

public class SQLStateConversionDelegate extends AbstractSQLExceptionConversionDelegate {
   private static final Set SQL_GRAMMAR_CATEGORIES = buildGrammarCategories();
   private static final Set DATA_CATEGORIES = buildDataCategories();
   private static final Set INTEGRITY_VIOLATION_CATEGORIES = buildContraintCategories();
   private static final Set CONNECTION_CATEGORIES = buildConnectionCategories();

   private static Set buildGrammarCategories() {
      HashSet<String> categories = new HashSet();
      categories.addAll(Arrays.asList("07", "20", "2A", "37", "42", "65", "S0"));
      return Collections.unmodifiableSet(categories);
   }

   private static Set buildDataCategories() {
      HashSet<String> categories = new HashSet();
      categories.addAll(Arrays.asList("21", "22"));
      return Collections.unmodifiableSet(categories);
   }

   private static Set buildContraintCategories() {
      HashSet<String> categories = new HashSet();
      categories.addAll(Arrays.asList("23", "27", "44"));
      return Collections.unmodifiableSet(categories);
   }

   private static Set buildConnectionCategories() {
      HashSet<String> categories = new HashSet();
      categories.add("08");
      return Collections.unmodifiableSet(categories);
   }

   public SQLStateConversionDelegate(ConversionContext conversionContext) {
      super(conversionContext);
   }

   public JDBCException convert(SQLException sqlException, String message, String sql) {
      String sqlState = JdbcExceptionHelper.extractSqlState(sqlException);
      if (sqlState != null) {
         String sqlStateClassCode = JdbcExceptionHelper.determineSqlStateClassCode(sqlState);
         if (sqlStateClassCode != null) {
            if (SQL_GRAMMAR_CATEGORIES.contains(sqlStateClassCode)) {
               return new SQLGrammarException(message, sqlException, sql);
            }

            if (INTEGRITY_VIOLATION_CATEGORIES.contains(sqlStateClassCode)) {
               String constraintName = this.getConversionContext().getViolatedConstraintNameExtracter().extractConstraintName(sqlException);
               return new ConstraintViolationException(message, sqlException, sql, constraintName);
            }

            if (CONNECTION_CATEGORIES.contains(sqlStateClassCode)) {
               return new JDBCConnectionException(message, sqlException, sql);
            }

            if (DATA_CATEGORIES.contains(sqlStateClassCode)) {
               return new DataException(message, sqlException, sql);
            }
         }

         if ("40001".equals(sqlState)) {
            return new LockAcquisitionException(message, sqlException, sql);
         }

         if ("40XL1".equals(sqlState) || "40XL2".equals(sqlState)) {
            return new PessimisticLockException(message, sqlException, sql);
         }

         if ("70100".equals(sqlState) || "72000".equals(sqlState)) {
            throw new QueryTimeoutException(message, sqlException, sql);
         }
      }

      return null;
   }
}
