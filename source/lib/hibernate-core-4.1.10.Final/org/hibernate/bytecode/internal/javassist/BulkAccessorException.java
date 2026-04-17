package org.hibernate.bytecode.internal.javassist;

public class BulkAccessorException extends RuntimeException {
   private Throwable myCause;
   private int index;

   public Throwable getCause() {
      return this.myCause == this ? null : this.myCause;
   }

   public synchronized Throwable initCause(Throwable cause) {
      this.myCause = cause;
      return this;
   }

   public BulkAccessorException(String message) {
      super(message);
      this.index = -1;
      this.initCause((Throwable)null);
   }

   public BulkAccessorException(String message, int index) {
      this(message + ": " + index);
      this.index = index;
   }

   public BulkAccessorException(String message, Throwable cause) {
      super(message);
      this.index = -1;
      this.initCause(cause);
   }

   public BulkAccessorException(Throwable cause, int index) {
      this("Property " + index);
      this.index = index;
      this.initCause(cause);
   }

   public int getIndex() {
      return this.index;
   }
}
