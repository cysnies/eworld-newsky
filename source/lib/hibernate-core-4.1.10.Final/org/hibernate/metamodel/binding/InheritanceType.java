package org.hibernate.metamodel.binding;

import org.hibernate.MappingException;

public enum InheritanceType {
   JOINED,
   SINGLE_TABLE,
   TABLE_PER_CLASS,
   NO_INHERITANCE;

   public static InheritanceType get(javax.persistence.InheritanceType jpaType) {
      switch (jpaType) {
         case SINGLE_TABLE:
            return SINGLE_TABLE;
         case JOINED:
            return JOINED;
         case TABLE_PER_CLASS:
            return TABLE_PER_CLASS;
         default:
            throw new MappingException("Unknown jpa inheritance type:" + jpaType.name());
      }
   }
}
