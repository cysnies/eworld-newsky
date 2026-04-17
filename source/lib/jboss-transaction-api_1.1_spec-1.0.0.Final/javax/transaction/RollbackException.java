package javax.transaction;

public class RollbackException extends Exception {
   public RollbackException() {
   }

   public RollbackException(String msg) {
      super(msg);
   }
}
