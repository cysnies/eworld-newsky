package javax.persistence.metamodel;

import java.lang.reflect.Member;

public interface Attribute {
   String getName();

   PersistentAttributeType getPersistentAttributeType();

   ManagedType getDeclaringType();

   Class getJavaType();

   Member getJavaMember();

   boolean isAssociation();

   boolean isCollection();

   public static enum PersistentAttributeType {
      MANY_TO_ONE,
      ONE_TO_ONE,
      BASIC,
      EMBEDDED,
      MANY_TO_MANY,
      ONE_TO_MANY,
      ELEMENT_COLLECTION;
   }
}
