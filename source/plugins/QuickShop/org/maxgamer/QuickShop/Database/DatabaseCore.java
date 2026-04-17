package org.maxgamer.QuickShop.Database;

import java.sql.Connection;

public interface DatabaseCore {
   Connection getConnection();

   void queue(BufferStatement var1);

   void flush();

   void close();
}
