package org.hibernate;

import java.io.Serializable;

public class ObjectDeletedException extends UnresolvableObjectException {
   public ObjectDeletedException(String message, Serializable identifier, String clazz) {
      super(message, identifier, clazz);
   }
}
