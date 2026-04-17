package org.hibernate.id;

import java.io.Serializable;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;

public abstract class AbstractPostInsertGenerator implements PostInsertIdentifierGenerator, BulkInsertionCapableIdentifierGenerator {
   public Serializable generate(SessionImplementor s, Object obj) {
      return IdentifierGeneratorHelper.POST_INSERT_INDICATOR;
   }

   public boolean supportsBulkInsertionIdentifierGeneration() {
      return true;
   }

   public String determineBulkInsertionIdentifierGenerationSelectFragment(Dialect dialect) {
      return null;
   }
}
