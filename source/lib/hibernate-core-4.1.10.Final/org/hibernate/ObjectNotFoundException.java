package org.hibernate;

import java.io.Serializable;

public class ObjectNotFoundException extends UnresolvableObjectException {
   public ObjectNotFoundException(Serializable identifier, String clazz) {
      super(identifier, clazz);
   }
}
