package org.hibernate.metamodel.source.annotations;

import org.hibernate.HibernateException;

public class UnknownInheritanceTypeException extends HibernateException {
   public UnknownInheritanceTypeException(String message) {
      super(message);
   }
}
