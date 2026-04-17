package com.mysql.jdbc;

public class ConnectionFeatureNotAvailableException extends CommunicationsException {
   public ConnectionFeatureNotAvailableException(ConnectionImpl conn, long lastPacketSentTimeMs, Exception underlyingException) {
      super(conn, lastPacketSentTimeMs, 0L, underlyingException);
   }

   public String getMessage() {
      return "Feature not available in this distribution of Connector/J";
   }

   public String getSQLState() {
      return "01S00";
   }
}
