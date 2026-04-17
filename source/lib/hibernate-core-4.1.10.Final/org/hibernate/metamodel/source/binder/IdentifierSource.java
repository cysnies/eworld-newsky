package org.hibernate.metamodel.source.binder;

import org.hibernate.metamodel.binding.IdGenerator;

public interface IdentifierSource {
   IdGenerator getIdentifierGeneratorDescriptor();

   Nature getNature();

   public static enum Nature {
      SIMPLE,
      COMPOSITE,
      AGGREGATED_COMPOSITE;
   }
}
