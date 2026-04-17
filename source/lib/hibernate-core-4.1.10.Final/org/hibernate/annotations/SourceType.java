package org.hibernate.annotations;

public enum SourceType {
   VM("timestamp"),
   DB("dbtimestamp");

   private final String typeName;

   private SourceType(String typeName) {
      this.typeName = typeName;
   }

   public String typeName() {
      return this.typeName;
   }
}
