package org.hibernate.engine.jdbc.spi;

import java.sql.PreparedStatement;
import org.hibernate.ScrollMode;

public interface StatementPreparer {
   PreparedStatement prepareStatement(String var1);

   PreparedStatement prepareStatement(String var1, boolean var2);

   PreparedStatement prepareStatement(String var1, int var2);

   PreparedStatement prepareStatement(String var1, String[] var2);

   PreparedStatement prepareQueryStatement(String var1, boolean var2, ScrollMode var3);
}
