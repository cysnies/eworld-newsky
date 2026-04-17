package org.hibernate.metamodel.relational;

import org.hibernate.HibernateException;

public class IllegalIdentifierException extends HibernateException {
   public IllegalIdentifierException(String s) {
      super(s);
   }
}
