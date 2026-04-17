package org.hibernate.dialect.function;

import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class CharIndexFunction implements SQLFunction {
   public boolean hasArguments() {
      return true;
   }

   public boolean hasParenthesesIfNoArguments() {
      return true;
   }

   public Type getReturnType(Type columnType, Mapping mapping) throws QueryException {
      return StandardBasicTypes.INTEGER;
   }

   public String render(Type columnType, List args, SessionFactoryImplementor factory) throws QueryException {
      boolean threeArgs = args.size() > 2;
      Object pattern = args.get(0);
      Object string = args.get(1);
      Object start = threeArgs ? args.get(2) : null;
      StringBuilder buf = new StringBuilder();
      buf.append("charindex(").append(pattern).append(", ");
      if (threeArgs) {
         buf.append("right(");
      }

      buf.append(string);
      if (threeArgs) {
         buf.append(", char_length(").append(string).append(")-(").append(start).append("-1))");
      }

      buf.append(')');
      return buf.toString();
   }
}
