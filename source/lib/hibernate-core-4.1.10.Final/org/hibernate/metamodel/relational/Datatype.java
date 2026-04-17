package org.hibernate.metamodel.relational;

public class Datatype {
   private final int typeCode;
   private final String typeName;
   private final Class javaType;
   private final int hashCode;

   public Datatype(int typeCode, String typeName, Class javaType) {
      this.typeCode = typeCode;
      this.typeName = typeName;
      this.javaType = javaType;
      this.hashCode = this.generateHashCode();
   }

   private int generateHashCode() {
      int result = this.typeCode;
      if (this.typeName != null) {
         result = 31 * result + this.typeName.hashCode();
      }

      if (this.javaType != null) {
         result = 31 * result + this.javaType.hashCode();
      }

      return result;
   }

   public int getTypeCode() {
      return this.typeCode;
   }

   public String getTypeName() {
      return this.typeName;
   }

   public Class getJavaType() {
      return this.javaType;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Datatype datatype = (Datatype)o;
         return this.typeCode == datatype.typeCode && this.javaType.equals(datatype.javaType) && this.typeName.equals(datatype.typeName);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.hashCode;
   }

   public String toString() {
      return super.toString() + "[code=" + this.typeCode + ", name=" + this.typeName + ", javaClass=" + this.javaType.getName() + "]";
   }
}
