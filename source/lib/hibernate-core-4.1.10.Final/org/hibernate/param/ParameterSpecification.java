package org.hibernate.param;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.Type;

public interface ParameterSpecification {
   int bind(PreparedStatement var1, QueryParameters var2, SessionImplementor var3, int var4) throws SQLException;

   Type getExpectedType();

   void setExpectedType(Type var1);

   String renderDisplayInfo();
}
