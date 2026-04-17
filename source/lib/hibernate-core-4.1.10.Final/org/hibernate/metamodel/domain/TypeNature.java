package org.hibernate.metamodel.domain;

public enum TypeNature {
   BASIC("basic"),
   COMPONENT("component"),
   ENTITY("entity"),
   SUPERCLASS("superclass"),
   NON_ENTITY("non-entity");

   private final String name;

   private TypeNature(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public String toString() {
      return super.toString() + "[" + this.getName() + "]";
   }
}
