package javax.transaction;

public class HeuristicRollbackException extends Exception {
   public HeuristicRollbackException() {
   }

   public HeuristicRollbackException(String msg) {
      super(msg);
   }
}
