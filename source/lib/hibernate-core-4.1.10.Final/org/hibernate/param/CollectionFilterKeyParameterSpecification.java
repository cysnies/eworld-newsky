package org.hibernate.param;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.Type;

public class CollectionFilterKeyParameterSpecification implements ParameterSpecification {
   private final String collectionRole;
   private final Type keyType;
   private final int queryParameterPosition;

   public CollectionFilterKeyParameterSpecification(String collectionRole, Type keyType, int queryParameterPosition) {
      this.collectionRole = collectionRole;
      this.keyType = keyType;
      this.queryParameterPosition = queryParameterPosition;
   }

   public int bind(PreparedStatement statement, QueryParameters qp, SessionImplementor session, int position) throws SQLException {
      Object value = qp.getPositionalParameterValues()[this.queryParameterPosition];
      this.keyType.nullSafeSet(statement, value, position, session);
      return this.keyType.getColumnSpan(session.getFactory());
   }

   public Type getExpectedType() {
      return this.keyType;
   }

   public void setExpectedType(Type expectedType) {
   }

   public String renderDisplayInfo() {
      return "collection-filter-key=" + this.collectionRole;
   }
}
