package org.hibernate.engine.jdbc.spi;

import java.sql.Connection;

public interface SchemaNameResolver {
   String resolveSchemaName(Connection var1);
}
