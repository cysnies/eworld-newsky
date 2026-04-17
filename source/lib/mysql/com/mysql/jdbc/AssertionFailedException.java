package com.mysql.jdbc;

public class AssertionFailedException extends RuntimeException {
   public static void shouldNotHappen(Exception ex) throws AssertionFailedException {
      throw new AssertionFailedException(ex);
   }

   public AssertionFailedException(Exception ex) {
      super(Messages.getString("AssertionFailedException.0") + ex.toString() + Messages.getString("AssertionFailedException.1"));
   }
}
