package org.hibernate.loader.custom;

import org.hibernate.HibernateException;

public class NonUniqueDiscoveredSqlAliasException extends HibernateException {
   public NonUniqueDiscoveredSqlAliasException(String message) {
      super(message);
   }
}
