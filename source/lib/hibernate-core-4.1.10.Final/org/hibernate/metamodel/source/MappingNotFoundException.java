package org.hibernate.metamodel.source;

import org.hibernate.internal.jaxb.Origin;

public class MappingNotFoundException extends MappingException {
   public MappingNotFoundException(String message, Origin origin) {
      super(message, origin);
   }

   public MappingNotFoundException(Origin origin) {
      super(String.format("Mapping (%s) not found : %s", origin.getType(), origin.getName()), origin);
   }

   public MappingNotFoundException(String message, Throwable root, Origin origin) {
      super(message, root, origin);
   }

   public MappingNotFoundException(Throwable root, Origin origin) {
      super(String.format("Mapping (%s) not found : %s", origin.getType(), origin.getName()), root, origin);
   }
}
