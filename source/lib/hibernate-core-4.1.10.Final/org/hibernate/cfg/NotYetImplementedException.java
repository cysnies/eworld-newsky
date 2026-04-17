package org.hibernate.cfg;

import org.hibernate.MappingException;

public class NotYetImplementedException extends MappingException {
   public NotYetImplementedException() {
      this("Not yet implemented!");
   }

   public NotYetImplementedException(String msg, Throwable root) {
      super(msg, root);
   }

   public NotYetImplementedException(Throwable root) {
      super(root);
   }

   public NotYetImplementedException(String s) {
      super(s);
   }
}
