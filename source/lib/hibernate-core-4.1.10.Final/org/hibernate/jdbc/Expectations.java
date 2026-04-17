package org.hibernate.jdbc;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.StaleStateException;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class Expectations {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, Expectations.class.getName());
   private static SqlExceptionHelper sqlExceptionHelper = new SqlExceptionHelper();
   public static final int USUAL_EXPECTED_COUNT = 1;
   public static final int USUAL_PARAM_POSITION = 1;
   public static final Expectation NONE = new Expectation() {
      public void verifyOutcome(int rowCount, PreparedStatement statement, int batchPosition) {
      }

      public int prepare(PreparedStatement statement) {
         return 0;
      }

      public boolean canBeBatched() {
         return true;
      }
   };
   public static final Expectation BASIC = new BasicExpectation(1);
   public static final Expectation PARAM = new BasicParamExpectation(1, 1);

   public static Expectation appropriateExpectation(ExecuteUpdateResultCheckStyle style) {
      if (style == ExecuteUpdateResultCheckStyle.NONE) {
         return NONE;
      } else if (style == ExecuteUpdateResultCheckStyle.COUNT) {
         return BASIC;
      } else if (style == ExecuteUpdateResultCheckStyle.PARAM) {
         return PARAM;
      } else {
         throw new HibernateException("unknown check style : " + style);
      }
   }

   private Expectations() {
   }

   public static class BasicExpectation implements Expectation {
      private final int expectedRowCount;

      protected BasicExpectation(int expectedRowCount) {
         this.expectedRowCount = expectedRowCount;
         if (expectedRowCount < 0) {
            throw new IllegalArgumentException("Expected row count must be greater than zero");
         }
      }

      public final void verifyOutcome(int rowCount, PreparedStatement statement, int batchPosition) {
         rowCount = this.determineRowCount(rowCount, statement);
         if (batchPosition < 0) {
            this.checkNonBatched(rowCount);
         } else {
            this.checkBatched(rowCount, batchPosition);
         }

      }

      private void checkBatched(int rowCount, int batchPosition) {
         if (rowCount == -2) {
            Expectations.LOG.debugf("Success of batch update unknown: %s", batchPosition);
         } else {
            if (rowCount == -3) {
               throw new BatchFailedException("Batch update failed: " + batchPosition);
            }

            if (this.expectedRowCount > rowCount) {
               throw new StaleStateException("Batch update returned unexpected row count from update [" + batchPosition + "]; actual row count: " + rowCount + "; expected: " + this.expectedRowCount);
            }

            if (this.expectedRowCount < rowCount) {
               String msg = "Batch update returned unexpected row count from update [" + batchPosition + "]; actual row count: " + rowCount + "; expected: " + this.expectedRowCount;
               throw new BatchedTooManyRowsAffectedException(msg, this.expectedRowCount, rowCount, batchPosition);
            }
         }

      }

      private void checkNonBatched(int rowCount) {
         if (this.expectedRowCount > rowCount) {
            throw new StaleStateException("Unexpected row count: " + rowCount + "; expected: " + this.expectedRowCount);
         } else if (this.expectedRowCount < rowCount) {
            String msg = "Unexpected row count: " + rowCount + "; expected: " + this.expectedRowCount;
            throw new TooManyRowsAffectedException(msg, this.expectedRowCount, rowCount);
         }
      }

      public int prepare(PreparedStatement statement) throws SQLException, HibernateException {
         return 0;
      }

      public boolean canBeBatched() {
         return true;
      }

      protected int determineRowCount(int reportedRowCount, PreparedStatement statement) {
         return reportedRowCount;
      }
   }

   public static class BasicParamExpectation extends BasicExpectation {
      private final int parameterPosition;

      protected BasicParamExpectation(int expectedRowCount, int parameterPosition) {
         super(expectedRowCount);
         this.parameterPosition = parameterPosition;
      }

      public int prepare(PreparedStatement statement) throws SQLException, HibernateException {
         this.toCallableStatement(statement).registerOutParameter(this.parameterPosition, 2);
         return 1;
      }

      public boolean canBeBatched() {
         return false;
      }

      protected int determineRowCount(int reportedRowCount, PreparedStatement statement) {
         try {
            return this.toCallableStatement(statement).getInt(this.parameterPosition);
         } catch (SQLException sqle) {
            Expectations.sqlExceptionHelper.logExceptions(sqle, "could not extract row counts from CallableStatement");
            throw new GenericJDBCException("could not extract row counts from CallableStatement", sqle);
         }
      }

      private CallableStatement toCallableStatement(PreparedStatement statement) {
         if (!CallableStatement.class.isInstance(statement)) {
            throw new HibernateException("BasicParamExpectation operates exclusively on CallableStatements : " + statement.getClass());
         } else {
            return (CallableStatement)statement;
         }
      }
   }
}
