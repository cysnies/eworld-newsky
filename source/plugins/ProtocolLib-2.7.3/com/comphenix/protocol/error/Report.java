package com.comphenix.protocol.error;

import javax.annotation.Nullable;

public class Report {
   private final ReportType type;
   private final Throwable exception;
   private final Object[] messageParameters;
   private final Object[] callerParameters;

   public static ReportBuilder newBuilder(ReportType type) {
      return (new ReportBuilder()).type(type);
   }

   protected Report(ReportType type, @Nullable Throwable exception, @Nullable Object[] messageParameters, @Nullable Object[] callerParameters) {
      if (type == null) {
         throw new IllegalArgumentException("type cannot be NULL.");
      } else {
         this.type = type;
         this.exception = exception;
         this.messageParameters = messageParameters;
         this.callerParameters = callerParameters;
      }
   }

   public String getReportMessage() {
      return this.type.getMessage(this.messageParameters);
   }

   public Object[] getMessageParameters() {
      return this.messageParameters;
   }

   public Object[] getCallerParameters() {
      return this.callerParameters;
   }

   public ReportType getType() {
      return this.type;
   }

   public Throwable getException() {
      return this.exception;
   }

   public boolean hasMessageParameters() {
      return this.messageParameters != null && this.messageParameters.length > 0;
   }

   public boolean hasCallerParameters() {
      return this.callerParameters != null && this.callerParameters.length > 0;
   }

   public static class ReportBuilder {
      private ReportType type;
      private Throwable exception;
      private Object[] messageParameters;
      private Object[] callerParameters;

      private ReportBuilder() {
      }

      public ReportBuilder type(ReportType type) {
         if (type == null) {
            throw new IllegalArgumentException("Report type cannot be set to NULL.");
         } else {
            this.type = type;
            return this;
         }
      }

      public ReportBuilder error(@Nullable Throwable exception) {
         this.exception = exception;
         return this;
      }

      public ReportBuilder messageParam(@Nullable Object... messageParameters) {
         this.messageParameters = messageParameters;
         return this;
      }

      public ReportBuilder callerParam(@Nullable Object... callerParameters) {
         this.callerParameters = callerParameters;
         return this;
      }

      public Report build() {
         return new Report(this.type, this.exception, this.messageParameters, this.callerParameters);
      }
   }
}
