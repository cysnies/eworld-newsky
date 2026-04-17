package org.hibernate.dialect.function;

import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class ConvertFunction implements SQLFunction {
   public boolean hasArguments() {
      return true;
   }

   public boolean hasParenthesesIfNoArguments() {
      return true;
   }

   public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
      return StandardBasicTypes.STRING;
   }

   public String render(Type firstArgumentType, List args, SessionFactoryImplementor factory) throws QueryException {
      if (args.size() != 2 && args.size() != 3) {
         throw new QueryException("convert() requires two or three arguments");
      } else {
         String type = (String)args.get(1);
         return args.size() == 2 ? "{fn convert(" + args.get(0) + " , " + type + ")}" : "convert(" + args.get(0) + " , " + type + "," + args.get(2) + ")";
      }
   }
}
