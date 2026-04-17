package org.hibernate.engine.jdbc.batch.spi;

import java.sql.PreparedStatement;

public interface Batch {
   BatchKey getKey();

   void addObserver(BatchObserver var1);

   PreparedStatement getBatchStatement(String var1, boolean var2);

   void addToBatch();

   void execute();

   void release();
}
