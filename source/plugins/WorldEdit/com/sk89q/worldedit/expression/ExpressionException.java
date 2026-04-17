package com.sk89q.worldedit.expression;

public class ExpressionException extends Exception {
   private static final long serialVersionUID = 1L;
   private final int position;

   public ExpressionException(int position) {
      this.position = position;
   }

   public ExpressionException(int position, String message, Throwable cause) {
      super(message, cause);
      this.position = position;
   }

   public ExpressionException(int position, String message) {
      super(message);
      this.position = position;
   }

   public ExpressionException(int position, Throwable cause) {
      super(cause);
      this.position = position;
   }

   public int getPosition() {
      return this.position;
   }
}
