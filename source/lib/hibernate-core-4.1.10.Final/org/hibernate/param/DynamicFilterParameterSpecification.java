package org.hibernate.param;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.Type;

public class DynamicFilterParameterSpecification implements ParameterSpecification {
   private final String filterName;
   private final String parameterName;
   private final Type definedParameterType;

   public DynamicFilterParameterSpecification(String filterName, String parameterName, Type definedParameterType) {
      this.filterName = filterName;
      this.parameterName = parameterName;
      this.definedParameterType = definedParameterType;
   }

   public int bind(PreparedStatement statement, QueryParameters qp, SessionImplementor session, int start) throws SQLException {
      int columnSpan = this.definedParameterType.getColumnSpan(session.getFactory());
      Object value = session.getLoadQueryInfluencers().getFilterParameterValue(this.filterName + '.' + this.parameterName);
      if (!Collection.class.isInstance(value)) {
         this.definedParameterType.nullSafeSet(statement, value, start, session);
         return columnSpan;
      } else {
         int positions = 0;

         for(Iterator itr = ((Collection)value).iterator(); itr.hasNext(); positions += columnSpan) {
            this.definedParameterType.nullSafeSet(statement, itr.next(), start + positions, session);
         }

         return positions;
      }
   }

   public Type getExpectedType() {
      return this.definedParameterType;
   }

   public void setExpectedType(Type expectedType) {
   }

   public String renderDisplayInfo() {
      return "dynamic-filter={filterName=" + this.filterName + ",paramName=" + this.parameterName + "}";
   }
}
