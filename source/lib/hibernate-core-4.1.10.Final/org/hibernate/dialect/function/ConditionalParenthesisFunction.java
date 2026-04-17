package org.hibernate.dialect.function;

import java.util.List;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

public class ConditionalParenthesisFunction extends StandardSQLFunction {
   public ConditionalParenthesisFunction(String name) {
      super(name);
   }

   public ConditionalParenthesisFunction(String name, Type type) {
      super(name, type);
   }

   public boolean hasParenthesesIfNoArguments() {
      return false;
   }

   public String render(List args, SessionFactoryImplementor factory) {
      boolean hasArgs = !args.isEmpty();
      StringBuilder buf = new StringBuilder();
      buf.append(this.getName());
      if (hasArgs) {
         buf.append("(");

         for(int i = 0; i < args.size(); ++i) {
            buf.append(args.get(i));
            if (i < args.size() - 1) {
               buf.append(", ");
            }
         }

         buf.append(")");
      }

      return buf.toString();
   }
}
