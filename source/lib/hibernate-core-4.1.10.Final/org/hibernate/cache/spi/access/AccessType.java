package org.hibernate.cache.spi.access;

public enum AccessType {
   READ_ONLY("read-only"),
   READ_WRITE("read-write"),
   NONSTRICT_READ_WRITE("nonstrict-read-write"),
   TRANSACTIONAL("transactional");

   private final String externalName;

   private AccessType(String externalName) {
      this.externalName = externalName;
   }

   public String getExternalName() {
      return this.externalName;
   }

   public String toString() {
      return "AccessType[" + this.externalName + "]";
   }

   public static AccessType fromExternalName(String externalName) {
      if (externalName == null) {
         return null;
      } else {
         for(AccessType accessType : values()) {
            if (accessType.getExternalName().equals(externalName)) {
               return accessType;
            }
         }

         throw new UnknownAccessTypeException(externalName);
      }
   }
}
