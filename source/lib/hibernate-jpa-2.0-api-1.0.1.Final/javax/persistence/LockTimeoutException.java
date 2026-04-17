package javax.persistence;

public class LockTimeoutException extends PersistenceException {
   Object entity;

   public LockTimeoutException() {
   }

   public LockTimeoutException(String message) {
      super(message);
   }

   public LockTimeoutException(String message, Throwable cause) {
      super(message, cause);
   }

   public LockTimeoutException(Throwable cause) {
      super(cause);
   }

   public LockTimeoutException(Object entity) {
      this.entity = entity;
   }

   public LockTimeoutException(String message, Throwable cause, Object entity) {
      super(message, cause);
      this.entity = entity;
   }

   public Object getObject() {
      return this.entity;
   }
}
