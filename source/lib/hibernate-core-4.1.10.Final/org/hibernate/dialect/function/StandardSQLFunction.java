package org.hibernate.dialect.function;

import java.util.List;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

public class StandardSQLFunction implements SQLFunction {
   private final String name;
   private final Type registeredType;

   public StandardSQLFunction(String name) {
      this(name, (Type)null);
   }

   public StandardSQLFunction(String name, Type registeredType) {
      this.name = name;
      this.registeredType = registeredType;
   }

   public String getName() {
      return this.name;
   }

   public Type getType() {
      return this.registeredType;
   }

   public boolean hasArguments() {
      return true;
   }

   public boolean hasParenthesesIfNoArguments() {
      return true;
   }

   public Type getReturnType(Type firstArgumentType, Mapping mapping) {
      return this.registeredType == null ? firstArgumentType : this.registeredType;
   }

   public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor sessionFactory) {
      StringBuilder buf = new StringBuilder();
      buf.append(this.name).append('(');

      for(int i = 0; i < arguments.size(); ++i) {
         buf.append(arguments.get(i));
         if (i < arguments.size() - 1) {
            buf.append(", ");
         }
      }

      return buf.append(')').toString();
   }

   public String toString() {
      return this.name;
   }
}
