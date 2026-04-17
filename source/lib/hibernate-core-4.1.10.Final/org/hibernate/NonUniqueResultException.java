package org.hibernate;

public class NonUniqueResultException extends HibernateException {
   public NonUniqueResultException(int resultCount) {
      super("query did not return a unique result: " + resultCount);
   }
}
