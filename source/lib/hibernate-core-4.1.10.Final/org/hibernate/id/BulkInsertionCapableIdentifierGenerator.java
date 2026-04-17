package org.hibernate.id;

import org.hibernate.dialect.Dialect;

public interface BulkInsertionCapableIdentifierGenerator extends IdentifierGenerator {
   boolean supportsBulkInsertionIdentifierGeneration();

   String determineBulkInsertionIdentifierGenerationSelectFragment(Dialect var1);
}
