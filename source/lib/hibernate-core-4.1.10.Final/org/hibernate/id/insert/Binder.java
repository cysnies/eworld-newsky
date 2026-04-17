package org.hibernate.id.insert;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface Binder {
   void bindValues(PreparedStatement var1) throws SQLException;

   Object getEntity();
}
