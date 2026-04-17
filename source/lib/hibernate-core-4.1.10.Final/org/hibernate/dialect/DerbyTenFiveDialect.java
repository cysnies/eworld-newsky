package org.hibernate.dialect;

import org.hibernate.dialect.function.AnsiTrimFunction;
import org.hibernate.dialect.function.DerbyConcatFunction;

public class DerbyTenFiveDialect extends DerbyDialect {
   public DerbyTenFiveDialect() {
      this.registerFunction("concat", new DerbyConcatFunction());
      this.registerFunction("trim", new AnsiTrimFunction());
   }

   public boolean supportsSequences() {
      return false;
   }

   public boolean supportsLimit() {
      return true;
   }

   public boolean supportsLimitOffset() {
      return true;
   }
}
