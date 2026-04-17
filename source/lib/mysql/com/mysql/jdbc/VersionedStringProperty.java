package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.List;

class VersionedStringProperty {
   int majorVersion;
   int minorVersion;
   int subminorVersion;
   boolean preferredValue = false;
   String propertyInfo;

   VersionedStringProperty(String property) {
      property = property.trim();
      if (property.startsWith("*")) {
         property = property.substring(1);
         this.preferredValue = true;
      }

      if (property.startsWith(">")) {
         property = property.substring(1);
         int charPos = 0;

         for(charPos = 0; charPos < property.length(); ++charPos) {
            char c = property.charAt(charPos);
            if (!Character.isWhitespace(c) && !Character.isDigit(c) && c != '.') {
               break;
            }
         }

         String versionInfo = property.substring(0, charPos);
         List versionParts = StringUtils.split(versionInfo, ".", true);
         this.majorVersion = Integer.parseInt(versionParts.get(0).toString());
         if (versionParts.size() > 1) {
            this.minorVersion = Integer.parseInt(versionParts.get(1).toString());
         } else {
            this.minorVersion = 0;
         }

         if (versionParts.size() > 2) {
            this.subminorVersion = Integer.parseInt(versionParts.get(2).toString());
         } else {
            this.subminorVersion = 0;
         }

         this.propertyInfo = property.substring(charPos);
      } else {
         this.majorVersion = this.minorVersion = this.subminorVersion = 0;
         this.propertyInfo = property;
      }

   }

   VersionedStringProperty(String property, int major, int minor, int subminor) {
      this.propertyInfo = property;
      this.majorVersion = major;
      this.minorVersion = minor;
      this.subminorVersion = subminor;
   }

   boolean isOkayForVersion(Connection conn) throws SQLException {
      return conn.versionMeetsMinimum(this.majorVersion, this.minorVersion, this.subminorVersion);
   }

   public String toString() {
      return this.propertyInfo;
   }
}
