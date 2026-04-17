package org.hibernate.engine.jdbc.spi;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.Statement;

public interface JdbcResourceRegistry extends Serializable {
   void register(Statement var1);

   void release(Statement var1);

   void register(ResultSet var1);

   void release(ResultSet var1);

   boolean hasRegisteredResources();

   void releaseResources();

   void close();

   void registerLastQuery(Statement var1);

   void cancelLastQuery();
}
