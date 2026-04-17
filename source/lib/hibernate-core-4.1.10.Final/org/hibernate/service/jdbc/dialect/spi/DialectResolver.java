package org.hibernate.service.jdbc.dialect.spi;

import java.sql.DatabaseMetaData;
import org.hibernate.dialect.Dialect;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.service.Service;

public interface DialectResolver extends Service {
   Dialect resolveDialect(DatabaseMetaData var1) throws JDBCConnectionException;
}
