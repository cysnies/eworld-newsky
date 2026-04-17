package org.hibernate;

import org.hibernate.type.VersionType;

public enum ReplicationMode {
   EXCEPTION {
      public boolean shouldOverwriteCurrentVersion(Object entity, Object currentVersion, Object newVersion, VersionType versionType) {
         throw new AssertionFailure("should not be called");
      }
   },
   IGNORE {
      public boolean shouldOverwriteCurrentVersion(Object entity, Object currentVersion, Object newVersion, VersionType versionType) {
         return false;
      }
   },
   OVERWRITE {
      public boolean shouldOverwriteCurrentVersion(Object entity, Object currentVersion, Object newVersion, VersionType versionType) {
         return true;
      }
   },
   LATEST_VERSION {
      public boolean shouldOverwriteCurrentVersion(Object entity, Object currentVersion, Object newVersion, VersionType versionType) {
         if (versionType == null) {
            return true;
         } else {
            return versionType.getComparator().compare(currentVersion, newVersion) <= 0;
         }
      }
   };

   private ReplicationMode() {
   }

   public abstract boolean shouldOverwriteCurrentVersion(Object var1, Object var2, Object var3, VersionType var4);
}
