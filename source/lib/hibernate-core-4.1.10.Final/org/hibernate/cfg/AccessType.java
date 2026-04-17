package org.hibernate.cfg;

public enum AccessType {
   DEFAULT("property"),
   PROPERTY("property"),
   FIELD("field");

   private final String accessType;

   private AccessType(String type) {
      this.accessType = type;
   }

   public String getType() {
      return this.accessType;
   }

   public static AccessType getAccessStrategy(String type) {
      if (type == null) {
         return DEFAULT;
      } else if (FIELD.getType().equals(type)) {
         return FIELD;
      } else {
         return PROPERTY.getType().equals(type) ? PROPERTY : DEFAULT;
      }
   }

   public static AccessType getAccessStrategy(javax.persistence.AccessType type) {
      if (javax.persistence.AccessType.PROPERTY.equals(type)) {
         return PROPERTY;
      } else {
         return javax.persistence.AccessType.FIELD.equals(type) ? FIELD : DEFAULT;
      }
   }
}
