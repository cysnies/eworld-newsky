package org.hibernate.dialect.function;

import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

public class NoArgSQLFunction implements SQLFunction {
   private Type returnType;
   private boolean hasParenthesesIfNoArguments;
   private String name;

   public NoArgSQLFunction(String name, Type returnType) {
      this(name, returnType, true);
   }

   public NoArgSQLFunction(String name, Type returnType, boolean hasParenthesesIfNoArguments) {
      this.returnType = returnType;
      this.hasParenthesesIfNoArguments = hasParenthesesIfNoArguments;
      this.name = name;
   }

   public boolean hasArguments() {
      return false;
   }

   public boolean hasParenthesesIfNoArguments() {
      return this.hasParenthesesIfNoArguments;
   }

   public Type getReturnType(Type argumentType, Mapping mapping) throws QueryException {
      return this.returnType;
   }

   public String render(Type argumentType, List args, SessionFactoryImplementor factory) throws QueryException {
      if (args.size() > 0) {
         throw new QueryException("function takes no arguments: " + this.name);
      } else {
         return this.hasParenthesesIfNoArguments ? this.name + "()" : this.name;
      }
   }
}
