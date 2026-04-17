package org.hibernate;

public class AnnotationException extends MappingException {
   public AnnotationException(String msg, Throwable root) {
      super(msg, root);
   }

   public AnnotationException(Throwable root) {
      super(root);
   }

   public AnnotationException(String s) {
      super(s);
   }
}
