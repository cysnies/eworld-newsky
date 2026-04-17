package org.hibernate.param;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.Type;

public class PositionalParameterSpecification extends AbstractExplicitParameterSpecification implements ParameterSpecification {
   private final int hqlPosition;

   public PositionalParameterSpecification(int sourceLine, int sourceColumn, int hqlPosition) {
      super(sourceLine, sourceColumn);
      this.hqlPosition = hqlPosition;
   }

   public int bind(PreparedStatement statement, QueryParameters qp, SessionImplementor session, int position) throws SQLException {
      Type type = qp.getPositionalParameterTypes()[this.hqlPosition];
      Object value = qp.getPositionalParameterValues()[this.hqlPosition];
      type.nullSafeSet(statement, value, position, session);
      return type.getColumnSpan(session.getFactory());
   }

   public String renderDisplayInfo() {
      return "ordinal=" + this.hqlPosition + ", expectedType=" + this.getExpectedType();
   }

   public int getHqlPosition() {
      return this.hqlPosition;
   }
}
