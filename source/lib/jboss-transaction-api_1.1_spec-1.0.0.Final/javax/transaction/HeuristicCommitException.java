package javax.transaction;

public class HeuristicCommitException extends Exception {
   public HeuristicCommitException() {
   }

   public HeuristicCommitException(String msg) {
      super(msg);
   }
}
