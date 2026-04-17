package org.hibernate.param;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.TypedValue;

public class NamedParameterSpecification extends AbstractExplicitParameterSpecification implements ParameterSpecification {
   private final String name;

   public NamedParameterSpecification(int sourceLine, int sourceColumn, String name) {
      super(sourceLine, sourceColumn);
      this.name = name;
   }

   public int bind(PreparedStatement statement, QueryParameters qp, SessionImplementor session, int position) throws SQLException {
      TypedValue typedValue = (TypedValue)qp.getNamedParameters().get(this.name);
      typedValue.getType().nullSafeSet(statement, typedValue.getValue(), position, session);
      return typedValue.getType().getColumnSpan(session.getFactory());
   }

   public String renderDisplayInfo() {
      return "name=" + this.name + ", expectedType=" + this.getExpectedType();
   }

   public String getName() {
      return this.name;
   }
}
