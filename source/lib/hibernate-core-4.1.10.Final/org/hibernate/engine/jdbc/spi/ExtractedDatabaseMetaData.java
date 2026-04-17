package org.hibernate.engine.jdbc.spi;

import java.util.LinkedHashSet;
import java.util.Set;

public interface ExtractedDatabaseMetaData {
   boolean supportsScrollableResults();

   boolean supportsGetGeneratedKeys();

   boolean supportsBatchUpdates();

   boolean supportsDataDefinitionInTransaction();

   boolean doesDataDefinitionCauseTransactionCommit();

   Set getExtraKeywords();

   SQLStateType getSqlStateType();

   boolean doesLobLocatorUpdateCopy();

   String getConnectionSchemaName();

   String getConnectionCatalogName();

   LinkedHashSet getTypeInfoSet();

   public static enum SQLStateType {
      XOpen,
      SQL99,
      UNKOWN;
   }
}
