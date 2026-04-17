package javax.transaction;

public class NotSupportedException extends Exception {
   public NotSupportedException() {
   }

   public NotSupportedException(String msg) {
      super(msg);
   }
}
