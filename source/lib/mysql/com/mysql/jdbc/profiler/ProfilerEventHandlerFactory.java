package com.mysql.jdbc.profiler;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.log.Log;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ProfilerEventHandlerFactory {
   private static final Map CONNECTIONS_TO_SINKS = new HashMap();
   private Connection ownerConnection = null;
   private Log log = null;

   public static synchronized ProfilerEventHandler getInstance(Connection conn) throws SQLException {
      ProfilerEventHandler handler = (ProfilerEventHandler)CONNECTIONS_TO_SINKS.get(conn);
      if (handler == null) {
         handler = (ProfilerEventHandler)Util.getInstance(conn.getProfilerEventHandler(), new Class[0], new Object[0]);
         conn.initializeExtension(handler);
         CONNECTIONS_TO_SINKS.put(conn, handler);
      }

      return handler;
   }

   public static synchronized void removeInstance(Connection conn) {
      ProfilerEventHandler handler = (ProfilerEventHandler)CONNECTIONS_TO_SINKS.remove(conn);
      if (handler != null) {
         handler.destroy();
      }

   }

   private ProfilerEventHandlerFactory(Connection conn) {
      this.ownerConnection = conn;

      try {
         this.log = this.ownerConnection.getLog();
      } catch (SQLException var3) {
         throw new RuntimeException("Unable to get logger from connection");
      }
   }
}
