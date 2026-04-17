package org.hibernate.dialect;

public class MySQLMyISAMDialect extends MySQLDialect {
   public String getTableTypeString() {
      return " type=MyISAM";
   }

   public boolean dropConstraints() {
      return false;
   }
}
