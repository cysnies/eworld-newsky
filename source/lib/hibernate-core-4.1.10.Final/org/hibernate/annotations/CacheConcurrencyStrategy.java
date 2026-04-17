package org.hibernate.annotations;

public enum CacheConcurrencyStrategy {
   NONE((org.hibernate.cache.spi.access.AccessType)null),
   READ_ONLY(org.hibernate.cache.spi.access.AccessType.READ_ONLY),
   NONSTRICT_READ_WRITE(org.hibernate.cache.spi.access.AccessType.NONSTRICT_READ_WRITE),
   READ_WRITE(org.hibernate.cache.spi.access.AccessType.READ_WRITE),
   TRANSACTIONAL(org.hibernate.cache.spi.access.AccessType.TRANSACTIONAL);

   private final org.hibernate.cache.spi.access.AccessType accessType;

   private CacheConcurrencyStrategy(org.hibernate.cache.spi.access.AccessType accessType) {
      this.accessType = accessType;
   }

   private boolean isMatch(String name) {
      return this.accessType != null && this.accessType.getExternalName().equalsIgnoreCase(name) || this.name().equalsIgnoreCase(name);
   }

   public static CacheConcurrencyStrategy fromAccessType(org.hibernate.cache.spi.access.AccessType accessType) {
      if (null == accessType) {
         return NONE;
      } else {
         switch (accessType) {
            case READ_ONLY:
               return READ_ONLY;
            case READ_WRITE:
               return READ_WRITE;
            case NONSTRICT_READ_WRITE:
               return NONSTRICT_READ_WRITE;
            case TRANSACTIONAL:
               return TRANSACTIONAL;
            default:
               return NONE;
         }
      }
   }

   public static CacheConcurrencyStrategy parse(String name) {
      if (READ_ONLY.isMatch(name)) {
         return READ_ONLY;
      } else if (READ_WRITE.isMatch(name)) {
         return READ_WRITE;
      } else if (NONSTRICT_READ_WRITE.isMatch(name)) {
         return NONSTRICT_READ_WRITE;
      } else if (TRANSACTIONAL.isMatch(name)) {
         return TRANSACTIONAL;
      } else {
         return NONE.isMatch(name) ? NONE : null;
      }
   }

   public org.hibernate.cache.spi.access.AccessType toAccessType() {
      return this.accessType;
   }
}
