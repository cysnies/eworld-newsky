package org.hibernate.engine.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.SQLException;
import org.hibernate.JDBCException;

public class ContextualLobCreator extends AbstractLobCreator implements LobCreator {
   private LobCreationContext lobCreationContext;
   public static final LobCreationContext.Callback CREATE_BLOB_CALLBACK = new LobCreationContext.Callback() {
      public Blob executeOnConnection(Connection connection) throws SQLException {
         return connection.createBlob();
      }
   };
   public static final LobCreationContext.Callback CREATE_CLOB_CALLBACK = new LobCreationContext.Callback() {
      public Clob executeOnConnection(Connection connection) throws SQLException {
         return connection.createClob();
      }
   };
   public static final LobCreationContext.Callback CREATE_NCLOB_CALLBACK = new LobCreationContext.Callback() {
      public NClob executeOnConnection(Connection connection) throws SQLException {
         return connection.createNClob();
      }
   };

   public ContextualLobCreator(LobCreationContext lobCreationContext) {
      this.lobCreationContext = lobCreationContext;
   }

   public Blob createBlob() {
      return (Blob)this.lobCreationContext.execute(CREATE_BLOB_CALLBACK);
   }

   public Blob createBlob(byte[] bytes) {
      try {
         Blob blob = this.createBlob();
         blob.setBytes(1L, bytes);
         return blob;
      } catch (SQLException e) {
         throw new JDBCException("Unable to set BLOB bytes after creation", e);
      }
   }

   public Blob createBlob(InputStream inputStream, long length) {
      return NonContextualLobCreator.INSTANCE.createBlob(inputStream, length);
   }

   public Clob createClob() {
      return (Clob)this.lobCreationContext.execute(CREATE_CLOB_CALLBACK);
   }

   public Clob createClob(String string) {
      try {
         Clob clob = this.createClob();
         clob.setString(1L, string);
         return clob;
      } catch (SQLException e) {
         throw new JDBCException("Unable to set CLOB string after creation", e);
      }
   }

   public Clob createClob(Reader reader, long length) {
      return NonContextualLobCreator.INSTANCE.createClob(reader, length);
   }

   public NClob createNClob() {
      return (NClob)this.lobCreationContext.execute(CREATE_NCLOB_CALLBACK);
   }

   public NClob createNClob(String string) {
      try {
         NClob nclob = this.createNClob();
         nclob.setString(1L, string);
         return nclob;
      } catch (SQLException e) {
         throw new JDBCException("Unable to set NCLOB string after creation", e);
      }
   }

   public NClob createNClob(Reader reader, long length) {
      return NonContextualLobCreator.INSTANCE.createNClob(reader, length);
   }
}
