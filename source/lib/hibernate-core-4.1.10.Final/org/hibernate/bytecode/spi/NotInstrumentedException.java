package org.hibernate.bytecode.spi;

import org.hibernate.HibernateException;

public class NotInstrumentedException extends HibernateException {
   public NotInstrumentedException(String message) {
      super(message);
   }

   public NotInstrumentedException(String message, Throwable root) {
      super(message, root);
   }
}
