package org.hibernate.cache.spi.access;

import org.hibernate.HibernateException;

public class UnknownAccessTypeException extends HibernateException {
   public UnknownAccessTypeException(String accessTypeName) {
      super("Unknown access type [" + accessTypeName + "]");
   }
}
