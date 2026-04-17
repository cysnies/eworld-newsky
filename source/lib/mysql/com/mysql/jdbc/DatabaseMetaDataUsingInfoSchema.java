package com.mysql.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseMetaDataUsingInfoSchema extends DatabaseMetaData {
   private boolean hasReferentialConstraintsView;

   protected DatabaseMetaDataUsingInfoSchema(ConnectionImpl connToSet, String databaseToSet) throws SQLException {
      super(connToSet, databaseToSet);
      this.hasReferentialConstraintsView = this.conn.versionMeetsMinimum(5, 1, 10);
   }

   private ResultSet executeMetadataQuery(PreparedStatement pStmt) throws SQLException {
      ResultSet rs = pStmt.executeQuery();
      ((ResultSetInternalMethods)rs).setOwningStatement((StatementImpl)null);
      return rs;
   }

   public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
      if (columnNamePattern == null) {
         if (!this.conn.getNullNamePatternMatchesAll()) {
            throw SQLError.createSQLException("Column name pattern can not be NULL or empty.", "S1009");
         }

         columnNamePattern = "%";
      }

      if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
         catalog = this.database;
      }

      String sql = "SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME,COLUMN_NAME, NULL AS GRANTOR, GRANTEE, PRIVILEGE_TYPE AS PRIVILEGE, IS_GRANTABLE FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES WHERE TABLE_SCHEMA LIKE ? AND TABLE_NAME =? AND COLUMN_NAME LIKE ? ORDER BY COLUMN_NAME, PRIVILEGE_TYPE";
      PreparedStatement pStmt = null;

      ResultSet var8;
      try {
         pStmt = this.prepareMetaDataSafeStatement(sql);
         if (catalog != null) {
            pStmt.setString(1, catalog);
         } else {
            pStmt.setString(1, "%");
         }

         pStmt.setString(2, table);
         pStmt.setString(3, columnNamePattern);
         ResultSet rs = this.executeMetadataQuery(pStmt);
         ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(new Field[]{new Field("", "TABLE_CAT", 1, 64), new Field("", "TABLE_SCHEM", 1, 1), new Field("", "TABLE_NAME", 1, 64), new Field("", "COLUMN_NAME", 1, 64), new Field("", "GRANTOR", 1, 77), new Field("", "GRANTEE", 1, 77), new Field("", "PRIVILEGE", 1, 64), new Field("", "IS_GRANTABLE", 1, 3)});
         var8 = rs;
      } finally {
         if (pStmt != null) {
            pStmt.close();
         }

      }

      return var8;
   }

   public ResultSet getColumns(String catalog, String schemaPattern, String tableName, String columnNamePattern) throws SQLException {
      if (columnNamePattern == null) {
         if (!this.conn.getNullNamePatternMatchesAll()) {
            throw SQLError.createSQLException("Column name pattern can not be NULL or empty.", "S1009");
         }

         columnNamePattern = "%";
      }

      if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
         catalog = this.database;
      }

      StringBuffer sqlBuf = new StringBuffer("SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM,TABLE_NAME,COLUMN_NAME,");
      MysqlDefs.appendJdbcTypeMappingQuery(sqlBuf, "DATA_TYPE");
      sqlBuf.append(" AS DATA_TYPE, ");
      if (this.conn.getCapitalizeTypeNames()) {
         sqlBuf.append("UPPER(CASE WHEN LOCATE('unsigned', COLUMN_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END) AS TYPE_NAME,");
      } else {
         sqlBuf.append("CASE WHEN LOCATE('unsigned', COLUMN_TYPE) != 0 AND LOCATE('unsigned', DATA_TYPE) = 0 THEN CONCAT(DATA_TYPE, ' unsigned') ELSE DATA_TYPE END AS TYPE_NAME,");
      }

      sqlBuf.append("CASE WHEN LCASE(DATA_TYPE)='date' THEN 10 WHEN LCASE(DATA_TYPE)='time' THEN 8 WHEN LCASE(DATA_TYPE)='datetime' THEN 19 WHEN LCASE(DATA_TYPE)='timestamp' THEN 19 WHEN CHARACTER_MAXIMUM_LENGTH IS NULL THEN NUMERIC_PRECISION WHEN CHARACTER_MAXIMUM_LENGTH > 2147483647 THEN 2147483647 ELSE CHARACTER_MAXIMUM_LENGTH END AS COLUMN_SIZE, " + MysqlIO.getMaxBuf() + " AS BUFFER_LENGTH," + "NUMERIC_SCALE AS DECIMAL_DIGITS," + "10 AS NUM_PREC_RADIX," + "CASE WHEN IS_NULLABLE='NO' THEN " + 0 + " ELSE CASE WHEN IS_NULLABLE='YES' THEN " + 1 + " ELSE " + 2 + " END END AS NULLABLE," + "COLUMN_COMMENT AS REMARKS," + "COLUMN_DEFAULT AS COLUMN_DEF," + "0 AS SQL_DATA_TYPE," + "0 AS SQL_DATETIME_SUB," + "CASE WHEN CHARACTER_OCTET_LENGTH > " + Integer.MAX_VALUE + " THEN " + Integer.MAX_VALUE + " ELSE CHARACTER_OCTET_LENGTH END AS CHAR_OCTET_LENGTH," + "ORDINAL_POSITION," + "IS_NULLABLE," + "NULL AS SCOPE_CATALOG," + "NULL AS SCOPE_SCHEMA," + "NULL AS SCOPE_TABLE," + "NULL AS SOURCE_DATA_TYPE," + "IF (EXTRA LIKE '%auto_increment%','YES','NO') AS IS_AUTOINCREMENT " + "FROM INFORMATION_SCHEMA.COLUMNS WHERE " + "TABLE_SCHEMA LIKE ? AND " + "TABLE_NAME LIKE ? AND COLUMN_NAME LIKE ? " + "ORDER BY TABLE_SCHEMA, TABLE_NAME, ORDINAL_POSITION");
      PreparedStatement pStmt = null;

      ResultSet var8;
      try {
         pStmt = this.prepareMetaDataSafeStatement(sqlBuf.toString());
         if (catalog != null) {
            pStmt.setString(1, catalog);
         } else {
            pStmt.setString(1, "%");
         }

         pStmt.setString(2, tableName);
         pStmt.setString(3, columnNamePattern);
         ResultSet rs = this.executeMetadataQuery(pStmt);
         ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(new Field[]{new Field("", "TABLE_CAT", 1, 255), new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_NAME", 1, 255), new Field("", "COLUMN_NAME", 1, 32), new Field("", "DATA_TYPE", 5, 5), new Field("", "TYPE_NAME", 1, 16), new Field("", "COLUMN_SIZE", 4, Integer.toString(Integer.MAX_VALUE).length()), new Field("", "BUFFER_LENGTH", 4, 10), new Field("", "DECIMAL_DIGITS", 4, 10), new Field("", "NUM_PREC_RADIX", 4, 10), new Field("", "NULLABLE", 4, 10), new Field("", "REMARKS", 1, 0), new Field("", "COLUMN_DEF", 1, 0), new Field("", "SQL_DATA_TYPE", 4, 10), new Field("", "SQL_DATETIME_SUB", 4, 10), new Field("", "CHAR_OCTET_LENGTH", 4, Integer.toString(Integer.MAX_VALUE).length()), new Field("", "ORDINAL_POSITION", 4, 10), new Field("", "IS_NULLABLE", 1, 3), new Field("", "SCOPE_CATALOG", 1, 255), new Field("", "SCOPE_SCHEMA", 1, 255), new Field("", "SCOPE_TABLE", 1, 255), new Field("", "SOURCE_DATA_TYPE", 5, 10), new Field("", "IS_AUTOINCREMENT", 1, 3)});
         var8 = rs;
      } finally {
         if (pStmt != null) {
            pStmt.close();
         }

      }

      return var8;
   }

   public ResultSet getCrossReference(String primaryCatalog, String primarySchema, String primaryTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
      if (primaryTable == null) {
         throw SQLError.createSQLException("Table not specified.", "S1009");
      } else {
         if (primaryCatalog == null && this.conn.getNullCatalogMeansCurrent()) {
            primaryCatalog = this.database;
         }

         if (foreignCatalog == null && this.conn.getNullCatalogMeansCurrent()) {
            foreignCatalog = this.database;
         }

         Field[] fields = new Field[14];
         fields[0] = new Field("", "PKTABLE_CAT", 1, 255);
         fields[1] = new Field("", "PKTABLE_SCHEM", 1, 0);
         fields[2] = new Field("", "PKTABLE_NAME", 1, 255);
         fields[3] = new Field("", "PKCOLUMN_NAME", 1, 32);
         fields[4] = new Field("", "FKTABLE_CAT", 1, 255);
         fields[5] = new Field("", "FKTABLE_SCHEM", 1, 0);
         fields[6] = new Field("", "FKTABLE_NAME", 1, 255);
         fields[7] = new Field("", "FKCOLUMN_NAME", 1, 32);
         fields[8] = new Field("", "KEY_SEQ", 5, 2);
         fields[9] = new Field("", "UPDATE_RULE", 5, 2);
         fields[10] = new Field("", "DELETE_RULE", 5, 2);
         fields[11] = new Field("", "FK_NAME", 1, 0);
         fields[12] = new Field("", "PK_NAME", 1, 0);
         fields[13] = new Field("", "DEFERRABILITY", 4, 2);
         String sql = "SELECT A.REFERENCED_TABLE_SCHEMA AS PKTABLE_CAT,NULL AS PKTABLE_SCHEM,A.REFERENCED_TABLE_NAME AS PKTABLE_NAME,A.REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME,A.TABLE_SCHEMA AS FKTABLE_CAT,NULL AS FKTABLE_SCHEM,A.TABLE_NAME AS FKTABLE_NAME, A.COLUMN_NAME AS FKCOLUMN_NAME, A.ORDINAL_POSITION AS KEY_SEQ," + this.generateUpdateRuleClause() + " AS UPDATE_RULE," + this.generateDeleteRuleClause() + " AS DELETE_RULE," + "A.CONSTRAINT_NAME AS FK_NAME," + "(SELECT CONSTRAINT_NAME FROM" + " INFORMATION_SCHEMA.TABLE_CONSTRAINTS" + " WHERE TABLE_SCHEMA = REFERENCED_TABLE_SCHEMA AND" + " TABLE_NAME = REFERENCED_TABLE_NAME AND" + " CONSTRAINT_TYPE IN ('UNIQUE','PRIMARY KEY') LIMIT 1)" + " AS PK_NAME," + 7 + " AS DEFERRABILITY " + "FROM " + "INFORMATION_SCHEMA.KEY_COLUMN_USAGE A JOIN " + "INFORMATION_SCHEMA.TABLE_CONSTRAINTS B " + "USING (TABLE_SCHEMA, TABLE_NAME, CONSTRAINT_NAME) " + this.generateOptionalRefContraintsJoin() + "WHERE " + "B.CONSTRAINT_TYPE = 'FOREIGN KEY' " + "AND A.REFERENCED_TABLE_SCHEMA LIKE ? AND A.REFERENCED_TABLE_NAME=? " + "AND A.TABLE_SCHEMA LIKE ? AND A.TABLE_NAME=? " + "ORDER BY " + "A.TABLE_SCHEMA, A.TABLE_NAME, A.ORDINAL_POSITION";
         PreparedStatement pStmt = null;

         ResultSet var11;
         try {
            pStmt = this.prepareMetaDataSafeStatement(sql);
            if (primaryCatalog != null) {
               pStmt.setString(1, primaryCatalog);
            } else {
               pStmt.setString(1, "%");
            }

            pStmt.setString(2, primaryTable);
            if (foreignCatalog != null) {
               pStmt.setString(3, foreignCatalog);
            } else {
               pStmt.setString(3, "%");
            }

            pStmt.setString(4, foreignTable);
            ResultSet rs = this.executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(new Field[]{new Field("", "PKTABLE_CAT", 1, 255), new Field("", "PKTABLE_SCHEM", 1, 0), new Field("", "PKTABLE_NAME", 1, 255), new Field("", "PKCOLUMN_NAME", 1, 32), new Field("", "FKTABLE_CAT", 1, 255), new Field("", "FKTABLE_SCHEM", 1, 0), new Field("", "FKTABLE_NAME", 1, 255), new Field("", "FKCOLUMN_NAME", 1, 32), new Field("", "KEY_SEQ", 5, 2), new Field("", "UPDATE_RULE", 5, 2), new Field("", "DELETE_RULE", 5, 2), new Field("", "FK_NAME", 1, 0), new Field("", "PK_NAME", 1, 0), new Field("", "DEFERRABILITY", 4, 2)});
            var11 = rs;
         } finally {
            if (pStmt != null) {
               pStmt.close();
            }

         }

         return var11;
      }
   }

   public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
      if (table == null) {
         throw SQLError.createSQLException("Table not specified.", "S1009");
      } else {
         if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
            catalog = this.database;
         }

         String sql = "SELECT A.REFERENCED_TABLE_SCHEMA AS PKTABLE_CAT,NULL AS PKTABLE_SCHEM,A.REFERENCED_TABLE_NAME AS PKTABLE_NAME, A.REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME, A.TABLE_SCHEMA AS FKTABLE_CAT,NULL AS FKTABLE_SCHEM,A.TABLE_NAME AS FKTABLE_NAME,A.COLUMN_NAME AS FKCOLUMN_NAME, A.ORDINAL_POSITION AS KEY_SEQ," + this.generateUpdateRuleClause() + " AS UPDATE_RULE," + this.generateDeleteRuleClause() + " AS DELETE_RULE," + "A.CONSTRAINT_NAME AS FK_NAME," + "(SELECT CONSTRAINT_NAME FROM" + " INFORMATION_SCHEMA.TABLE_CONSTRAINTS" + " WHERE TABLE_SCHEMA = REFERENCED_TABLE_SCHEMA AND" + " TABLE_NAME = REFERENCED_TABLE_NAME AND" + " CONSTRAINT_TYPE IN ('UNIQUE','PRIMARY KEY') LIMIT 1)" + " AS PK_NAME," + 7 + " AS DEFERRABILITY " + "FROM " + "INFORMATION_SCHEMA.KEY_COLUMN_USAGE A JOIN " + "INFORMATION_SCHEMA.TABLE_CONSTRAINTS B " + "USING (TABLE_SCHEMA, TABLE_NAME, CONSTRAINT_NAME) " + this.generateOptionalRefContraintsJoin() + "WHERE " + "B.CONSTRAINT_TYPE = 'FOREIGN KEY' " + "AND A.REFERENCED_TABLE_SCHEMA LIKE ? AND A.REFERENCED_TABLE_NAME=? " + "ORDER BY A.TABLE_SCHEMA, A.TABLE_NAME, A.ORDINAL_POSITION";
         PreparedStatement pStmt = null;

         ResultSet var7;
         try {
            pStmt = this.prepareMetaDataSafeStatement(sql);
            if (catalog != null) {
               pStmt.setString(1, catalog);
            } else {
               pStmt.setString(1, "%");
            }

            pStmt.setString(2, table);
            ResultSet rs = this.executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(new Field[]{new Field("", "PKTABLE_CAT", 1, 255), new Field("", "PKTABLE_SCHEM", 1, 0), new Field("", "PKTABLE_NAME", 1, 255), new Field("", "PKCOLUMN_NAME", 1, 32), new Field("", "FKTABLE_CAT", 1, 255), new Field("", "FKTABLE_SCHEM", 1, 0), new Field("", "FKTABLE_NAME", 1, 255), new Field("", "FKCOLUMN_NAME", 1, 32), new Field("", "KEY_SEQ", 5, 2), new Field("", "UPDATE_RULE", 5, 2), new Field("", "DELETE_RULE", 5, 2), new Field("", "FK_NAME", 1, 255), new Field("", "PK_NAME", 1, 0), new Field("", "DEFERRABILITY", 4, 2)});
            var7 = rs;
         } finally {
            if (pStmt != null) {
               pStmt.close();
            }

         }

         return var7;
      }
   }

   private String generateOptionalRefContraintsJoin() {
      return this.hasReferentialConstraintsView ? "JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS R ON (R.CONSTRAINT_NAME = B.CONSTRAINT_NAME AND R.TABLE_NAME = B.TABLE_NAME AND R.CONSTRAINT_SCHEMA = B.TABLE_SCHEMA) " : "";
   }

   private String generateDeleteRuleClause() {
      return this.hasReferentialConstraintsView ? "CASE WHEN R.DELETE_RULE='CASCADE' THEN " + String.valueOf(0) + " WHEN R.DELETE_RULE='SET NULL' THEN " + 2 + " WHEN R.DELETE_RULE='SET DEFAULT' THEN " + 4 + " WHEN R.DELETE_RULE='RESTRICT' THEN " + 1 + " WHEN R.DELETE_RULE='NO ACTION' THEN " + 3 + " ELSE " + 3 + " END " : String.valueOf(1);
   }

   private String generateUpdateRuleClause() {
      return this.hasReferentialConstraintsView ? "CASE WHEN R.UPDATE_RULE='CASCADE' THEN " + String.valueOf(0) + " WHEN R.UPDATE_RULE='SET NULL' THEN " + 2 + " WHEN R.UPDATE_RULE='SET DEFAULT' THEN " + 4 + " WHEN R.UPDATE_RULE='RESTRICT' THEN " + 1 + " WHEN R.UPDATE_RULE='NO ACTION' THEN " + 3 + " ELSE " + 3 + " END " : String.valueOf(1);
   }

   public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
      if (table == null) {
         throw SQLError.createSQLException("Table not specified.", "S1009");
      } else {
         if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
            catalog = this.database;
         }

         String sql = "SELECT A.REFERENCED_TABLE_SCHEMA AS PKTABLE_CAT,NULL AS PKTABLE_SCHEM,A.REFERENCED_TABLE_NAME AS PKTABLE_NAME,A.REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME,A.TABLE_SCHEMA AS FKTABLE_CAT,NULL AS FKTABLE_SCHEM,A.TABLE_NAME AS FKTABLE_NAME, A.COLUMN_NAME AS FKCOLUMN_NAME, A.ORDINAL_POSITION AS KEY_SEQ," + this.generateUpdateRuleClause() + " AS UPDATE_RULE," + this.generateDeleteRuleClause() + " AS DELETE_RULE," + "A.CONSTRAINT_NAME AS FK_NAME," + "(SELECT CONSTRAINT_NAME FROM" + " INFORMATION_SCHEMA.TABLE_CONSTRAINTS" + " WHERE TABLE_SCHEMA = REFERENCED_TABLE_SCHEMA AND" + " TABLE_NAME = REFERENCED_TABLE_NAME AND" + " CONSTRAINT_TYPE IN ('UNIQUE','PRIMARY KEY') LIMIT 1)" + " AS PK_NAME," + 7 + " AS DEFERRABILITY " + "FROM " + "INFORMATION_SCHEMA.KEY_COLUMN_USAGE A " + "JOIN INFORMATION_SCHEMA.TABLE_CONSTRAINTS B USING " + "(CONSTRAINT_NAME, TABLE_NAME) " + this.generateOptionalRefContraintsJoin() + "WHERE " + "B.CONSTRAINT_TYPE = 'FOREIGN KEY' " + "AND A.TABLE_SCHEMA LIKE ? " + "AND A.TABLE_NAME=? " + "AND A.REFERENCED_TABLE_SCHEMA IS NOT NULL " + "ORDER BY " + "A.REFERENCED_TABLE_SCHEMA, A.REFERENCED_TABLE_NAME, " + "A.ORDINAL_POSITION";
         PreparedStatement pStmt = null;

         ResultSet var7;
         try {
            pStmt = this.prepareMetaDataSafeStatement(sql);
            if (catalog != null) {
               pStmt.setString(1, catalog);
            } else {
               pStmt.setString(1, "%");
            }

            pStmt.setString(2, table);
            ResultSet rs = this.executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(new Field[]{new Field("", "PKTABLE_CAT", 1, 255), new Field("", "PKTABLE_SCHEM", 1, 0), new Field("", "PKTABLE_NAME", 1, 255), new Field("", "PKCOLUMN_NAME", 1, 32), new Field("", "FKTABLE_CAT", 1, 255), new Field("", "FKTABLE_SCHEM", 1, 0), new Field("", "FKTABLE_NAME", 1, 255), new Field("", "FKCOLUMN_NAME", 1, 32), new Field("", "KEY_SEQ", 5, 2), new Field("", "UPDATE_RULE", 5, 2), new Field("", "DELETE_RULE", 5, 2), new Field("", "FK_NAME", 1, 255), new Field("", "PK_NAME", 1, 0), new Field("", "DEFERRABILITY", 4, 2)});
            var7 = rs;
         } finally {
            if (pStmt != null) {
               pStmt.close();
            }

         }

         return var7;
      }
   }

   public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
      StringBuffer sqlBuf = new StringBuffer("SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM,TABLE_NAME,NON_UNIQUE,TABLE_SCHEMA AS INDEX_QUALIFIER,INDEX_NAME,3 AS TYPE,SEQ_IN_INDEX AS ORDINAL_POSITION,COLUMN_NAME,COLLATION AS ASC_OR_DESC,CARDINALITY,NULL AS PAGES,NULL AS FILTER_CONDITION FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA LIKE ? AND TABLE_NAME LIKE ?");
      if (unique) {
         sqlBuf.append(" AND NON_UNIQUE=0 ");
      }

      sqlBuf.append("ORDER BY NON_UNIQUE, INDEX_NAME, SEQ_IN_INDEX");
      PreparedStatement pStmt = null;

      ResultSet var9;
      try {
         if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
            catalog = this.database;
         }

         pStmt = this.prepareMetaDataSafeStatement(sqlBuf.toString());
         if (catalog != null) {
            pStmt.setString(1, catalog);
         } else {
            pStmt.setString(1, "%");
         }

         pStmt.setString(2, table);
         ResultSet rs = this.executeMetadataQuery(pStmt);
         ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(new Field[]{new Field("", "TABLE_CAT", 1, 255), new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_NAME", 1, 255), new Field("", "NON_UNIQUE", 1, 4), new Field("", "INDEX_QUALIFIER", 1, 1), new Field("", "INDEX_NAME", 1, 32), new Field("", "TYPE", 1, 32), new Field("", "ORDINAL_POSITION", 5, 5), new Field("", "COLUMN_NAME", 1, 32), new Field("", "ASC_OR_DESC", 1, 1), new Field("", "CARDINALITY", 4, 10), new Field("", "PAGES", 4, 10), new Field("", "FILTER_CONDITION", 1, 32)});
         var9 = rs;
      } finally {
         if (pStmt != null) {
            pStmt.close();
         }

      }

      return var9;
   }

   public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
      if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
         catalog = this.database;
      }

      if (table == null) {
         throw SQLError.createSQLException("Table not specified.", "S1009");
      } else {
         String sql = "SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME, COLUMN_NAME, SEQ_IN_INDEX AS KEY_SEQ, 'PRIMARY' AS PK_NAME FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA LIKE ? AND TABLE_NAME LIKE ? AND INDEX_NAME='PRIMARY' ORDER BY TABLE_SCHEMA, TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX";
         PreparedStatement pStmt = null;

         ResultSet var7;
         try {
            pStmt = this.prepareMetaDataSafeStatement(sql);
            if (catalog != null) {
               pStmt.setString(1, catalog);
            } else {
               pStmt.setString(1, "%");
            }

            pStmt.setString(2, table);
            ResultSet rs = this.executeMetadataQuery(pStmt);
            ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(new Field[]{new Field("", "TABLE_CAT", 1, 255), new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_NAME", 1, 255), new Field("", "COLUMN_NAME", 1, 32), new Field("", "KEY_SEQ", 5, 5), new Field("", "PK_NAME", 1, 32)});
            var7 = rs;
         } finally {
            if (pStmt != null) {
               pStmt.close();
            }

         }

         return var7;
      }
   }

   public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
      if (procedureNamePattern == null || procedureNamePattern.length() == 0) {
         if (!this.conn.getNullNamePatternMatchesAll()) {
            throw SQLError.createSQLException("Procedure name pattern can not be NULL or empty.", "S1009");
         }

         procedureNamePattern = "%";
      }

      String db = null;
      if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
         db = this.database;
      }

      String sql = "SELECT ROUTINE_SCHEMA AS PROCEDURE_CAT, NULL AS PROCEDURE_SCHEM, ROUTINE_NAME AS PROCEDURE_NAME, NULL AS RESERVED_1, NULL AS RESERVED_2, NULL AS RESERVED_3, ROUTINE_COMMENT AS REMARKS, CASE WHEN ROUTINE_TYPE = 'PROCEDURE' THEN 1 WHEN ROUTINE_TYPE='FUNCTION' THEN 2 ELSE 0 END AS PROCEDURE_TYPE FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_SCHEMA LIKE ? AND ROUTINE_NAME LIKE ? ORDER BY ROUTINE_SCHEMA, ROUTINE_NAME";
      PreparedStatement pStmt = null;

      ResultSet var8;
      try {
         pStmt = this.prepareMetaDataSafeStatement(sql);
         if (db != null) {
            pStmt.setString(1, db);
         } else {
            pStmt.setString(1, "%");
         }

         pStmt.setString(2, procedureNamePattern);
         ResultSet rs = this.executeMetadataQuery(pStmt);
         ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(new Field[]{new Field("", "PROCEDURE_CAT", 1, 0), new Field("", "PROCEDURE_SCHEM", 1, 0), new Field("", "PROCEDURE_NAME", 1, 0), new Field("", "reserved1", 1, 0), new Field("", "reserved2", 1, 0), new Field("", "reserved3", 1, 0), new Field("", "REMARKS", 1, 0), new Field("", "PROCEDURE_TYPE", 5, 0)});
         var8 = rs;
      } finally {
         if (pStmt != null) {
            pStmt.close();
         }

      }

      return var8;
   }

   public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
      if (catalog == null && this.conn.getNullCatalogMeansCurrent()) {
         catalog = this.database;
      }

      if (tableNamePattern == null) {
         if (!this.conn.getNullNamePatternMatchesAll()) {
            throw SQLError.createSQLException("Table name pattern can not be NULL or empty.", "S1009");
         }

         tableNamePattern = "%";
      }

      PreparedStatement pStmt = null;
      String sql = "SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME, CASE WHEN TABLE_TYPE='BASE TABLE' THEN 'TABLE' WHEN TABLE_TYPE='TEMPORARY' THEN 'LOCAL_TEMPORARY' ELSE TABLE_TYPE END AS TABLE_TYPE, TABLE_COMMENT AS REMARKS FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA LIKE ? AND TABLE_NAME LIKE ? AND TABLE_TYPE IN (?,?,?) ORDER BY TABLE_TYPE, TABLE_SCHEMA, TABLE_NAME";

      ResultSet var8;
      try {
         pStmt = this.prepareMetaDataSafeStatement(sql);
         if (catalog != null) {
            pStmt.setString(1, catalog);
         } else {
            pStmt.setString(1, "%");
         }

         pStmt.setString(2, tableNamePattern);
         if (types != null && types.length != 0) {
            pStmt.setNull(3, 12);
            pStmt.setNull(4, 12);
            pStmt.setNull(5, 12);

            for(int i = 0; i < types.length; ++i) {
               if ("TABLE".equalsIgnoreCase(types[i])) {
                  pStmt.setString(3, "BASE TABLE");
               }

               if ("VIEW".equalsIgnoreCase(types[i])) {
                  pStmt.setString(4, "VIEW");
               }

               if ("LOCAL TEMPORARY".equalsIgnoreCase(types[i])) {
                  pStmt.setString(5, "TEMPORARY");
               }
            }
         } else {
            pStmt.setString(3, "BASE TABLE");
            pStmt.setString(4, "VIEW");
            pStmt.setString(5, "TEMPORARY");
         }

         ResultSet rs = this.executeMetadataQuery(pStmt);
         ((ResultSetInternalMethods)rs).redefineFieldsForDBMD(new Field[]{new Field("", "TABLE_CAT", 12, catalog == null ? 0 : catalog.length()), new Field("", "TABLE_SCHEM", 12, 0), new Field("", "TABLE_NAME", 12, 255), new Field("", "TABLE_TYPE", 12, 5), new Field("", "REMARKS", 12, 0)});
         var8 = rs;
      } finally {
         if (pStmt != null) {
            pStmt.close();
         }

      }

      return var8;
   }

   private PreparedStatement prepareMetaDataSafeStatement(String sql) throws SQLException {
      PreparedStatement pStmt = (PreparedStatement)this.conn.clientPrepareStatement(sql);
      if (pStmt.getMaxRows() != 0) {
         pStmt.setMaxRows(0);
      }

      pStmt.setHoldResultsOpenOverClose(true);
      return pStmt;
   }
}
