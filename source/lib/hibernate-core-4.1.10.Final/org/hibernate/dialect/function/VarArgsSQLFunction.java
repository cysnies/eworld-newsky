package org.hibernate.dialect.function;

import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

public class VarArgsSQLFunction implements SQLFunction {
   private final String begin;
   private final String sep;
   private final String end;
   private final Type registeredType;

   public VarArgsSQLFunction(Type registeredType, String begin, String sep, String end) {
      this.registeredType = registeredType;
      this.begin = begin;
      this.sep = sep;
      this.end = end;
   }

   public VarArgsSQLFunction(String begin, String sep, String end) {
      this((Type)null, begin, sep, end);
   }

   public boolean hasArguments() {
      return true;
   }

   public boolean hasParenthesesIfNoArguments() {
      return true;
   }

   public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
      return this.registeredType == null ? firstArgumentType : this.registeredType;
   }

   public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) {
      StringBuilder buf = (new StringBuilder()).append(this.begin);

      for(int i = 0; i < arguments.size(); ++i) {
         buf.append(this.transformArgument((String)arguments.get(i)));
         if (i < arguments.size() - 1) {
            buf.append(this.sep);
         }
      }

      return buf.append(this.end).toString();
   }

   protected String transformArgument(String argument) {
      return argument;
   }
}
