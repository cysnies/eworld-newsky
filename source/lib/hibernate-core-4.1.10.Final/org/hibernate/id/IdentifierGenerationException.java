package org.hibernate.id;

import org.hibernate.HibernateException;

public class IdentifierGenerationException extends HibernateException {
   public IdentifierGenerationException(String msg) {
      super(msg);
   }

   public IdentifierGenerationException(String msg, Throwable t) {
      super(msg, t);
   }
}
