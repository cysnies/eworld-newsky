package org.hibernate.param;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.Type;
import org.hibernate.type.VersionType;

public class VersionTypeSeedParameterSpecification implements ParameterSpecification {
   private VersionType type;

   public VersionTypeSeedParameterSpecification(VersionType type) {
      this.type = type;
   }

   public int bind(PreparedStatement statement, QueryParameters qp, SessionImplementor session, int position) throws SQLException {
      this.type.nullSafeSet(statement, this.type.seed(session), position, session);
      return 1;
   }

   public Type getExpectedType() {
      return this.type;
   }

   public void setExpectedType(Type expectedType) {
   }

   public String renderDisplayInfo() {
      return "version-seed, type=" + this.type;
   }
}
