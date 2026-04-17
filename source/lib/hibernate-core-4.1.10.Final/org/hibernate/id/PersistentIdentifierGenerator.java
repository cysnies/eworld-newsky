package org.hibernate.id;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;

public interface PersistentIdentifierGenerator extends IdentifierGenerator {
   String SCHEMA = "schema";
   String TABLE = "target_table";
   String TABLES = "identity_tables";
   String PK = "target_column";
   String CATALOG = "catalog";
   String IDENTIFIER_NORMALIZER = "identifier_normalizer";

   String[] sqlCreateStrings(Dialect var1) throws HibernateException;

   String[] sqlDropStrings(Dialect var1) throws HibernateException;

   Object generatorKey();
}
