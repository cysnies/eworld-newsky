package org.hibernate.dialect;

public class DerbyTenSixDialect extends DerbyTenFiveDialect {
   public boolean supportsSequences() {
      return true;
   }
}
