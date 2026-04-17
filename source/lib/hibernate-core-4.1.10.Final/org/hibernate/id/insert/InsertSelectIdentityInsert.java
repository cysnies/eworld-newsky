package org.hibernate.id.insert;

import org.hibernate.dialect.Dialect;

public class InsertSelectIdentityInsert extends IdentifierGeneratingInsert {
   public InsertSelectIdentityInsert(Dialect dialect) {
      super(dialect);
   }

   public String toStatementString() {
      return this.getDialect().appendIdentitySelectToInsert(super.toStatementString());
   }
}
