package javax.persistence.metamodel;

public interface Type {
   PersistenceType getPersistenceType();

   Class getJavaType();

   public static enum PersistenceType {
      ENTITY,
      EMBEDDABLE,
      MAPPED_SUPERCLASS,
      BASIC;
   }
}
