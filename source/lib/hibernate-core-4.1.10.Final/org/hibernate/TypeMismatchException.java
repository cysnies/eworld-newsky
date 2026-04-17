package org.hibernate;

public class TypeMismatchException extends HibernateException {
   public TypeMismatchException(Throwable root) {
      super(root);
   }

   public TypeMismatchException(String s) {
      super(s);
   }

   public TypeMismatchException(String string, Throwable root) {
      super(string, root);
   }
}
