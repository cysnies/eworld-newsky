package org.hibernate.dialect;

import java.sql.SQLException;
import org.hibernate.MappingException;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.exception.spi.TemplatedViolatedConstraintNameExtracter;
import org.hibernate.exception.spi.ViolatedConstraintNameExtracter;
import org.hibernate.internal.util.JdbcExceptionHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.StandardBasicTypes;

public class InformixDialect extends Dialect {
   private static ViolatedConstraintNameExtracter EXTRACTER = new TemplatedViolatedConstraintNameExtracter() {
      public String extractConstraintName(SQLException sqle) {
         String constraintName = null;
         int errorCode = JdbcExceptionHelper.extractErrorCode(sqle);
         if (errorCode == -268) {
            constraintName = this.extractUsingTemplate("Unique constraint (", ") violated.", sqle.getMessage());
         } else if (errorCode == -691) {
            constraintName = this.extractUsingTemplate("Missing key in referenced table for referential constraint (", ").", sqle.getMessage());
         } else if (errorCode == -692) {
            constraintName = this.extractUsingTemplate("Key value for constraint (", ") is still being referenced.", sqle.getMessage());
         }

         if (constraintName != null) {
            int i = constraintName.indexOf(46);
            if (i != -1) {
               constraintName = constraintName.substring(i + 1);
            }
         }

         return constraintName;
      }
   };

   public InformixDialect() {
      this.registerColumnType(-5, "int8");
      this.registerColumnType(-2, "byte");
      this.registerColumnType(-7, "smallint");
      this.registerColumnType(1, "char($l)");
      this.registerColumnType(91, "date");
      this.registerColumnType(3, "decimal");
      this.registerColumnType(8, "float");
      this.registerColumnType(6, "smallfloat");
      this.registerColumnType(4, "integer");
      this.registerColumnType(-4, "blob");
      this.registerColumnType(-1, "clob");
      this.registerColumnType(2, "decimal");
      this.registerColumnType(7, "smallfloat");
      this.registerColumnType(5, "smallint");
      this.registerColumnType(93, "datetime year to fraction(5)");
      this.registerColumnType(92, "datetime hour to second");
      this.registerColumnType(-6, "smallint");
      this.registerColumnType(-3, "byte");
      this.registerColumnType(12, "varchar($l)");
      this.registerColumnType(12, 255L, "varchar($l)");
      this.registerColumnType(12, 32739L, "lvarchar($l)");
      this.registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "(", "||", ")"));
   }

   public String getAddColumnString() {
      return "add";
   }

   public boolean supportsIdentityColumns() {
      return true;
   }

   public String getIdentitySelectString(String table, String column, int type) throws MappingException {
      return type == -5 ? "select dbinfo('serial8') from informix.systables where tabid=1" : "select dbinfo('sqlca.sqlerrd1') from informix.systables where tabid=1";
   }

   public String getIdentityColumnString(int type) throws MappingException {
      return type == -5 ? "serial8 not null" : "serial not null";
   }

   public boolean hasDataTypeInIdentityColumn() {
      return false;
   }

   public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable, String[] primaryKey, boolean referencesPrimaryKey) {
      StringBuilder result = (new StringBuilder(30)).append(" add constraint ").append(" foreign key (").append(StringHelper.join(", ", foreignKey)).append(") references ").append(referencedTable);
      if (!referencesPrimaryKey) {
         result.append(" (").append(StringHelper.join(", ", primaryKey)).append(')');
      }

      result.append(" constraint ").append(constraintName);
      return result.toString();
   }

   public String getAddPrimaryKeyConstraintString(String constraintName) {
      return " add constraint primary key constraint " + constraintName + " ";
   }

   public String getCreateSequenceString(String sequenceName) {
      return "create sequence " + sequenceName;
   }

   public String getDropSequenceString(String sequenceName) {
      return "drop sequence " + sequenceName + " restrict";
   }

   public String getSequenceNextValString(String sequenceName) {
      return "select " + this.getSelectSequenceNextValString(sequenceName) + " from informix.systables where tabid=1";
   }

   public String getSelectSequenceNextValString(String sequenceName) {
      return sequenceName + ".nextval";
   }

   public boolean supportsSequences() {
      return true;
   }

   public boolean supportsPooledSequences() {
      return true;
   }

   public String getQuerySequencesString() {
      return "select tabname from informix.systables where tabtype='Q'";
   }

   public boolean supportsLimit() {
      return true;
   }

   public boolean useMaxForLimit() {
      return true;
   }

   public boolean supportsLimitOffset() {
      return false;
   }

   public String getLimitString(String querySelect, int offset, int limit) {
      if (offset > 0) {
         throw new UnsupportedOperationException("query result offset is not supported");
      } else {
         return (new StringBuilder(querySelect.length() + 8)).append(querySelect).insert(querySelect.toLowerCase().indexOf("select") + 6, " first " + limit).toString();
      }
   }

   public boolean supportsVariableLimit() {
      return false;
   }

   public ViolatedConstraintNameExtracter getViolatedConstraintNameExtracter() {
      return EXTRACTER;
   }

   public boolean supportsCurrentTimestampSelection() {
      return true;
   }

   public boolean isCurrentTimestampSelectStringCallable() {
      return false;
   }

   public String getCurrentTimestampSelectString() {
      return "select distinct current timestamp from informix.systables";
   }

   public boolean supportsTemporaryTables() {
      return true;
   }

   public String getCreateTemporaryTableString() {
      return "create temp table";
   }

   public String getCreateTemporaryTablePostfix() {
      return "with no log";
   }
}
