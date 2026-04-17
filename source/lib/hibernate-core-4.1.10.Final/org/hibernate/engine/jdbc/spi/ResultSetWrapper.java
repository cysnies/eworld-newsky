package org.hibernate.engine.jdbc.spi;

import java.sql.ResultSet;
import org.hibernate.engine.jdbc.ColumnNameCache;

public interface ResultSetWrapper {
   ResultSet wrap(ResultSet var1, ColumnNameCache var2);
}
