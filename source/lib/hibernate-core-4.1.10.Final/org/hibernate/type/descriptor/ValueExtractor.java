package org.hibernate.type.descriptor;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ValueExtractor {
   Object extract(ResultSet var1, String var2, WrapperOptions var3) throws SQLException;
}
