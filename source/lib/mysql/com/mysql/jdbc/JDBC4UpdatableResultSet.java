package com.mysql.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

public class JDBC4UpdatableResultSet extends UpdatableResultSet {
   public JDBC4UpdatableResultSet(String catalog, Field[] fields, RowData tuples, ConnectionImpl conn, StatementImpl creatorStmt) throws SQLException {
      super(catalog, fields, tuples, conn, creatorStmt);
   }

   public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateClob(int columnIndex, Reader reader) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
      this.updateNCharacterStream(columnIndex, x, (int)length);
   }

   public void updateNClob(int columnIndex, Reader reader) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateRowId(int columnIndex, RowId x) throws SQLException {
      throw new NotUpdatable();
   }

   public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
      this.updateAsciiStream(this.findColumn(columnLabel), x);
   }

   public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
      this.updateAsciiStream(this.findColumn(columnLabel), x, length);
   }

   public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
      this.updateBinaryStream(this.findColumn(columnLabel), x);
   }

   public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
      this.updateBinaryStream(this.findColumn(columnLabel), x, length);
   }

   public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
      this.updateBlob(this.findColumn(columnLabel), inputStream);
   }

   public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
      this.updateBlob(this.findColumn(columnLabel), inputStream, length);
   }

   public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
      this.updateCharacterStream(this.findColumn(columnLabel), reader);
   }

   public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
      this.updateCharacterStream(this.findColumn(columnLabel), reader, length);
   }

   public void updateClob(String columnLabel, Reader reader) throws SQLException {
      this.updateClob(this.findColumn(columnLabel), reader);
   }

   public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
      this.updateClob(this.findColumn(columnLabel), reader, length);
   }

   public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
      this.updateNCharacterStream(this.findColumn(columnLabel), reader);
   }

   public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
      this.updateNCharacterStream(this.findColumn(columnLabel), reader, length);
   }

   public void updateNClob(String columnLabel, Reader reader) throws SQLException {
      this.updateNClob(this.findColumn(columnLabel), reader);
   }

   public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
      this.updateNClob(this.findColumn(columnLabel), reader, length);
   }

   public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
      this.updateSQLXML(this.findColumn(columnLabel), xmlObject);
   }

   public synchronized void updateNCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
      String fieldEncoding = this.fields[columnIndex - 1].getCharacterSet();
      if (fieldEncoding != null && fieldEncoding.equals("UTF-8")) {
         if (!this.onInsertRow) {
            if (!this.doingUpdates) {
               this.doingUpdates = true;
               this.syncUpdate();
            }

            ((JDBC4PreparedStatement)this.updater).setNCharacterStream(columnIndex, x, (long)length);
         } else {
            ((JDBC4PreparedStatement)this.inserter).setNCharacterStream(columnIndex, x, (long)length);
            if (x == null) {
               this.thisRow.setColumnValue(columnIndex, (byte[])null);
            } else {
               this.thisRow.setColumnValue(columnIndex, STREAM_DATA_MARKER);
            }
         }

      } else {
         throw new SQLException("Can not call updateNCharacterStream() when field's character set isn't UTF-8");
      }
   }

   public synchronized void updateNCharacterStream(String columnName, Reader reader, int length) throws SQLException {
      this.updateNCharacterStream(this.findColumn(columnName), reader, length);
   }

   public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
      String fieldEncoding = this.fields[columnIndex - 1].getCharacterSet();
      if (fieldEncoding != null && fieldEncoding.equals("UTF-8")) {
         if (nClob == null) {
            this.updateNull(columnIndex);
         } else {
            this.updateNCharacterStream(columnIndex, nClob.getCharacterStream(), (int)nClob.length());
         }

      } else {
         throw new SQLException("Can not call updateNClob() when field's character set isn't UTF-8");
      }
   }

   public void updateNClob(String columnName, NClob nClob) throws SQLException {
      this.updateNClob(this.findColumn(columnName), nClob);
   }

   public synchronized void updateNString(int columnIndex, String x) throws SQLException {
      String fieldEncoding = this.fields[columnIndex - 1].getCharacterSet();
      if (fieldEncoding != null && fieldEncoding.equals("UTF-8")) {
         if (!this.onInsertRow) {
            if (!this.doingUpdates) {
               this.doingUpdates = true;
               this.syncUpdate();
            }

            ((JDBC4PreparedStatement)this.updater).setNString(columnIndex, x);
         } else {
            ((JDBC4PreparedStatement)this.inserter).setNString(columnIndex, x);
            if (x == null) {
               this.thisRow.setColumnValue(columnIndex, (byte[])null);
            } else {
               this.thisRow.setColumnValue(columnIndex, StringUtils.getBytes(x, this.charConverter, fieldEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode()));
            }
         }

      } else {
         throw new SQLException("Can not call updateNString() when field's character set isn't UTF-8");
      }
   }

   public synchronized void updateNString(String columnName, String x) throws SQLException {
      this.updateNString(this.findColumn(columnName), x);
   }

   public int getHoldability() throws SQLException {
      throw SQLError.notImplemented();
   }

   protected NClob getNativeNClob(int columnIndex) throws SQLException {
      String stringVal = this.getStringForNClob(columnIndex);
      return stringVal == null ? null : this.getNClobFromString(stringVal, columnIndex);
   }

   public Reader getNCharacterStream(int columnIndex) throws SQLException {
      String fieldEncoding = this.fields[columnIndex - 1].getCharacterSet();
      if (fieldEncoding != null && fieldEncoding.equals("UTF-8")) {
         return this.getCharacterStream(columnIndex);
      } else {
         throw new SQLException("Can not call getNCharacterStream() when field's charset isn't UTF-8");
      }
   }

   public Reader getNCharacterStream(String columnName) throws SQLException {
      return this.getNCharacterStream(this.findColumn(columnName));
   }

   public NClob getNClob(int columnIndex) throws SQLException {
      String fieldEncoding = this.fields[columnIndex - 1].getCharacterSet();
      if (fieldEncoding != null && fieldEncoding.equals("UTF-8")) {
         if (!this.isBinaryEncoded) {
            String asString = this.getStringForNClob(columnIndex);
            return asString == null ? null : new JDBC4NClob(asString);
         } else {
            return this.getNativeNClob(columnIndex);
         }
      } else {
         throw new SQLException("Can not call getNClob() when field's charset isn't UTF-8");
      }
   }

   public NClob getNClob(String columnName) throws SQLException {
      return this.getNClob(this.findColumn(columnName));
   }

   private final NClob getNClobFromString(String stringVal, int columnIndex) throws SQLException {
      return new JDBC4NClob(stringVal);
   }

   public String getNString(int columnIndex) throws SQLException {
      String fieldEncoding = this.fields[columnIndex - 1].getCharacterSet();
      if (fieldEncoding != null && fieldEncoding.equals("UTF-8")) {
         return this.getString(columnIndex);
      } else {
         throw new SQLException("Can not call getNString() when field's charset isn't UTF-8");
      }
   }

   public String getNString(String columnName) throws SQLException {
      return this.getNString(this.findColumn(columnName));
   }

   public RowId getRowId(int columnIndex) throws SQLException {
      throw SQLError.notImplemented();
   }

   public RowId getRowId(String columnLabel) throws SQLException {
      return this.getRowId(this.findColumn(columnLabel));
   }

   public SQLXML getSQLXML(int columnIndex) throws SQLException {
      return new JDBC4MysqlSQLXML(this, columnIndex);
   }

   public SQLXML getSQLXML(String columnLabel) throws SQLException {
      return this.getSQLXML(this.findColumn(columnLabel));
   }

   private String getStringForNClob(int columnIndex) throws SQLException {
      String asString = null;
      String forcedEncoding = "UTF-8";

      try {
         byte[] asBytes = null;
         if (!this.isBinaryEncoded) {
            asBytes = this.getBytes(columnIndex);
         } else {
            asBytes = this.getNativeBytes(columnIndex, true);
         }

         if (asBytes != null) {
            asString = new String(asBytes, forcedEncoding);
         }

         return asString;
      } catch (UnsupportedEncodingException var5) {
         throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009");
      }
   }

   public synchronized boolean isClosed() throws SQLException {
      return this.isClosed;
   }

   public boolean isWrapperFor(Class iface) throws SQLException {
      this.checkClosed();
      return iface.isInstance(this);
   }

   public Object unwrap(Class iface) throws SQLException {
      try {
         return iface.cast(this);
      } catch (ClassCastException var3) {
         throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009");
      }
   }
}
