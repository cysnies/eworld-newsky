package org.hibernate.engine.jdbc.spi;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.LobCreationContext;
import org.hibernate.engine.jdbc.LobCreator;
import org.hibernate.service.Service;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;

public interface JdbcServices extends Service {
   /** @deprecated */
   @Deprecated
   ConnectionProvider getConnectionProvider();

   Dialect getDialect();

   SqlStatementLogger getSqlStatementLogger();

   SqlExceptionHelper getSqlExceptionHelper();

   ExtractedDatabaseMetaData getExtractedMetaDataSupport();

   LobCreator getLobCreator(LobCreationContext var1);

   ResultSetWrapper getResultSetWrapper();
}
