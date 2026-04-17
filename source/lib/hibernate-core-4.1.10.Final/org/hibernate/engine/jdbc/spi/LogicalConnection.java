package org.hibernate.engine.jdbc.spi;

import java.io.Serializable;
import java.sql.Connection;

public interface LogicalConnection extends Serializable {
   boolean isOpen();

   boolean isPhysicallyConnected();

   Connection getConnection();

   Connection getShareableConnectionProxy();

   Connection getDistinctConnectionProxy();

   Connection close();

   void afterTransaction();
}
