package org.hibernate.metamodel.relational;

import org.hibernate.dialect.Dialect;

public interface Exportable {
   String getExportIdentifier();

   String[] sqlCreateStrings(Dialect var1);

   String[] sqlDropStrings(Dialect var1);
}
