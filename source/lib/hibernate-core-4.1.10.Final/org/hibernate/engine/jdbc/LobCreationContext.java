package org.hibernate.engine.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface LobCreationContext {
   Object execute(Callback var1);

   public interface Callback {
      Object executeOnConnection(Connection var1) throws SQLException;
   }
}
