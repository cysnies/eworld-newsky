package org.hibernate.type.descriptor;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ValueBinder {
   void bind(PreparedStatement var1, Object var2, int var3, WrapperOptions var4) throws SQLException;
}
