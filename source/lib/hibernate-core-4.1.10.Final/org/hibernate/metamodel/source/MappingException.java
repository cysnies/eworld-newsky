package org.hibernate.metamodel.source;

import org.hibernate.HibernateException;
import org.hibernate.internal.jaxb.Origin;

public class MappingException extends HibernateException {
   private final Origin origin;

   public MappingException(String message, Origin origin) {
      super(message);
      this.origin = origin;
   }

   public MappingException(String message, Throwable root, Origin origin) {
      super(message, root);
      this.origin = origin;
   }

   public Origin getOrigin() {
      return this.origin;
   }
}
