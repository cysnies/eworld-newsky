package com.mysql.jdbc;

import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandlerFactory;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class UpdatableResultSet extends ResultSetImpl {
   protected static final byte[] STREAM_DATA_MARKER = "** STREAM DATA **".getBytes();
   protected SingleByteCharsetConverter charConverter;
   private String charEncoding;
   private byte[][] defaultColumnValue;
   private PreparedStatement deleter = null;
   private String deleteSQL = null;
   private boolean initializedCharConverter = false;
   protected PreparedStatement inserter = null;
   private String insertSQL = null;
   private boolean isUpdatable = false;
   private String notUpdatableReason = null;
   private List primaryKeyIndicies = null;
   private String qualifiedAndQuotedTableName;
   private String quotedIdChar = null;
   private PreparedStatement refresher;
   private String refreshSQL = null;
   private ResultSetRow savedCurrentRow;
   protected PreparedStatement updater = null;
   private String updateSQL = null;
   private boolean populateInserterWithDefaultValues = false;
   private Map databasesUsedToTablesUsed = null;

   protected UpdatableResultSet(String catalog, Field[] fields, RowData tuples, ConnectionImpl conn, StatementImpl creatorStmt) throws SQLException {
      super(catalog, fields, tuples, conn, creatorStmt);
      this.checkUpdatability();
      this.populateInserterWithDefaultValues = this.connection.getPopulateInsertRowWithDefaultValues();
   }

   public synchronized boolean absolute(int row) throws SQLException {
      return super.absolute(row);
   }

   public synchronized void afterLast() throws SQLException {
      super.afterLast();
   }

   public synchronized void beforeFirst() throws SQLException {
      super.beforeFirst();
   }

   public synchronized void cancelRowUpdates() throws SQLException {
      this.checkClosed();
      if (this.doingUpdates) {
         this.doingUpdates = false;
         this.updater.clearParameters();
      }

   }

   protected void checkRowPos() throws SQLException {
      this.checkClosed();
      if (!this.onInsertRow) {
         super.checkRowPos();
      }

   }

   protected void checkUpdatability() throws SQLException {
      if (this.fields != null) {
         String singleTableName = null;
         String catalogName = null;
         int primaryKeyCount = 0;
         if (this.catalog == null || this.catalog.length() == 0) {
            this.catalog = this.fields[0].getDatabaseName();
            if (this.catalog == null || this.catalog.length() == 0) {
               throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.43"), "S1009");
            }
         }

         if (this.fields.length <= 0) {
            this.isUpdatable = false;
            this.notUpdatableReason = Messages.getString("NotUpdatableReason.3");
         } else {
            singleTableName = this.fields[0].getOriginalTableName();
            catalogName = this.fields[0].getDatabaseName();
            if (singleTableName == null) {
               singleTableName = this.fields[0].getTableName();
               catalogName = this.catalog;
            }

            if (singleTableName != null && singleTableName.length() == 0) {
               this.isUpdatable = false;
               this.notUpdatableReason = Messages.getString("NotUpdatableReason.3");
            } else {
               if (this.fields[0].isPrimaryKey()) {
                  ++primaryKeyCount;
               }

               int i = 1;

               while(i < this.fields.length) {
                  String otherTableName = this.fields[i].getOriginalTableName();
                  String otherCatalogName = this.fields[i].getDatabaseName();
                  if (otherTableName == null) {
                     otherTableName = this.fields[i].getTableName();
                     otherCatalogName = this.catalog;
                  }

                  if (otherTableName != null && otherTableName.length() == 0) {
                     this.isUpdatable = false;
                     this.notUpdatableReason = Messages.getString("NotUpdatableReason.3");
                     return;
                  }

                  if (singleTableName != null && otherTableName.equals(singleTableName)) {
                     if (catalogName != null && otherCatalogName.equals(catalogName)) {
                        if (this.fields[i].isPrimaryKey()) {
                           ++primaryKeyCount;
                        }

                        ++i;
                        continue;
                     }

                     this.isUpdatable = false;
                     this.notUpdatableReason = Messages.getString("NotUpdatableReason.1");
                     return;
                  }

                  this.isUpdatable = false;
                  this.notUpdatableReason = Messages.getString("NotUpdatableReason.0");
                  return;
               }

               if (singleTableName != null && singleTableName.length() != 0) {
                  if (this.connection.getStrictUpdates()) {
                     java.sql.DatabaseMetaData dbmd = this.connection.getMetaData();
                     ResultSet rs = null;
                     HashMap primaryKeyNames = new HashMap();

                     try {
                        rs = dbmd.getPrimaryKeys(catalogName, (String)null, singleTableName);

                        while(rs.next()) {
                           String keyName = rs.getString(4);
                           keyName = keyName.toUpperCase();
                           primaryKeyNames.put(keyName, keyName);
                        }
                     } finally {
                        if (rs != null) {
                           try {
                              rs.close();
                           } catch (Exception ex) {
                              AssertionFailedException.shouldNotHappen(ex);
                           }

                           ResultSet var20 = null;
                        }

                     }

                     int existingPrimaryKeysCount = primaryKeyNames.size();
                     if (existingPrimaryKeysCount == 0) {
                        this.isUpdatable = false;
                        this.notUpdatableReason = Messages.getString("NotUpdatableReason.5");
                        return;
                     }

                     for(int i = 0; i < this.fields.length; ++i) {
                        if (this.fields[i].isPrimaryKey()) {
                           String columnNameUC = this.fields[i].getName().toUpperCase();
                           if (primaryKeyNames.remove(columnNameUC) == null) {
                              String originalName = this.fields[i].getOriginalName();
                              if (originalName != null && primaryKeyNames.remove(originalName.toUpperCase()) == null) {
                                 this.isUpdatable = false;
                                 this.notUpdatableReason = Messages.getString("NotUpdatableReason.6", new Object[]{originalName});
                                 return;
                              }
                           }
                        }
                     }

                     this.isUpdatable = primaryKeyNames.isEmpty();
                     if (!this.isUpdatable) {
                        if (existingPrimaryKeysCount > 1) {
                           this.notUpdatableReason = Messages.getString("NotUpdatableReason.7");
                        } else {
                           this.notUpdatableReason = Messages.getString("NotUpdatableReason.4");
                        }

                        return;
                     }
                  }

                  if (primaryKeyCount == 0) {
                     this.isUpdatable = false;
                     this.notUpdatableReason = Messages.getString("NotUpdatableReason.4");
                  } else {
                     this.isUpdatable = true;
                     this.notUpdatableReason = null;
                  }
               } else {
                  this.isUpdatable = false;
                  this.notUpdatableReason = Messages.getString("NotUpdatableReason.2");
               }
            }
         }
      }
   }

   public synchronized void deleteRow() throws SQLException {
      this.checkClosed();
      if (!this.isUpdatable) {
         throw new NotUpdatable(this.notUpdatableReason);
      } else if (this.onInsertRow) {
         throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.1"));
      } else if (this.rowData.size() == 0) {
         throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.2"));
      } else if (this.isBeforeFirst()) {
         throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.3"));
      } else if (this.isAfterLast()) {
         throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.4"));
      } else {
         if (this.deleter == null) {
            if (this.deleteSQL == null) {
               this.generateStatements();
            }

            this.deleter = (PreparedStatement)this.connection.clientPrepareStatement(this.deleteSQL);
         }

         this.deleter.clearParameters();
         String characterEncoding = null;
         if (this.connection.getUseUnicode()) {
            characterEncoding = this.connection.getEncoding();
         }

         try {
            int numKeys = this.primaryKeyIndicies.size();
            if (numKeys == 1) {
               int index = (Integer)this.primaryKeyIndicies.get(0);
               String currentVal = characterEncoding == null ? new String(this.thisRow.getColumnValue(index)) : new String(this.thisRow.getColumnValue(index), characterEncoding);
               this.deleter.setString(1, currentVal);
            } else {
               for(int i = 0; i < numKeys; ++i) {
                  int index = (Integer)this.primaryKeyIndicies.get(i);
                  String currentVal = characterEncoding == null ? new String(this.thisRow.getColumnValue(index)) : new String(this.thisRow.getColumnValue(index), characterEncoding);
                  this.deleter.setString(i + 1, currentVal);
               }
            }

            this.deleter.executeUpdate();
            this.rowData.removeRow(this.rowData.getCurrentRowNumber());
         } catch (UnsupportedEncodingException var6) {
            throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.39", new Object[]{this.charEncoding}), "S1009");
         }
      }
   }

   private synchronized void extractDefaultValues() throws SQLException {
      java.sql.DatabaseMetaData dbmd = this.connection.getMetaData();
      this.defaultColumnValue = new byte[this.fields.length][];
      ResultSet columnsResultSet = null;

      for(Map.Entry dbEntry : this.databasesUsedToTablesUsed.entrySet()) {
         String databaseName = dbEntry.getKey().toString();

         for(Map.Entry tableEntry : ((Map)dbEntry.getValue()).entrySet()) {
            String tableName = tableEntry.getKey().toString();
            Map columnNamesToIndices = (Map)tableEntry.getValue();

            try {
               columnsResultSet = dbmd.getColumns(this.catalog, (String)null, tableName, "%");

               while(columnsResultSet.next()) {
                  String columnName = columnsResultSet.getString("COLUMN_NAME");
                  byte[] defaultValue = columnsResultSet.getBytes("COLUMN_DEF");
                  if (columnNamesToIndices.containsKey(columnName)) {
                     int localColumnIndex = (Integer)columnNamesToIndices.get(columnName);
                     this.defaultColumnValue[localColumnIndex] = defaultValue;
                  }
               }
            } finally {
               if (columnsResultSet != null) {
                  columnsResultSet.close();
                  columnsResultSet = null;
               }

            }
         }
      }

   }

   public synchronized boolean first() throws SQLException {
      return super.first();
   }

   protected synchronized void generateStatements() throws SQLException {
      if (!this.isUpdatable) {
         this.doingUpdates = false;
         this.onInsertRow = false;
         throw new NotUpdatable(this.notUpdatableReason);
      } else {
         String quotedId = this.getQuotedIdChar();
         Map tableNamesSoFar = null;
         if (this.connection.lowerCaseTableNames()) {
            tableNamesSoFar = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            this.databasesUsedToTablesUsed = new TreeMap(String.CASE_INSENSITIVE_ORDER);
         } else {
            tableNamesSoFar = new TreeMap();
            this.databasesUsedToTablesUsed = new TreeMap();
         }

         this.primaryKeyIndicies = new ArrayList();
         StringBuffer fieldValues = new StringBuffer();
         StringBuffer keyValues = new StringBuffer();
         StringBuffer columnNames = new StringBuffer();
         StringBuffer insertPlaceHolders = new StringBuffer();
         StringBuffer allTablesBuf = new StringBuffer();
         Map columnIndicesToTable = new HashMap();
         boolean firstTime = true;
         boolean keysFirstTime = true;
         String equalsStr = this.connection.versionMeetsMinimum(3, 23, 0) ? "<=>" : "=";

         for(int i = 0; i < this.fields.length; ++i) {
            StringBuffer tableNameBuffer = new StringBuffer();
            Map updColumnNameToIndex = null;
            if (this.fields[i].getOriginalTableName() != null) {
               String databaseName = this.fields[i].getDatabaseName();
               if (databaseName != null && databaseName.length() > 0) {
                  tableNameBuffer.append(quotedId);
                  tableNameBuffer.append(databaseName);
                  tableNameBuffer.append(quotedId);
                  tableNameBuffer.append('.');
               }

               String tableOnlyName = this.fields[i].getOriginalTableName();
               tableNameBuffer.append(quotedId);
               tableNameBuffer.append(tableOnlyName);
               tableNameBuffer.append(quotedId);
               String fqTableName = tableNameBuffer.toString();
               if (!tableNamesSoFar.containsKey(fqTableName)) {
                  if (!tableNamesSoFar.isEmpty()) {
                     allTablesBuf.append(',');
                  }

                  allTablesBuf.append(fqTableName);
                  tableNamesSoFar.put(fqTableName, fqTableName);
               }

               columnIndicesToTable.put(new Integer(i), fqTableName);
               updColumnNameToIndex = this.getColumnsToIndexMapForTableAndDB(databaseName, tableOnlyName);
            } else {
               String tableOnlyName = this.fields[i].getTableName();
               if (tableOnlyName != null) {
                  tableNameBuffer.append(quotedId);
                  tableNameBuffer.append(tableOnlyName);
                  tableNameBuffer.append(quotedId);
                  String fqTableName = tableNameBuffer.toString();
                  if (!tableNamesSoFar.containsKey(fqTableName)) {
                     if (!tableNamesSoFar.isEmpty()) {
                        allTablesBuf.append(',');
                     }

                     allTablesBuf.append(fqTableName);
                     tableNamesSoFar.put(fqTableName, fqTableName);
                  }

                  columnIndicesToTable.put(new Integer(i), fqTableName);
                  updColumnNameToIndex = this.getColumnsToIndexMapForTableAndDB(this.catalog, tableOnlyName);
               }
            }

            String originalColumnName = this.fields[i].getOriginalName();
            String columnName = null;
            if (this.connection.getIO().hasLongColumnInfo() && originalColumnName != null && originalColumnName.length() > 0) {
               columnName = originalColumnName;
            } else {
               columnName = this.fields[i].getName();
            }

            if (updColumnNameToIndex != null && columnName != null) {
               updColumnNameToIndex.put(columnName, new Integer(i));
            }

            String originalTableName = this.fields[i].getOriginalTableName();
            String tableName = null;
            if (this.connection.getIO().hasLongColumnInfo() && originalTableName != null && originalTableName.length() > 0) {
               tableName = originalTableName;
            } else {
               tableName = this.fields[i].getTableName();
            }

            StringBuffer fqcnBuf = new StringBuffer();
            String databaseName = this.fields[i].getDatabaseName();
            if (databaseName != null && databaseName.length() > 0) {
               fqcnBuf.append(quotedId);
               fqcnBuf.append(databaseName);
               fqcnBuf.append(quotedId);
               fqcnBuf.append('.');
            }

            fqcnBuf.append(quotedId);
            fqcnBuf.append(tableName);
            fqcnBuf.append(quotedId);
            fqcnBuf.append('.');
            fqcnBuf.append(quotedId);
            fqcnBuf.append(columnName);
            fqcnBuf.append(quotedId);
            String qualifiedColumnName = fqcnBuf.toString();
            if (this.fields[i].isPrimaryKey()) {
               this.primaryKeyIndicies.add(Constants.integerValueOf(i));
               if (!keysFirstTime) {
                  keyValues.append(" AND ");
               } else {
                  keysFirstTime = false;
               }

               keyValues.append(qualifiedColumnName);
               keyValues.append(equalsStr);
               keyValues.append("?");
            }

            if (firstTime) {
               firstTime = false;
               fieldValues.append("SET ");
            } else {
               fieldValues.append(",");
               columnNames.append(",");
               insertPlaceHolders.append(",");
            }

            insertPlaceHolders.append("?");
            columnNames.append(qualifiedColumnName);
            fieldValues.append(qualifiedColumnName);
            fieldValues.append("=?");
         }

         this.qualifiedAndQuotedTableName = allTablesBuf.toString();
         this.updateSQL = "UPDATE " + this.qualifiedAndQuotedTableName + " " + fieldValues.toString() + " WHERE " + keyValues.toString();
         this.insertSQL = "INSERT INTO " + this.qualifiedAndQuotedTableName + " (" + columnNames.toString() + ") VALUES (" + insertPlaceHolders.toString() + ")";
         this.refreshSQL = "SELECT " + columnNames.toString() + " FROM " + this.qualifiedAndQuotedTableName + " WHERE " + keyValues.toString();
         this.deleteSQL = "DELETE FROM " + this.qualifiedAndQuotedTableName + " WHERE " + keyValues.toString();
      }
   }

   private Map getColumnsToIndexMapForTableAndDB(String databaseName, String tableName) {
      Map tablesUsedToColumnsMap = (Map)this.databasesUsedToTablesUsed.get(databaseName);
      if (tablesUsedToColumnsMap == null) {
         if (this.connection.lowerCaseTableNames()) {
            tablesUsedToColumnsMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
         } else {
            tablesUsedToColumnsMap = new TreeMap();
         }

         this.databasesUsedToTablesUsed.put(databaseName, tablesUsedToColumnsMap);
      }

      Map nameToIndex = (Map)tablesUsedToColumnsMap.get(tableName);
      if (nameToIndex == null) {
         nameToIndex = new HashMap();
         tablesUsedToColumnsMap.put(tableName, nameToIndex);
      }

      return nameToIndex;
   }

   private synchronized SingleByteCharsetConverter getCharConverter() throws SQLException {
      if (!this.initializedCharConverter) {
         this.initializedCharConverter = true;
         if (this.connection.getUseUnicode()) {
            this.charEncoding = this.connection.getEncoding();
            this.charConverter = this.connection.getCharsetConverter(this.charEncoding);
         }
      }

      return this.charConverter;
   }

   public int getConcurrency() throws SQLException {
      return this.isUpdatable ? 1008 : 1007;
   }

   private synchronized String getQuotedIdChar() throws SQLException {
      if (this.quotedIdChar == null) {
         boolean useQuotedIdentifiers = this.connection.supportsQuotedIdentifiers();
         if (useQuotedIdentifiers) {
            java.sql.DatabaseMetaData dbmd = this.connection.getMetaData();
            this.quotedIdChar = dbmd.getIdentifierQuoteString();
         } else {
            this.quotedIdChar = "";
         }
      }

      return this.quotedIdChar;
   }

   public synchronized void insertRow() throws SQLException {
      this.checkClosed();
      if (!this.onInsertRow) {
         throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.7"));
      } else {
         this.inserter.executeUpdate();
         long autoIncrementId = this.inserter.getLastInsertID();
         int numFields = this.fields.length;
         byte[][] newRow = new byte[numFields][];

         for(int i = 0; i < numFields; ++i) {
            if (this.inserter.isNull(i)) {
               newRow[i] = null;
            } else {
               newRow[i] = this.inserter.getBytesRepresentation(i);
            }

            if (this.fields[i].isAutoIncrement() && autoIncrementId > 0L) {
               newRow[i] = String.valueOf(autoIncrementId).getBytes();
               this.inserter.setBytesNoEscapeNoQuotes(i + 1, newRow[i]);
            }
         }

         ResultSetRow resultSetRow = new ByteArrayRow(newRow);
         this.refreshRow(this.inserter, resultSetRow);
         this.rowData.addRow(resultSetRow);
         this.resetInserter();
      }
   }

   public synchronized boolean isAfterLast() throws SQLException {
      return super.isAfterLast();
   }

   public synchronized boolean isBeforeFirst() throws SQLException {
      return super.isBeforeFirst();
   }

   public synchronized boolean isFirst() throws SQLException {
      return super.isFirst();
   }

   public synchronized boolean isLast() throws SQLException {
      return super.isLast();
   }

   boolean isUpdatable() {
      return this.isUpdatable;
   }

   public synchronized boolean last() throws SQLException {
      return super.last();
   }

   public synchronized void moveToCurrentRow() throws SQLException {
      this.checkClosed();
      if (!this.isUpdatable) {
         throw new NotUpdatable(this.notUpdatableReason);
      } else {
         if (this.onInsertRow) {
            this.onInsertRow = false;
            this.thisRow = this.savedCurrentRow;
         }

      }
   }

   public synchronized void moveToInsertRow() throws SQLException {
      this.checkClosed();
      if (!this.isUpdatable) {
         throw new NotUpdatable(this.notUpdatableReason);
      } else {
         if (this.inserter == null) {
            if (this.insertSQL == null) {
               this.generateStatements();
            }

            this.inserter = (PreparedStatement)this.connection.clientPrepareStatement(this.insertSQL);
            if (this.populateInserterWithDefaultValues) {
               this.extractDefaultValues();
            }

            this.resetInserter();
         } else {
            this.resetInserter();
         }

         int numFields = this.fields.length;
         this.onInsertRow = true;
         this.doingUpdates = false;
         this.savedCurrentRow = this.thisRow;
         byte[][] newRowData = new byte[numFields][];
         this.thisRow = new ByteArrayRow(newRowData);

         for(int i = 0; i < numFields; ++i) {
            if (!this.populateInserterWithDefaultValues) {
               this.inserter.setBytesNoEscapeNoQuotes(i + 1, "DEFAULT".getBytes());
               newRowData = (byte[][])null;
            } else if (this.defaultColumnValue[i] != null) {
               Field f = this.fields[i];
               switch (f.getMysqlType()) {
                  case 7:
                  case 10:
                  case 11:
                  case 12:
                  case 14:
                     if (this.defaultColumnValue[i].length > 7 && this.defaultColumnValue[i][0] == 67 && this.defaultColumnValue[i][1] == 85 && this.defaultColumnValue[i][2] == 82 && this.defaultColumnValue[i][3] == 82 && this.defaultColumnValue[i][4] == 69 && this.defaultColumnValue[i][5] == 78 && this.defaultColumnValue[i][6] == 84 && this.defaultColumnValue[i][7] == 95) {
                        this.inserter.setBytesNoEscapeNoQuotes(i + 1, this.defaultColumnValue[i]);
                        break;
                     }
                  case 8:
                  case 9:
                  case 13:
                  default:
                     this.inserter.setBytes(i + 1, this.defaultColumnValue[i], false, false);
               }

               byte[] defaultValueCopy = new byte[this.defaultColumnValue[i].length];
               System.arraycopy(this.defaultColumnValue[i], 0, defaultValueCopy, 0, defaultValueCopy.length);
               newRowData[i] = defaultValueCopy;
            } else {
               this.inserter.setNull(i + 1, 0);
               newRowData[i] = null;
            }
         }

      }
   }

   public synchronized boolean next() throws SQLException {
      return super.next();
   }

   public synchronized boolean prev() throws SQLException {
      return super.prev();
   }

   public synchronized boolean previous() throws SQLException {
      return super.previous();
   }

   public void realClose(boolean calledExplicitly) throws SQLException {
      if (!this.isClosed) {
         SQLException sqlEx = null;
         if (this.useUsageAdvisor && this.deleter == null && this.inserter == null && this.refresher == null && this.updater == null) {
            this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
            String message = Messages.getString("UpdatableResultSet.34");
            this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.owningStatement == null ? "N/A" : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, (String)null, this.pointOfOrigin, message));
         }

         try {
            if (this.deleter != null) {
               this.deleter.close();
            }
         } catch (SQLException ex) {
            sqlEx = ex;
         }

         try {
            if (this.inserter != null) {
               this.inserter.close();
            }
         } catch (SQLException ex) {
            sqlEx = ex;
         }

         try {
            if (this.refresher != null) {
               this.refresher.close();
            }
         } catch (SQLException ex) {
            sqlEx = ex;
         }

         try {
            if (this.updater != null) {
               this.updater.close();
            }
         } catch (SQLException ex) {
            sqlEx = ex;
         }

         super.realClose(calledExplicitly);
         if (sqlEx != null) {
            throw sqlEx;
         }
      }
   }

   public synchronized void refreshRow() throws SQLException {
      this.checkClosed();
      if (!this.isUpdatable) {
         throw new NotUpdatable();
      } else if (this.onInsertRow) {
         throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.8"));
      } else if (this.rowData.size() == 0) {
         throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.9"));
      } else if (this.isBeforeFirst()) {
         throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.10"));
      } else if (this.isAfterLast()) {
         throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.11"));
      } else {
         this.refreshRow(this.updater, this.thisRow);
      }
   }

   private synchronized void refreshRow(PreparedStatement updateInsertStmt, ResultSetRow rowToRefresh) throws SQLException {
      if (this.refresher == null) {
         if (this.refreshSQL == null) {
            this.generateStatements();
         }

         this.refresher = (PreparedStatement)this.connection.clientPrepareStatement(this.refreshSQL);
      }

      this.refresher.clearParameters();
      int numKeys = this.primaryKeyIndicies.size();
      if (numKeys == 1) {
         byte[] dataFrom = null;
         int index = (Integer)this.primaryKeyIndicies.get(0);
         if (!this.doingUpdates && !this.onInsertRow) {
            dataFrom = rowToRefresh.getColumnValue(index);
         } else {
            dataFrom = updateInsertStmt.getBytesRepresentation(index);
            if (!updateInsertStmt.isNull(index) && dataFrom.length != 0) {
               dataFrom = this.stripBinaryPrefix(dataFrom);
            } else {
               dataFrom = rowToRefresh.getColumnValue(index);
            }
         }

         this.refresher.setBytesNoEscape(1, dataFrom);
      } else {
         for(int i = 0; i < numKeys; ++i) {
            byte[] dataFrom = null;
            int index = (Integer)this.primaryKeyIndicies.get(i);
            if (!this.doingUpdates && !this.onInsertRow) {
               dataFrom = rowToRefresh.getColumnValue(index);
            } else {
               dataFrom = updateInsertStmt.getBytesRepresentation(index);
               if (!updateInsertStmt.isNull(index) && dataFrom.length != 0) {
                  dataFrom = this.stripBinaryPrefix(dataFrom);
               } else {
                  dataFrom = rowToRefresh.getColumnValue(index);
               }
            }

            this.refresher.setBytesNoEscape(i + 1, dataFrom);
         }
      }

      ResultSet rs = null;

      try {
         rs = this.refresher.executeQuery();
         int numCols = rs.getMetaData().getColumnCount();
         if (!rs.next()) {
            throw SQLError.createSQLException(Messages.getString("UpdatableResultSet.12"), "S1000");
         }

         for(int i = 0; i < numCols; ++i) {
            byte[] val = rs.getBytes(i + 1);
            if (val != null && !rs.wasNull()) {
               rowToRefresh.setColumnValue(i, rs.getBytes(i + 1));
            } else {
               rowToRefresh.setColumnValue(i, (byte[])null);
            }
         }
      } finally {
         if (rs != null) {
            try {
               rs.close();
            } catch (SQLException var14) {
            }
         }

      }

   }

   public synchronized boolean relative(int rows) throws SQLException {
      return super.relative(rows);
   }

   private void resetInserter() throws SQLException {
      this.inserter.clearParameters();

      for(int i = 0; i < this.fields.length; ++i) {
         this.inserter.setNull(i + 1, 0);
      }

   }

   public synchronized boolean rowDeleted() throws SQLException {
      throw SQLError.notImplemented();
   }

   public synchronized boolean rowInserted() throws SQLException {
      throw SQLError.notImplemented();
   }

   public synchronized boolean rowUpdated() throws SQLException {
      throw SQLError.notImplemented();
   }

   protected void setResultSetConcurrency(int concurrencyFlag) {
      super.setResultSetConcurrency(concurrencyFlag);
   }

   private byte[] stripBinaryPrefix(byte[] dataFrom) {
      return StringUtils.stripEnclosure(dataFrom, "_binary'", "'");
   }

   protected synchronized void syncUpdate() throws SQLException {
      if (this.updater == null) {
         if (this.updateSQL == null) {
            this.generateStatements();
         }

         this.updater = (PreparedStatement)this.connection.clientPrepareStatement(this.updateSQL);
      }

      int numFields = this.fields.length;
      this.updater.clearParameters();

      for(int i = 0; i < numFields; ++i) {
         if (this.thisRow.getColumnValue(i) != null) {
            this.updater.setBytes(i + 1, this.thisRow.getColumnValue(i), this.fields[i].isBinary(), false);
         } else {
            this.updater.setNull(i + 1, 0);
         }
      }

      int numKeys = this.primaryKeyIndicies.size();
      if (numKeys == 1) {
         int index = (Integer)this.primaryKeyIndicies.get(0);
         byte[] keyData = this.thisRow.getColumnValue(index);
         this.updater.setBytes(numFields + 1, keyData, false, false);
      } else {
         for(int i = 0; i < numKeys; ++i) {
            byte[] currentVal = this.thisRow.getColumnValue((Integer)this.primaryKeyIndicies.get(i));
            if (currentVal != null) {
               this.updater.setBytes(numFields + i + 1, currentVal, false, false);
            } else {
               this.updater.setNull(numFields + i + 1, 0);
            }
         }
      }

   }

   public synchronized void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setAsciiStream(columnIndex, x, length);
      } else {
         this.inserter.setAsciiStream(columnIndex, x, length);
         this.thisRow.setColumnValue(columnIndex - 1, STREAM_DATA_MARKER);
      }

   }

   public synchronized void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
      this.updateAsciiStream(this.findColumn(columnName), x, length);
   }

   public synchronized void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setBigDecimal(columnIndex, x);
      } else {
         this.inserter.setBigDecimal(columnIndex, x);
         if (x == null) {
            this.thisRow.setColumnValue(columnIndex - 1, (byte[])null);
         } else {
            this.thisRow.setColumnValue(columnIndex - 1, x.toString().getBytes());
         }
      }

   }

   public synchronized void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
      this.updateBigDecimal(this.findColumn(columnName), x);
   }

   public synchronized void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setBinaryStream(columnIndex, x, length);
      } else {
         this.inserter.setBinaryStream(columnIndex, x, length);
         if (x == null) {
            this.thisRow.setColumnValue(columnIndex - 1, (byte[])null);
         } else {
            this.thisRow.setColumnValue(columnIndex - 1, STREAM_DATA_MARKER);
         }
      }

   }

   public synchronized void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
      this.updateBinaryStream(this.findColumn(columnName), x, length);
   }

   public synchronized void updateBlob(int columnIndex, java.sql.Blob blob) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setBlob(columnIndex, blob);
      } else {
         this.inserter.setBlob(columnIndex, blob);
         if (blob == null) {
            this.thisRow.setColumnValue(columnIndex - 1, (byte[])null);
         } else {
            this.thisRow.setColumnValue(columnIndex - 1, STREAM_DATA_MARKER);
         }
      }

   }

   public synchronized void updateBlob(String columnName, java.sql.Blob blob) throws SQLException {
      this.updateBlob(this.findColumn(columnName), blob);
   }

   public synchronized void updateBoolean(int columnIndex, boolean x) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setBoolean(columnIndex, x);
      } else {
         this.inserter.setBoolean(columnIndex, x);
         this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
      }

   }

   public synchronized void updateBoolean(String columnName, boolean x) throws SQLException {
      this.updateBoolean(this.findColumn(columnName), x);
   }

   public synchronized void updateByte(int columnIndex, byte x) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setByte(columnIndex, x);
      } else {
         this.inserter.setByte(columnIndex, x);
         this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
      }

   }

   public synchronized void updateByte(String columnName, byte x) throws SQLException {
      this.updateByte(this.findColumn(columnName), x);
   }

   public synchronized void updateBytes(int columnIndex, byte[] x) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setBytes(columnIndex, x);
      } else {
         this.inserter.setBytes(columnIndex, x);
         this.thisRow.setColumnValue(columnIndex - 1, x);
      }

   }

   public synchronized void updateBytes(String columnName, byte[] x) throws SQLException {
      this.updateBytes(this.findColumn(columnName), x);
   }

   public synchronized void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setCharacterStream(columnIndex, x, length);
      } else {
         this.inserter.setCharacterStream(columnIndex, x, length);
         if (x == null) {
            this.thisRow.setColumnValue(columnIndex - 1, (byte[])null);
         } else {
            this.thisRow.setColumnValue(columnIndex - 1, STREAM_DATA_MARKER);
         }
      }

   }

   public synchronized void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
      this.updateCharacterStream(this.findColumn(columnName), reader, length);
   }

   public void updateClob(int columnIndex, java.sql.Clob clob) throws SQLException {
      if (clob == null) {
         this.updateNull(columnIndex);
      } else {
         this.updateCharacterStream(columnIndex, clob.getCharacterStream(), (int)clob.length());
      }

   }

   public synchronized void updateDate(int columnIndex, Date x) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setDate(columnIndex, x);
      } else {
         this.inserter.setDate(columnIndex, x);
         this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
      }

   }

   public synchronized void updateDate(String columnName, Date x) throws SQLException {
      this.updateDate(this.findColumn(columnName), x);
   }

   public synchronized void updateDouble(int columnIndex, double x) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setDouble(columnIndex, x);
      } else {
         this.inserter.setDouble(columnIndex, x);
         this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
      }

   }

   public synchronized void updateDouble(String columnName, double x) throws SQLException {
      this.updateDouble(this.findColumn(columnName), x);
   }

   public synchronized void updateFloat(int columnIndex, float x) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setFloat(columnIndex, x);
      } else {
         this.inserter.setFloat(columnIndex, x);
         this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
      }

   }

   public synchronized void updateFloat(String columnName, float x) throws SQLException {
      this.updateFloat(this.findColumn(columnName), x);
   }

   public synchronized void updateInt(int columnIndex, int x) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setInt(columnIndex, x);
      } else {
         this.inserter.setInt(columnIndex, x);
         this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
      }

   }

   public synchronized void updateInt(String columnName, int x) throws SQLException {
      this.updateInt(this.findColumn(columnName), x);
   }

   public synchronized void updateLong(int columnIndex, long x) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setLong(columnIndex, x);
      } else {
         this.inserter.setLong(columnIndex, x);
         this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
      }

   }

   public synchronized void updateLong(String columnName, long x) throws SQLException {
      this.updateLong(this.findColumn(columnName), x);
   }

   public synchronized void updateNull(int columnIndex) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setNull(columnIndex, 0);
      } else {
         this.inserter.setNull(columnIndex, 0);
         this.thisRow.setColumnValue(columnIndex - 1, (byte[])null);
      }

   }

   public synchronized void updateNull(String columnName) throws SQLException {
      this.updateNull(this.findColumn(columnName));
   }

   public synchronized void updateObject(int columnIndex, Object x) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setObject(columnIndex, x);
      } else {
         this.inserter.setObject(columnIndex, x);
         this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
      }

   }

   public synchronized void updateObject(int columnIndex, Object x, int scale) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setObject(columnIndex, x);
      } else {
         this.inserter.setObject(columnIndex, x);
         this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
      }

   }

   public synchronized void updateObject(String columnName, Object x) throws SQLException {
      this.updateObject(this.findColumn(columnName), x);
   }

   public synchronized void updateObject(String columnName, Object x, int scale) throws SQLException {
      this.updateObject(this.findColumn(columnName), x);
   }

   public synchronized void updateRow() throws SQLException {
      if (!this.isUpdatable) {
         throw new NotUpdatable(this.notUpdatableReason);
      } else {
         if (this.doingUpdates) {
            this.updater.executeUpdate();
            this.refreshRow();
            this.doingUpdates = false;
         }

         this.syncUpdate();
      }
   }

   public synchronized void updateShort(int columnIndex, short x) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setShort(columnIndex, x);
      } else {
         this.inserter.setShort(columnIndex, x);
         this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
      }

   }

   public synchronized void updateShort(String columnName, short x) throws SQLException {
      this.updateShort(this.findColumn(columnName), x);
   }

   public synchronized void updateString(int columnIndex, String x) throws SQLException {
      this.checkClosed();
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setString(columnIndex, x);
      } else {
         this.inserter.setString(columnIndex, x);
         if (x == null) {
            this.thisRow.setColumnValue(columnIndex - 1, (byte[])null);
         } else if (this.getCharConverter() != null) {
            this.thisRow.setColumnValue(columnIndex - 1, StringUtils.getBytes(x, this.charConverter, this.charEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode()));
         } else {
            this.thisRow.setColumnValue(columnIndex - 1, x.getBytes());
         }
      }

   }

   public synchronized void updateString(String columnName, String x) throws SQLException {
      this.updateString(this.findColumn(columnName), x);
   }

   public synchronized void updateTime(int columnIndex, Time x) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setTime(columnIndex, x);
      } else {
         this.inserter.setTime(columnIndex, x);
         this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
      }

   }

   public synchronized void updateTime(String columnName, Time x) throws SQLException {
      this.updateTime(this.findColumn(columnName), x);
   }

   public synchronized void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
      if (!this.onInsertRow) {
         if (!this.doingUpdates) {
            this.doingUpdates = true;
            this.syncUpdate();
         }

         this.updater.setTimestamp(columnIndex, x);
      } else {
         this.inserter.setTimestamp(columnIndex, x);
         this.thisRow.setColumnValue(columnIndex - 1, this.inserter.getBytesRepresentation(columnIndex - 1));
      }

   }

   public synchronized void updateTimestamp(String columnName, Timestamp x) throws SQLException {
      this.updateTimestamp(this.findColumn(columnName), x);
   }
}
