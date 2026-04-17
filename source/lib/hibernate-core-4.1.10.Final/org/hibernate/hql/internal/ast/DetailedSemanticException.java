package org.hibernate.hql.internal.ast;

import antlr.SemanticException;
import java.io.PrintStream;
import java.io.PrintWriter;

public class DetailedSemanticException extends SemanticException {
   private Throwable cause;
   private boolean showCauseMessage = true;

   public DetailedSemanticException(String message) {
      super(message);
   }

   public DetailedSemanticException(String s, Throwable e) {
      super(s);
      this.cause = e;
   }

   public String toString() {
      return this.cause != null && this.showCauseMessage ? super.toString() + "\n[cause=" + this.cause.toString() + "]" : super.toString();
   }

   public void printStackTrace() {
      super.printStackTrace();
      if (this.cause != null) {
         this.cause.printStackTrace();
      }

   }

   public void printStackTrace(PrintStream s) {
      super.printStackTrace(s);
      if (this.cause != null) {
         s.println("Cause:");
         this.cause.printStackTrace(s);
      }

   }

   public void printStackTrace(PrintWriter w) {
      super.printStackTrace(w);
      if (this.cause != null) {
         w.println("Cause:");
         this.cause.printStackTrace(w);
      }

   }
}
