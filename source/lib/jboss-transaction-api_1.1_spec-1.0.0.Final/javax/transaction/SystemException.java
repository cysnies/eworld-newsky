package javax.transaction;

public class SystemException extends Exception {
   public int errorCode;

   public SystemException() {
   }

   public SystemException(String msg) {
      super(msg);
   }

   public SystemException(int errcode) {
      this.errorCode = errcode;
   }
}
