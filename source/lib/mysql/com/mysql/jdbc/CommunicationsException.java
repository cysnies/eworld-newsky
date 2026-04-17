package com.mysql.jdbc;

import java.sql.SQLException;

public class CommunicationsException extends SQLException implements StreamingNotifiable {
   private String exceptionMessage;
   private boolean streamingResultSetInPlay = false;

   public CommunicationsException(ConnectionImpl conn, long lastPacketSentTimeMs, long lastPacketReceivedTimeMs, Exception underlyingException) {
      this.exceptionMessage = SQLError.createLinkFailureMessageBasedOnHeuristics(conn, lastPacketSentTimeMs, lastPacketReceivedTimeMs, underlyingException, this.streamingResultSetInPlay);
      if (underlyingException != null) {
         this.initCause(underlyingException);
      }

   }

   public String getMessage() {
      return this.exceptionMessage;
   }

   public String getSQLState() {
      return "08S01";
   }

   public void setWasStreamingResults() {
      this.streamingResultSetInPlay = true;
   }
}
